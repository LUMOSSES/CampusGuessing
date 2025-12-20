import { apiClient, unwrapApiResponse } from './apiClient';

/**
 * POST /users/{username}/friends
 * @param {string} username
 * @param {{ friendUsername: string }} payload
 */
export async function addFriend(username, payload) {
  const resp = await apiClient.post(
    `/users/${encodeURIComponent(username)}/friends`,
    { friendUsername: payload.friendUsername }
  );
  return unwrapApiResponse(resp.data);
}

/**
 * PUT /users/{username}/friends/applications
 * @param {string} username
 * @param {{ friendUsername: string, handleType: 'accept'|'reject' }} payload
 */
export async function handleFriendApplication(username, payload) {
  const resp = await apiClient.put(
    `/users/${encodeURIComponent(username)}/friends/applications`,
    {
      friendUsername: payload.friendUsername,
      handleType: payload.handleType,
    }
  );
  return unwrapApiResponse(resp.data);
}

/**
 * GET /users/{username}/friends?page=1&size=20
 * @param {string} username
 * @param {{ page?: number, size?: number }} [query]
 */
export async function getFriendList(username, query = {}) {
  const page = query.page ?? 1;
  const size = query.size ?? 20;

  const resp = await apiClient.get(`/users/${encodeURIComponent(username)}/friends`, {
    params: { page, size },
  });
  return unwrapApiResponse(resp.data);
}

/**
 * GET /users/{username}/friends/pending?page=1&size=20
 * @param {string} username
 * @param {{ page?: number, size?: number }} [query]
 */
export async function getPendingFriendRequests(username, query = {}) {
  const page = query.page ?? 1;
  const size = query.size ?? 20;

  const resp = await apiClient.get(
    `/users/${encodeURIComponent(username)}/friends/pending`,
    { params: { page, size } }
  );
  return unwrapApiResponse(resp.data);
}

/**
 * GET /users/{username}/friends/sent?page=1&size=20
 * @param {string} username
 * @param {{ page?: number, size?: number }} [query]
 */
export async function getSentFriendRequests(username, query = {}) {
  const page = query.page ?? 1;
  const size = query.size ?? 20;

  const resp = await apiClient.get(`/users/${encodeURIComponent(username)}/friends/sent`, {
    params: { page, size },
  });
  return unwrapApiResponse(resp.data);
}

/**
 * GET /users/{username}/friends/count
 * @param {string} username
 */
export async function getFriendCount(username) {
  const resp = await apiClient.get(`/users/${encodeURIComponent(username)}/friends/count`);
  return unwrapApiResponse(resp.data);
}
