package http;

public enum ContentType {
    HTML("html", "text/html;charset=utf-8"),
    CSS("css", "text/css;charset=utf-8"),
    DEFAULT("", "application/octet-stream");   // 추가

    private final String extension;
    private final String mimeType;

    ContentType(String extension, String mimeType) {
        this.extension = extension;
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    // 추가
    public static ContentType fromUrl(String url) {
        for (ContentType type : values()) {
            if (!type.extension.isEmpty() && url.endsWith("." + type.extension)) {
                return type;
            }
        }
        return DEFAULT;
    }
}