package webserver;

public record RequestKey(HttpMethod method, String path) {
    static RequestKey from(HttpRequest request) {
        String path = request.path();
        int queryStart = path.indexOf('?');
        if (queryStart >= 0) {
            path = path.substring(0, queryStart);
        }
        return new RequestKey(request.method(), path);
    }
}
