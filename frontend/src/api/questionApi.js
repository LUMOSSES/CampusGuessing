import { apiClient, unwrapApiResponse } from './apiClient';

/**
 * GET /questions?page=1&size=20&campus=&difficulty=
 * 注意：后端当前仅实现 page/size，campus/difficulty 作为可选参数传递（如后端未来支持筛选）。
 * @param {{ page?: number, size?: number, campus?: string, difficulty?: string }} [query]
 */
export async function getQuestionList(query = {}) {
  const page = query.page ?? 1;
  const size = query.size ?? 20;

  const params = { page, size };
  if (query.campus) params.campus = query.campus;
  if (query.difficulty) params.difficulty = query.difficulty;

  const resp = await apiClient.get('/questions', { params });
  return unwrapApiResponse(resp.data);
}

/**
 * GET /questions/{questionId}
 * @param {number|string} questionId
 */
export async function getQuestionDetail(questionId) {
  const resp = await apiClient.get(`/questions/${encodeURIComponent(questionId)}`);
  return unwrapApiResponse(resp.data);
}

/**
 * POST /users/{username}/questions
 * @param {string} username
 * @param {{
 *   campus: string,
 *   difficulty: string,
 *   key: string,
 *   correctCoord?: { lon?: number, lat?: number },
 *   title?: string,
 *   content?: string,
 *   answer?: string,
 * }} payload
 */
export async function createQuestion(username, payload) {
  const body = {
    campus: payload.campus,
    difficulty: payload.difficulty,
    key: payload.key, // 图片Key
    correctCoord: payload.correctCoord,
    title: payload.title,
    content: payload.content,
    answer: payload.answer,
  };

  const resp = await apiClient.post(`/users/${encodeURIComponent(username)}/questions`, body);
  return unwrapApiResponse(resp.data);
}
