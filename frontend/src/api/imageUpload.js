/**
 * 图片上传工具函数
 * 使用 PICUI 图床服务
 */

// PICUI 配置（需要在环境变量或配置文件中设置）
const PICUI_API_BASE = 'https://picui.cn/api/v1';
const PICUI_TOKEN = 'Bearer 2083|KEIbu81Xhll4EprEN0pVGJb02R9dGNYlWKHYpsYr'; // 需要替换为实际的Bearer Token

/**
 * 生成临时上传Token
 * @returns {Promise<string>} 临时上传token
 */
async function generateUploadToken() {
  try {
    const response = await fetch(`${PICUI_API_BASE}/images/tokens`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${PICUI_TOKEN}`,
        'Content-Type': 'application/json',
        'Accept': 'application/json',
      },
      body: JSON.stringify({
        num: 1,
        seconds: 3600, // 1小时有效期
      }),
    });

    if (!response.ok) {
      throw new Error(`生成上传Token失败，状态码: ${response.status}`);
    }

    const result = await response.json();
    
    if (result.status && result.data?.tokens?.length > 0) {
      return result.data.tokens[0].token;
    } else {
      throw new Error(result.message || '生成上传Token失败');
    }
  } catch (error) {
    console.error('生成Token出错:', error);
    throw new Error('无法生成上传Token，请检查配置');
  }
}

/**
 * 上传图片到 PICUI 图床
 * @param {File} file - 图片文件对象
 * @returns {Promise<string>} 图片URL
 * @throws {Error} 上传失败时抛出错误
 */
export async function uploadImage(file) {
  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    throw new Error('请选择图片文件');
  }

  // 验证文件大小（限制为 10MB）
  const maxSize = 10 * 1024 * 1024; // 10MB
  if (file.size > maxSize) {
    throw new Error('图片大小不能超过 10MB');
  }

  try {
    console.log('开始上传图片到 PICUI...');
    
    // 第一步：生成临时上传Token
    console.log('正在生成上传Token...');
    const uploadToken = await generateUploadToken();
    console.log('Token生成成功');

    // 第二步：上传图片
    const formData = new FormData();
    formData.append('file', file);
    formData.append('token', uploadToken);
    formData.append('permission', '1'); // 1=公开

    const response = await fetch(`${PICUI_API_BASE}/upload`, {
      method: 'POST',
      headers: {
        'Accept': 'application/json',
      },
      body: formData,
    });

    console.log('收到响应，状态码:', response.status);
    
    if (!response.ok) {
      throw new Error(`上传失败，HTTP 状态码: ${response.status}`);
    }

    const result = await response.json();
    console.log('API 响应数据:', result);

    // PICUI API 响应格式 - 返回包含key和url的对象
    if (result.status && result.data?.key) {
      const imageKey = result.data.key;
      const imageUrl = result.data?.links?.url || '';
      console.log('上传成功，图片Key:', imageKey);
      console.log('图片URL:', imageUrl);
      return { key: imageKey, url: imageUrl }; // 返回 key 和 url
    } else {
      throw new Error(result.message || '上传失败，未返回图片Key');
    }
  } catch (error) {
    console.error('上传过程出错:', error);
    throw new Error(error.message || '图片上传失败，请检查网络连接');
  }
}
