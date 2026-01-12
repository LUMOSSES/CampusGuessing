/**
 * 统一管理登录态（JWT Token + 用户信息）
 * 约定：后端返回结构为 ApiResponse<LoginResponse>
 */

const TOKEN_KEY = 'campusguess.token';
const AUTH_KEY = 'campusguess.auth';

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) || '';
}

export function setToken(token) {
  if (!token) {
    localStorage.removeItem(TOKEN_KEY);
    return;
  }
  localStorage.setItem(TOKEN_KEY, token);
}

export function clearAuth() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(AUTH_KEY);
}

export function setAuth(auth) {
  // auth: { token, expireTime, userInfo }
  if (!auth?.token) {
    clearAuth();
    return;
  }
  // 兼容旧结构：为昵称提供默认值
  const normalized = {
    ...auth,
    displayName: auth.displayName || auth?.userInfo?.username || '',
  };

  setToken(normalized.token);
  localStorage.setItem(AUTH_KEY, JSON.stringify(normalized));
}

export function getAuth() {
  try {
    const raw = localStorage.getItem(AUTH_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
}

export function getCurrentUserInfo() {
  const auth = getAuth();
  return auth?.userInfo ?? null;
}

// 昵称（仅前端显示用，不影响登录 username）
export function getDisplayName() {
  const auth = getAuth();
  return auth?.displayName || auth?.userInfo?.username || '';
}

export function setDisplayName(displayName) {
  const auth = getAuth();
  if (!auth) return;
  const next = { ...auth, displayName: displayName || auth?.userInfo?.username || '' };
  localStorage.setItem(AUTH_KEY, JSON.stringify(next));
}
