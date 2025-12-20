import axios from 'axios';
import { getToken, clearAuth } from './authStorage';

/**
 * 后端统一响应：{ code, message, data }
 */
export class ApiError extends Error {
  /**
   * @param {string} message
   * @param {{ httpStatus?: number, code?: number, details?: any }} [options]
   */
  constructor(message, options = {}) {
    super(message);
    this.name = 'ApiError';
    this.httpStatus = options.httpStatus;
    this.code = options.code;
    this.details = options.details;
  }
}

function resolveBaseURL() {
  // 优先使用环境变量；未配置则默认本地后端
  const envBase = import.meta?.env?.VITE_API_BASE_URL;
  // 默认走同源 /api（开发环境由 Vite proxy 转发到后端）
  return envBase || '/api';
}

export const apiClient = axios.create({
  baseURL: resolveBaseURL(),
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers = config.headers || {};
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (resp) => resp,
  (error) => {
    // 统一处理：401/403 直接清理登录态，避免死循环
    const status = error?.response?.status;
    if (status === 401 || status === 403) {
      clearAuth();
    }

    const apiBody = error?.response?.data;
    const httpStatus = status;
    const message =
      apiBody?.message ||
      error?.message ||
      (httpStatus ? `请求失败（HTTP ${httpStatus}）` : '请求失败');

    return Promise.reject(
      new ApiError(message, {
        httpStatus,
        code: apiBody?.code,
        details: apiBody,
      })
    );
  }
);

/**
 * 将 ApiResponse<T> 解包为 T，并用 code 判断业务错误
 * @template T
 * @param {{ code: number, message: string, data: T }} apiResponse
 * @returns {T}
 */
export function unwrapApiResponse(apiResponse) {
  if (!apiResponse || typeof apiResponse.code !== 'number') {
    throw new ApiError('响应格式不正确', { details: apiResponse });
  }

  // 200/201 视为成功
  if (apiResponse.code >= 200 && apiResponse.code < 300) {
    return apiResponse.data;
  }

  throw new ApiError(apiResponse.message || '请求失败', {
    code: apiResponse.code,
    details: apiResponse,
  });
}
