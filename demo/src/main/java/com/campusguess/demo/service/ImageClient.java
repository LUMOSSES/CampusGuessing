package com.campusguess.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${image.host.url:https:picui.cn/api/v1}")
    private String imageHostUrl;

    @Value("${image.host.token:Bearer 2083|KEIbu81Xhll4EprEN0pVGJb02R9dGNYlWKHYpsYr}")
    private String imageHostToken;

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> fetchImageByKey(String key) {
        String url = imageHostUrl + "/images";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", imageHostToken);
        headers.add("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp;
        try {
            resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (Exception e) {
            log.warn("调用图床 {} 出错: {}", url, e.getMessage());
            return null;
        }

        Map<String, Object> body = resp.getBody();
        if (body == null) return null;

        Object statusObj = body.get("status");
        boolean status = false;
        if (statusObj instanceof Boolean) status = (Boolean) statusObj;
        else if (statusObj instanceof String) status = Boolean.parseBoolean((String) statusObj);

        if (!status) {
            Object msg = body.get("message");
            log.warn("图床返回 status=false, message={}", msg);
            return null;
        }

        Object data = body.get("data");
        // 支持两层嵌套：data 或 data.data
        Object listObj = data;
        if (data instanceof Map) {
            Object inner = ((Map<?, ?>) data).get("data");
            if (inner != null) {
                listObj = inner;
            }
        }

        if (!(listObj instanceof List)) {
            log.warn("图床返回的 data 不是图片列表，type={}", listObj == null ? null : listObj.getClass().getName());
            return null;
        }

        List<?> list = (List<?>) listObj;
        for (Object o : list) {
            if (o instanceof Map) {
                Map<?, ?> m = (Map<?, ?>) o;
                Object k = m.get("key");
                if (k != null && k.toString().equals(key)) {
                    log.info("在图床返回的列表中找到 key={}", key);
                    // safe cast
                    return (Map<String, Object>) m;
                }
            }
        }
        log.warn("图床返回的图片列表中未找到 key={}", key);
        return null;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, Object> fetchAllImages() {
        String url = imageHostUrl + "/images";

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", imageHostToken);
        headers.add("Accept", "application/json");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> resp;
        try {
            resp = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        } catch (Exception e) {
            log.warn("调用图床 {} 出错: {}", url, e.getMessage());
            return null;
        }

        Map<String, Object> body = resp.getBody();
        return body;
    }
}
