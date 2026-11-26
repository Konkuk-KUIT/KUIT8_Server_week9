//포장 종류 정의

package http.constant;

public enum ContentType {
    HTML(".html", "text/html;charset=utf-8"),
    CSS(".css", "text/css"),
    JS(".js", "application/javascript");

    private final String extension;
    private final String value;

    ContentType(String extension, String value) {
        this.extension = extension;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    // "/css/styles.css" → CSS, 모르는 확장자는 HTML로 취급
    public static ContentType from(String path) {
        for (ContentType type : values()) {
            if (path.endsWith(type.extension)) {
                return type;
            }
        }
        return HTML;
    }
}
