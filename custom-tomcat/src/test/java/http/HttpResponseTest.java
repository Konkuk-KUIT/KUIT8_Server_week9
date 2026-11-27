package http;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import support.HttpTestResponse;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class HttpResponseTest {
    @ParameterizedTest
    @ValueSource(strings = {"/", "/index.html", "/user/form.html", "/user/login.html",
            "/user/login_failed.html", "/qna/form.html", "/qna/show.html"})
    void forwardsTheRequestedHtmlWithByteBasedLength(String path) throws Exception {
        String relativePath = "/".equals(path) ? "index.html" : path.substring(1);
        byte[] expected = Files.readAllBytes(Path.of("webapp", relativePath));
        HttpTestResponse response = forward(path);

        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals("text/html;charset=utf-8", response.header("Content-Type"));
        assertEquals(Integer.toString(expected.length), response.header("Content-Length"));
        assertEquals("close", response.header("Connection"));
        assertArrayEquals(expected, response.body());
        assertNull(response.header("Location"));
        assertNull(response.header("Set-Cookie"));
    }

    @Test
    void forwardsCssWithItsOwnContentType() throws Exception {
        byte[] expected = Files.readAllBytes(Path.of("webapp", "css", "styles.css"));
        HttpTestResponse response = forward("/css/styles.css");

        assertEquals("HTTP/1.1 200 OK", response.statusLine());
        assertEquals("text/css", response.header("Content-Type"));
        assertEquals(Integer.toString(expected.length), response.header("Content-Length"));
        assertArrayEquals(expected, response.body());
    }

    @Test
    void redirectsWithAUrlAndAnEmptyBody() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).redirect("/index.html");
        HttpTestResponse response = HttpTestResponse.parse(output.toByteArray());

        assertEquals("HTTP/1.1 302 Found", response.statusLine());
        assertEquals("/index.html", response.header("Location"));
        assertEquals("0", response.header("Content-Length"));
        assertEquals("close", response.header("Connection"));
        assertEquals(0, response.body().length);
        assertNull(response.header("Set-Cookie"));
    }

    @Test
    void sendsLoginCookieAndLocationInTheSameRedirect() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).redirect("/index.html", "logined=true; Path=/");
        HttpTestResponse response = HttpTestResponse.parse(output.toByteArray());

        assertEquals("HTTP/1.1 302 Found", response.statusLine());
        assertEquals("/index.html", response.header("Location"));
        assertEquals("logined=true; Path=/", response.header("Set-Cookie"));
        assertEquals("0", response.header("Content-Length"));
        assertEquals(0, response.body().length);
    }

    @ParameterizedTest
    @ValueSource(strings = {"/missing.html", "/css/missing.css", "/../webapp/index.html"})
    void rejectsMissingAndParentPaths(String path) throws Exception {
        assertNotFound(forward(path));
    }

    @Test
    void doesNotReadAnExistingFileOutsideWebapp(@TempDir Path temporaryDirectory) throws Exception {
        Path outside = temporaryDirectory.resolve("outside.html");
        Files.writeString(outside, "This file must not be served.");
        Path webRoot = Path.of("webapp").toAbsolutePath().normalize();
        String relative = webRoot.relativize(outside.toAbsolutePath()).toString().replace('\\', '/');

        assertNotFound(forward("/" + relative));
    }

    private void assertNotFound(HttpTestResponse response) {
        assertEquals("HTTP/1.1 404 Not Found", response.statusLine());
        assertEquals("Not Found", new String(response.body(), StandardCharsets.UTF_8));
        assertEquals(Integer.toString(response.body().length), response.header("Content-Length"));
    }

    private HttpTestResponse forward(String path) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        new HttpResponse(output).forward(path);
        return HttpTestResponse.parse(output.toByteArray());
    }
}
