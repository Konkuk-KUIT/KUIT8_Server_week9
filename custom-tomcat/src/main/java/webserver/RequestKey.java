package webserver;

import http.HttpMethod;   // 직접 만든 enum

public record RequestKey(HttpMethod method, String path) {}
