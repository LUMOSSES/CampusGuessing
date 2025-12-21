import { getCurrentUserInfo } from './authStorage';

// 假设你的后端基础路径是 /api
const BASE_URL = 'http://localhost:8080/api'; 

export async function submitGameRecord(payload) {
  const token = localStorage.getItem('token'); // 假设你有 token 存储
  
  const response = await fetch(`${BASE_URL}/records`, { // 请根据实际后端接口路径修改 '/records'
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': token ? `Bearer ${token}` : '',
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    throw new Error('上传记录失败');
  }

  return await response.json();
}