package webserver;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import static org.assertj.core.api.Assertions.assertThat;

class HttpRequestTest {
    private BufferedReader parseRequest(String message) {
        return new BufferedReader(new StringReader(message));
    }

    @Test
    void 읽어온_HTTPMethod가_정확한지() throws IOException {
        //given
        String raw = "GET /index.html HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "\r\n";
        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.getHttpMethod()).isEqualTo(HttpMethod.GET);
    }

    @Test
    void 쿼리가_존재할때_파싱이_잘되는지_path확인() throws IOException{
        //given
        String raw = "GET /index.html?id=1234&pw=2222 HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "\r\n";
        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.getPath()).isEqualTo("/index.html");
    }

    @Test
    void 쿼리가_존재할때_파싱이_잘되는지_qeury확인() throws IOException{
        String raw = "GET /index.html?id=1234&pw=2222 HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "\r\n";
        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.getQueryParameter())
                .hasSize(2)
                .containsEntry("id", "1234")
                .containsEntry("pw", "2222");
    }

    @Test
    void content_length_데이터_파싱_잘되는지_확인() throws IOException{
        //given
        String raw = "POST /index.html?id=1234&pw=2222 HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "Content-Length: 15\r\n" +
                "Cookie: true\r\n" +
                "\r\n"+
                "id=1234&pw=2222\r\n";
        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.getContentLength()).isEqualTo(15);
    }

    @Test
    void isLogin_데이터_파싱_잘되는지_확인() throws IOException{
        //given
        String raw = "GET /index.html?id=1234&pw=2222 HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "Content-Length: 0\r\n" +
                "Cookie: true\r\n" +
                "\r\n";
        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.isLogin()).isEqualTo(true);
    }


    @Test
    void body_데이터_파싱_잘되는지_확인() throws IOException{
        //given
        String raw = "POST /index.html?id=1234&pw=2222 HTTP/1.1\r\n"+
                "Host: localhost:8080\r\n" +
                "Content-Length: 15\r\n" +
                "Cookie: true\r\n" +
                "\r\n"+
                "id=1234&pw=2222\r\n";

        BufferedReader br = parseRequest(raw);

        //when
        HttpRequest request = HttpRequest.from(br);

        //then
        assertThat(request.getRequestBody()).isEqualTo("id=1234&pw=2222");
    }

}