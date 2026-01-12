import { apiClient, unwrapApiResponse } from './apiClient';

/**
 * POST /records
 * 提交游戏对战记录
 * @param {{ userId: number, gameType: string, questionRecords: Array }} payload
 */
export async function submitGameRecord(payload) {
  // 对应后端接口: POST /api/records
  // apiClient 通常配置了 baseURL (如 /api)，所以这里直接写 '/records'
  const resp = await apiClient.post('/records', payload);
  return unwrapApiResponse(resp.data);
}