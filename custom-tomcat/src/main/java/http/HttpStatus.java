package http;

public enum HttpStatus {
    OK(200, "OK"),
    FOUND(302, "Found");

    private final int code;
    private final String reasonPhrase;

    HttpStatus(int code, String reasonPhrase) {
        this.code = code;
        this.reasonPhrase = reasonPhrase;
    }

    public String statusLine() {
        return "HTTP/1.1 " + code + " " + reasonPhrase + "\r\n";
    }
}
