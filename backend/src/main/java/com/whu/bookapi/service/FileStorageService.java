package com.whu.bookapi.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;

@Service
public class FileStorageService {
    private final JdbcTemplate jdbcTemplate;
    private final Path uploadDir;

    public FileStorageService(JdbcTemplate jdbcTemplate, @Value("${app.files.upload-dir:./uploads}") String uploadDir) {
        this.jdbcTemplate = jdbcTemplate;
        this.uploadDir = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    public long save(MultipartFile file, String uploader) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }
        String originalName0 = file.getOriginalFilename();
        final String originalName = (originalName0 == null || originalName0.isBlank()) ? "file" : originalName0;

        String storedName = UUID.randomUUID().toString().replace("-", "");
        Path target = ensureUploadDir().resolve(storedName);

        String sha256;
        try (InputStream in = file.getInputStream()) {
            sha256 = sha256Hex(in);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash file", e);
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store file", e);
        }

        long now = System.currentTimeMillis();
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = "INSERT INTO stored_file (original_name, content_type, size, sha256, storage_path, uploader, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, originalName);

            ps.setString(2, file.getContentType());
            ps.setLong(3, file.getSize());
            ps.setString(4, sha256);
            ps.setString(5, target.toString());
            ps.setString(6, uploader);
            ps.setLong(7, now);
            return ps;
        }, keyHolder);
        Number key = keyHolder.getKey();
        if (key == null) throw new RuntimeException("Failed to generate file id");
        return key.longValue();
    }

    public Map<String, Object> getMeta(long id) {
        try {
            return jdbcTemplate.queryForMap(
                    "SELECT id, original_name AS originalName, content_type AS contentType, size, sha256, uploader, created_at AS createdAt FROM stored_file WHERE id = ?",
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public StoredFileLocation getLocation(long id) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT storage_path, original_name, content_type FROM stored_file WHERE id = ?",
                    (rs, rowNum) -> new StoredFileLocation(rs.getString("storage_path"), rs.getString("original_name"), rs.getString("content_type")),
                    id
            );
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    private Path ensureUploadDir() {
        try {
            Files.createDirectories(uploadDir);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create upload dir", e);
        }
        return uploadDir;
    }

    private static String sha256Hex(InputStream in) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) > 0) {
            digest.update(buf, 0, n);
        }
        return HexFormat.of().formatHex(digest.digest());
    }

    public record StoredFileLocation(String storagePath, String originalName, String contentType) {
    }
}
