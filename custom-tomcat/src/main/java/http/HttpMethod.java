package http;

public enum HttpMethod {
    GET,
    POST,
    UNSUPPORTED;

    public static HttpMethod from(String value) {
        for (HttpMethod method : values()) {
            if (method.name().equals(value)) {
                return method;
            }
        }

        return UNSUPPORTED; //과제에서 지원하지 않는 메서드는 405 응답으로 처리
    }
}
