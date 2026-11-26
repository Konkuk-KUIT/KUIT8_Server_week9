package http;

public enum HttpMethod {
    GET,
    POST,
    UNKNOWN;

    // 요청에 적힌 문자열을 Enum으로 바꿉니다.
    public static HttpMethod from(String value) {
        for (HttpMethod method : values()) {
            if (method.name().equals(value)) {
                return method;
            }
        }
        // 아직 지원하지 않는 메서드는 기존처럼 404 처리까지 넘어갑니다.
        return UNKNOWN;
    }
}
