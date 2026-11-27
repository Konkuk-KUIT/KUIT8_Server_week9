package http;

import db.MemoryUserRepository;
import model.User;
import org.junit.jupiter.api.Test;
import webserver.RequestHandler;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HttpIntegrationTest {
    @Test
    void parsesGetMethodPathQueryAndCookies() throws Exception {
        HttpRequest request = parse("GET /user/list.html?name=Kim+Lee HTTP/1.1\r\n"
                + "cOoKiE: theme=dark; logined=true\r\n\r\n");
        assertEquals(HttpMethod.GET, request.getMethod());
        assertEquals("/user/list.html", request.getPath());
        assertEquals("Kim Lee", request.getParameter("name"));
        assertEquals("true", request.getCookie("logined"));
        assertNull(request.getCookie("missing"));
    }

    @Test
    void parsesPostBodyUsingContentLengthHeaderCaseInsensitively() throws Exception {
        String body = "userId=student&name=%ED%99%8D%EA%B8%B8%EB%8F%99&password=a%3Db";
        HttpRequest request = parse("POST /user/signup HTTP/1.1\r\n"
                + "cOnTeNt-LeNgTh: " + body.getBytes(StandardCharsets.UTF_8).length
                + "\r\nCookie: logined=false\r\n\r\n" + body);
        assertEquals(HttpMethod.POST, request.getMethod());
        assertEquals("/user/signup", request.getPath());
        assertEquals("student", request.getParameter("userId"));
        assertEquals("홍길동", request.getParameter("name"));
        assertEquals("a=b", request.getParameter("password"));
        assertEquals("false", request.getCookie("logined"));
    }

    @Test
    void successfulLoginRedirectsAndSetsCookie() throws Exception {
        String id = "test-" + UUID.randomUUID();
        MemoryUserRepository.getInstance().addUser(new User(id, "correct", "Name", "a@example.com"));
        Response response = send(post("/user/login", "userId=" + id + "&password=correct"));
        assertTrue(response.status.startsWith("HTTP/1.1 302"));
        assertEquals("/index.html", response.header("Location"));
        assertEquals("logined=true; Path=/", response.header("Set-Cookie"));
        assertEquals("0", response.header("Content-Length"));
        assertEquals(0, response.body.length);
    }

    @Test
    void wrongPasswordAndMissingUserRedirectToFailure() throws Exception {
        String id = "test-" + UUID.randomUUID();
        MemoryUserRepository.getInstance().addUser(new User(id, "correct", "Name", "a@example.com"));
        for (String data : new String[] { "userId=" + id + "&password=wrong", "userId=missing-" + id + "&password=wrong" }) {
            Response response = send(post("/user/login", data));
            assertTrue(response.status.startsWith("HTTP/1.1 302"));
            assertEquals("/user/login_failed.html", response.header("Location"));
            assertNull(response.header("Set-Cookie"));
        }
    }

    @Test
    void listRoutesRequireCookieAndPermitLoggedInRequests() throws Exception {
        for (String route : new String[] { "/user/userList", "/user/list.html" }) {
            for (String header : new String[] { "", "Cookie: theme=dark; logined=false\r\n" }) {
                Response denied = send("GET " + route + " HTTP/1.1\r\n" + header + "\r\n");
                assertTrue(denied.status.startsWith("HTTP/1.1 302"));
                assertEquals("/user/login.html", denied.header("Location"));
            }
            Response allowed = send("GET " + route + " HTTP/1.1\r\nCookie: theme=dark; logined=true\r\n\r\n");
            assertFileResponse(allowed, "user/list.html", "text/html;charset=utf-8");
        }
    }

    @Test
    void returnsHtmlAndCssWithExactBytesAndLength() throws Exception {
        assertFileResponse(send("GET /index.html HTTP/1.1\r\n\r\n"), "index.html", "text/html;charset=utf-8");
        assertFileResponse(send("GET /css/styles.css HTTP/1.1\r\n\r\n"), "css/styles.css", "text/css");
    }

    @Test
    void postSignupStoresUserAndRedirects() throws Exception {
        String id = "test-" + UUID.randomUUID();
        Response response = send(post("/user/signup", "userId=" + id + "&password=secret&name=Kim&email=a%40example.com"));
        assertTrue(response.status.startsWith("HTTP/1.1 302"));
        assertEquals("/index.html", response.header("Location"));
        assertEquals("secret", MemoryUserRepository.getInstance().findUserById(id).getPassword());
    }

    private static HttpRequest parse(String text) throws IOException {
        return HttpRequest.from(new BufferedReader(new StringReader(text)));
    }

    private static String post(String path, String body) {
        return "POST " + path + " HTTP/1.1\r\nContent-Type: application/x-www-form-urlencoded\r\nContent-Length: "
                + body.getBytes(StandardCharsets.UTF_8).length + "\r\n\r\n" + body;
    }

    private static void assertFileResponse(Response response, String path, String contentType) throws IOException {
        byte[] expected = Files.readAllBytes(Path.of("webapp", path));
        assertTrue(response.status.startsWith("HTTP/1.1 200"));
        assertEquals(contentType, response.header("Content-Type"));
        assertEquals(Integer.toString(expected.length), response.header("Content-Length"));
        assertEquals(response.body.length, Integer.parseInt(response.header("Content-Length")));
        assertArrayEquals(expected, response.body);
    }

    private static Response send(String request) throws Exception {
        TestSocket socket = new TestSocket(request.getBytes(StandardCharsets.UTF_8));
        new RequestHandler(socket).run();
        return new Response(socket.output.toByteArray());
    }

    private static class TestSocket extends Socket {
        private final ByteArrayInputStream input;
        private final ByteArrayOutputStream output = new ByteArrayOutputStream();
        TestSocket(byte[] request) { input = new ByteArrayInputStream(request); }
        @Override public InputStream getInputStream() { return input; }
        @Override public OutputStream getOutputStream() { return output; }
        @Override public InetAddress getInetAddress() { return InetAddress.getLoopbackAddress(); }
        @Override public int getPort() { return 12345; }
    }

    private static class Response {
        final String status;
        final String[] headers;
        final byte[] body;
        Response(byte[] bytes) {
            byte[] separator = "\r\n\r\n".getBytes(StandardCharsets.US_ASCII);
            int index = -1;
            for (int i = 0; i <= bytes.length - separator.length; i++) {
                if (bytes[i] == separator[0] && bytes[i + 1] == separator[1]
                        && bytes[i + 2] == separator[2] && bytes[i + 3] == separator[3]) {
                    index = i;
                    break;
                }
            }
            assertTrue(index >= 0, "HTTP response must contain header/body separator");
            String[] lines = new String(bytes, 0, index, StandardCharsets.ISO_8859_1).split("\r\n");
            status = lines[0];
            headers = Arrays.copyOfRange(lines, 1, lines.length);
            body = Arrays.copyOfRange(bytes, index + separator.length, bytes.length);
        }
        String header(String name) {
            for (String line : headers) {
                int colon = line.indexOf(':');
                if (colon > 0 && line.substring(0, colon).toLowerCase(Locale.ROOT).equals(name.toLowerCase(Locale.ROOT))) {
                    return line.substring(colon + 1).trim();
                }
            }
            return null;
        }
    }
}