package webserver;

import http.constant.HttpMethod;

public record RequestKey(HttpMethod method, String path) {
}