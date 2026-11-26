package webserver;

import http.HttpMethod;

public record RequestKey(HttpMethod method, String path) {
}
