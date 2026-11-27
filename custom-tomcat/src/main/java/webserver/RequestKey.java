package webserver;

import http.HttpMethod;

// 메서드와 경로가 모두 같으면 같은 키로 취급한다.
public record RequestKey(HttpMethod method, String path) {
}
