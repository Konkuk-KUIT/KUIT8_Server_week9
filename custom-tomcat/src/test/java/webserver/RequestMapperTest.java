package webserver;

import controller.ListController;
import controller.LoginController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import static http.HttpMethod.*;
import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {
    private final RequestMapper mapper = new RequestMapper(Map.of(
            new RequestKey(POST, "/user/login"), new LoginController(MemoryUserRepository.getInstance()),
            new RequestKey(GET, "/user/list.html"), new ListController()), new StaticFileController());

    private HttpRequest request(String line) throws IOException {
        return HttpRequest.from(new BufferedReader(new StringReader(line + "\r\n\r\n")));
    }

    private String response(String line) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        mapper.handle(request(line), new HttpResponse(out));
        return out.toString();
    }

    @Test
    void 매핑된_요청은_해당_컨트롤러가_선택된다() throws IOException {
        assertInstanceOf(LoginController.class, mapper.find(request("POST /user/login HTTP/1.1")));
    }

    @Test
    void 매핑에_없는_정적_파일은_파일이_응답된다() throws IOException {
        String res = response("GET /css/styles.css HTTP/1.1");
        assertTrue(res.startsWith("HTTP/1.1 200 OK"));
        assertTrue(res.contains("Content-Type: text/css"));
    }

    @Test
    void 없는_경로는_404() throws IOException {
        assertTrue(response("GET /nothing HTTP/1.1").startsWith("HTTP/1.1 404 Not Found"));
    }

    @Test
    void 쿠키_없이_목록을_요청하면_로그인_화면으로_302() throws IOException {
        String res = response("GET /user/list.html HTTP/1.1");
        assertTrue(res.startsWith("HTTP/1.1 302 Found"));
        assertTrue(res.contains("Location: /user/login.html"));
    }
}