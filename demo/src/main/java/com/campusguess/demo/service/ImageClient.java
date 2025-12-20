package com.campusguess.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class ImageClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${image.host.url:https://picui.cn/api/v1}")
    private String imageHostUrl;

    @Value("${image.host.token:Bearer 2083|KEIbu81Xhll4EprEN0pVGJb02R9dGNYlWKHYpsYr}")
    private String imageHostToken;

    /** 根据 key 查找图片信息 */
    @SuppressWarnings("unchecked")
    public Map<String, Object> fetchImageByKey(String key) {
        List<?> list = fetchImageList();
        if (list == null) return null;

        for (Object o : list) {
            if (o instanceof Map<?, ?> m && key.equals(String.valueOf(m.get("key")))) {
                log.info("在图床返回的列表中找到 key={}", key);
                return (Map<String, Object>) m;
            }
        }
        log.warn("图床返回的图片列表中未找到 key={}", key);
        return null;
    }


    /** 提取图片列表 */
    private List<?> fetchImageList() {
        Map<String, Object> body = doRequest();
        if (body == null) return null;

        // 校验 status
        Object statusObj = body.get("status");
        boolean status = statusObj instanceof Boolean b ? b : Boolean.parseBoolean(String.valueOf(statusObj));
        if (!status) {
            log.warn("图床返回 status=false, message={}", body.get("message"));
            return null;
        }

        // 支持两层嵌套：data 或 data.data
        Object data = body.get("data");
        if (data instanceof Map<?, ?> m && m.get("data") != null) {
            data = m.get("data");
        }
        if (!(data instanceof List<?> list)) {
            log.warn("图床返回的 data 不是图片列表");
            return null;
        }
        return list;
    }

    /** 公共 HTTP 请求方法 */
    private Map<String, Object> doRequest() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", imageHostToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        try {
            ResponseEntity<Map<String, Object>> resp = restTemplate.exchange(
                    imageHostUrl + "/images", HttpMethod.GET, new HttpEntity<>(headers), new ParameterizedTypeReference<Map<String, Object>>() {});
            return resp.getBody();
        } catch (Exception e) {
            log.warn("调用图床出错: {}", e.getMessage());
            return null;
        }
    }
}
