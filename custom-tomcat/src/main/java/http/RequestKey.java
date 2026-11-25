package http;

public record RequestKey(HttpMethod method, String path) {}
