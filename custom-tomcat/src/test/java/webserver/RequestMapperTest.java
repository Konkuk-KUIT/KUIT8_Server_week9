package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;
import http.constant.HttpMethod;
import http.request.HttpRequest;
import http.response.HttpResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private RequestMapper requestMapper;
    private LoginController loginController;

    @BeforeEach
    void setUp() {
        Repository repository = MemoryUserRepository.getInstance();
        loginController = new LoginController(repository);
        ListController listController = new ListController();
        requestMapper = new RequestMapper(Map.of(
                new RequestKey(HttpMethod.GET, "/"), new HomeController(),
                new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(repository),
                new RequestKey(HttpMethod.POST, "/user/login"), loginController,
                new RequestKey(HttpMethod.GET, "/user/userList"), listController,
                new RequestKey(HttpMethod.GET, "/user/list.html"), listController
        ), new StaticFileController());
    }

    private HttpRequest requestOf(String raw) throws IOException {
        return HttpRequest.from(new BufferedReader(new StringReader(raw)));
    }

    private String run(String raw) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        requestMapper.proceed(requestOf(raw), new HttpResponse(out));
        String all = out.toString(StandardCharsets.ISO_8859_1);
        return all.substring(0, all.indexOf("\r\n\r\n"));
    }

    @Test
    void 매핑에_있는_요청은_해당_컨트롤러를_찾는다() throws IOException {
        HttpRequest request = requestOf("POST /user/login HTTP/1.1\r\nContent-Length: 0\r\n\r\n");
        assertSame(loginController, requestMapper.findController(request));
    }

    @Test
    void 매핑에_없는_정적_파일은_파일로_응답한다() throws IOException {
        String head = run("GET /css/styles.css HTTP/1.1\r\n\r\n");
        assertTrue(head.startsWith("HTTP/1.1 200 OK"));
        assertTrue(head.contains("Content-Type: text/css"));
    }

    @Test
    void 어디에도_없는_경로는_404를_응답한다() throws IOException {
        String head = run("GET /nothing/here HTTP/1.1\r\n\r\n");
        assertTrue(head.startsWith("HTTP/1.1 404 Not Found"));
    }

    @Test
    void 쿠키없이_list_html에_가면_로그인_페이지로_302() throws IOException {
        String head = run("GET /user/list.html HTTP/1.1\r\n\r\n");
        assertTrue(head.startsWith("HTTP/1.1 302 Found"));
        assertTrue(head.contains("Location: /user/login.html"));
    }
}