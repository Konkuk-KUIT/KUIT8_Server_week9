package http;

public enum HttpMethod {
    GET,
    POST;

    public static HttpMethod from(String value) {
        return switch (value) {
            case "GET" -> GET;
            case "POST" -> POST;
            default -> null;
        };
    }
}
