package webserver;

public enum HttpMethod {
    GET,
    POST;

    public boolean matches(String method) {
        return name().equals(method);
    }
}