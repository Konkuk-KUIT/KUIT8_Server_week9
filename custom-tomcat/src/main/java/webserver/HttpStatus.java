package webserver;

public enum HttpStatus {
    OK(200, "OK"),
    FOUND(302, "Found");

    private final int code;
    private final String reason;

    HttpStatus(int code, String reason) {
        this.code = code;
        this.reason = reason;
    }

    public String statusLine() {
        return "HTTP/1.1 " + code + " " + reason + "\r\n";
    }
}