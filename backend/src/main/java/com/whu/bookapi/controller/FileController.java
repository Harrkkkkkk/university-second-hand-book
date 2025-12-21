package com.whu.bookapi.controller;

import com.whu.bookapi.model.User;
import com.whu.bookapi.service.FileStorageService;
import com.whu.bookapi.service.UserService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {
    private final FileStorageService fileStorageService;
    private final UserService userService;

    public FileController(FileStorageService fileStorageService, UserService userService) {
        this.fileStorageService = fileStorageService;
        this.userService = userService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestHeader(value = "token", required = false) String token,
                                    @RequestPart("file") MultipartFile file) {
        User u = userService.getByToken(token);
        if (u == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        long id;
        try {
            id = fileStorageService.save(file, u.getUsername());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/files/")
                .path(Long.toString(id))
                .path("/raw")
                .build()
                .toUri()
                .getPath();
        Map<String, Object> resp = new HashMap<>();
        resp.put("id", id);
        resp.put("url", url);
        resp.put("originalName", file.getOriginalFilename());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}/meta")
    public ResponseEntity<?> meta(@PathVariable("id") long id) {
        Map<String, Object> meta = fileStorageService.getMeta(id);
        if (meta == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        return ResponseEntity.ok(meta);
    }

    @GetMapping("/{id}/raw")
    public ResponseEntity<?> raw(@PathVariable("id") long id) {
        FileStorageService.StoredFileLocation loc = fileStorageService.getLocation(id);
        if (loc == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        try {
            Path p = Path.of(loc.storagePath()).toAbsolutePath().normalize();
            Resource r = new UrlResource(p.toUri());
            if (!r.exists() || !r.isReadable()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

            MediaType mt = MediaType.APPLICATION_OCTET_STREAM;
            if (loc.contentType() != null && !loc.contentType().isBlank()) {
                mt = MediaType.parseMediaType(loc.contentType());
            }
            return ResponseEntity.ok()
                    .contentType(mt)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + safeFilename(loc.originalName()) + "\"")
                    .body(r);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private static String safeFilename(String name) {
        if (name == null || name.isBlank()) return "file";
        return name.replaceAll("[\\r\\n\"]", "_");
    }
}
