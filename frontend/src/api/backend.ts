/**
 * 后端接口封装。
 *
 * 注意这里请求的是 `/api/...` 这样的相对路径，不是 `http://localhost:8080/api/...`。
 * 原因：vite.config.ts 里配了 proxy，开发时请求会被转发到 Spring Boot；
 * 打包上线时交给 Nginx 之类的反向代理处理。好处是前端代码里不写死后端地址，
 * 也不会有跨域（CORS）问题。
 */

/** 后端统一返回结构 */
export interface ApiResult<T> {
  code: number
  data: T
}

/** /api/env 返回的运行环境信息 */
export interface EnvInfo {
  javaVersion: string
  javaVendor: string
  javaHome: string
  osName: string
  userDir: string
}

async function request<T>(path: string, init?: RequestInit): Promise<ApiResult<T>> {
  const response = await fetch(`/api${path}`, init)
  if (!response.ok) {
    throw new Error(`请求失败：HTTP ${response.status}`)
  }
  return (await response.json()) as ApiResult<T>
}

export const backendApi = {
  /** GET /api/hello */
  hello: () => request<string>('/hello'),

  /** GET /api/hello/{name} */
  helloName: (name: string) => request<string>(`/hello/${encodeURIComponent(name)}`),

  /** GET /api/echo?msg=... */
  echo: (msg: string) => request<string>(`/echo?msg=${encodeURIComponent(msg)}`),

  /** GET /api/time */
  time: () => request<string>('/time'),

  /** GET /api/env */
  env: () => request<EnvInfo>('/env'),
}
