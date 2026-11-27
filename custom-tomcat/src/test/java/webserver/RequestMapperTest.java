package webserver;

import db.MemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RequestMapperTest {
    private RequestMapper mapper;
    private Controller login;

    @BeforeEach
    void setUp() throws IOException {
        Path webRoot = Path.of("webapp");
        Path listPage = webRoot.resolve("user/list.html");
        login = new LoginController(MemoryUserRepository.getInstance());
        Controller list = new ListController(listPage);
        mapper = new RequestMapper(Map.of(
                new RequestKey(HttpMethod.POST, "/user/login"), login,
                new RequestKey(HttpMethod.GET, "/user/userList"), list,
                new RequestKey(HttpMethod.GET, "/user/list.html"), list
        ), new StaticFileController(webRoot, listPage));
    }

    @Test
    void findsLoginByMethodAndPathWithoutQueryString() {
        assertSame(login, mapper.findController(request(HttpMethod.POST, "/user/login")));
        assertSame(login, mapper.findController(request(HttpMethod.POST, "/user/login?source=form")));
        assertNull(mapper.findController(request(HttpMethod.GET, "/user/login")));
    }

    @Test
    void servesUnmappedCssFile() throws IOException {
        String response = dispatch("/css/styles.css?v=1");
        assertTrue(response.startsWith("HTTP/1.1 200 OK\r\n"));
        assertTrue(response.contains("Content-Type: text/css; charset=UTF-8\r\n"));
    }

    @Test
    void returnsNotFoundForUnknownPath() throws IOException {
        assertTrue(dispatch("/missing").startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    @Test
    void redirectsUnauthenticatedRequestsForBothListPaths() throws IOException {
        for (String path : new String[]{"/user/userList", "/user/list.html?source=menu"}) {
            String response = dispatch(path);
            assertTrue(response.startsWith("HTTP/1.1 302 Found\r\n"));
            assertTrue(response.contains("Location: /user/login.html\r\n"));
        }
    }

    @Test
    void staticFallbackDoesNotServeProtectedListAliases() throws IOException {
        assertTrue(dispatch("/user/./list.html").startsWith("HTTP/1.1 404 Not Found\r\n"));
    }

    private String dispatch(String path) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mapper.dispatch(request(HttpMethod.GET, path), new HttpResponse(output));
        return output.toString(StandardCharsets.UTF_8);
    }

    private HttpRequest request(HttpMethod method, String path) {
        return new HttpRequest(method, path, "HTTP/1.1", Map.of(), Map.of());
    }
}
