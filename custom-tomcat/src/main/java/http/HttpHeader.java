package http;

public enum HttpHeader {
    CONTENT_TYPE("Content-Type"),
    CONTENT_LENGTH("Content-Length"),
    LOCATION("Location"),
    SET_COOKIE("Set-Cookie"),
    COOKIE("Cookie"),
    CONNECTION("Connection");

    private final String value;

    HttpHeader(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public String line(Object headerValue) {   // "Content-Type: text/css\r\n"
        return value + ": " + headerValue + "\r\n";
    }
}