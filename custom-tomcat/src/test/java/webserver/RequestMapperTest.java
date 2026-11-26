package webserver;

import controller.HomeController;
import controller.SignUpController;
import controller.LoginController;
import controller.ListController;
import controller.StaticFileController;

import db.MemoryUserRepository;
import db.Repository;

import http.HttpRequest;
import http.HttpResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class RequestMapperTest {

    private RequestMapper mapper;
    private LoginController loginController;

    @BeforeEach
    void setUp() {

        Repository repository = MemoryUserRepository.getInstance();

        loginController = new LoginController(repository);

        mapper = new RequestMapper(
                new HomeController(),
                new SignUpController(repository),
                loginController,
                new ListController(),
                new StaticFileController()
        );
    }

    @Test
    void 로그인_요청은_LoginController에_매핑된다() throws IOException {

        HttpRequest request = createRequest(
                "POST /user/login HTTP/1.1\r\n"
                        + "Content-Length: 0\r\n"
                        + "\r\n"
        );

        assertSame(loginController, mapper.findController(request));
    }

    @Test
    void 매핑에_없는_CSS_파일은_정적_파일로_응답한다()
            throws IOException {

        HttpRequest request = createRequest(
                "GET /css/styles.css HTTP/1.1\r\n\r\n"
        );

        assertNull(mapper.findController(request));

        String result = executeRequest(request);

        assertTrue(result.startsWith("HTTP/1.1 200 OK"));
        assertTrue(result.contains("Content-Type: text/css"));
    }

    @Test
    void 존재하지_않는_경로는_404를_응답한다()
            throws IOException {

        HttpRequest request = createRequest(
                "GET /unknown.html HTTP/1.1\r\n\r\n"
        );

        String result = executeRequest(request);

        assertTrue(result.startsWith("HTTP/1.1 404 Not Found"));
    }

    @Test
    void 로그인_쿠키_없이_목록에_접근하면_리다이렉트한다()
            throws IOException {

        HttpRequest request = createRequest(
                "GET /user/list.html HTTP/1.1\r\n\r\n"
        );

        String result = executeRequest(request);

        assertTrue(result.startsWith("HTTP/1.1 302 Found"));
        assertTrue(result.contains("Location: /user/login.html"));
    }

    @Test
    void 쿼리스트링을_제외한_경로로_컨트롤러를_찾는다()
            throws IOException {

        HttpRequest request = createRequest(
                "POST /user/login?from=main HTTP/1.1\r\n"
                        + "Content-Length: 0\r\n"
                        + "\r\n"
        );

        assertSame(loginController, mapper.findController(request));
    }

    // HTTP 요청 객체 생성
    private HttpRequest createRequest(String rawRequest)
            throws IOException {

        BufferedReader reader = new BufferedReader(
                new StringReader(rawRequest)
        );

        return HttpRequest.from(reader);
    }

    // 요청 실행 후 HTTP 응답 문자열 반환
    private String executeRequest(HttpRequest request)
            throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        HttpResponse response = new HttpResponse(out);

        mapper.dispatch(request, response);

        return out.toString(StandardCharsets.UTF_8);
    }
}
