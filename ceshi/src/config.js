// 前端运行时配置
// VUE_APP_API_BASE_URL 由 Docker 构建时注入，生产环境为空（相对路径）
// 本地开发时可设为 http://localhost:8080

const API_BASE_URL = process.env.VUE_APP_API_BASE_URL || ''

export { API_BASE_URL }
