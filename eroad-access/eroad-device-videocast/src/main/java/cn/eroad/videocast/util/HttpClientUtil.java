package cn.eroad.videocast.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class HttpClientUtil {
    private static final SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
    private static RestTemplate restTemplate;

    public HttpClientUtil() {
    }

    public static String getHttp(String url, String token) {
        ResponseEntity<String> result;
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "application/json");
        headers.add("Content-Encoding", "UTF-8");
        headers.add("Content-Type", "application/json; charset=UTF-8");
        if (token != null) {
            headers.add("Authorization", token);
        }
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        try {
            result = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        log.debug("URL IS {} ==========result is {}", url, result.getBody());
        return result.getBody();
    }

    static {
        requestFactory.setConnectTimeout(10000);
        requestFactory.setReadTimeout(5000);
        restTemplate = new RestTemplate(requestFactory);
    }

}

