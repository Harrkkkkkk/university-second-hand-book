import request from './request'

export function recognizeText(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/ocr/recognize',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}
