package webserver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class HttpResponseTest {

    //File mock 객체를 만들기 위한 annotation
    @TempDir
    Path tempdir;

    @Test
    void 파일이_존재할때_200응답() throws IOException {
        Path file = tempdir.resolve("index.html");
        Files.writeString(file, "<h1>hello</h2>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        HttpResponse httpResponse = new HttpResponse(dos);

        httpResponse.serveResource(file.toString());

        String output = out.toString();
        assertThat(output).contains("HTTP/1.1 200 OK");
    }

    @Test
    void 파일이_존재하지_않을때_404응답() throws IOException {
        Path file = tempdir.resolve("index.html");
        Files.writeString(file, "<h1>hello</h2>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        HttpResponse httpResponse = new HttpResponse(dos);

        httpResponse.serveResource("/index.txt");

        String output = out.toString();
        assertThat(output).contains("HTTP/1.1 404 NotFound");
    }

    @Test
    void ridect_302응답() throws IOException {
        Path file = tempdir.resolve("index.html");
        Files.writeString(file, "<h1>hello</h2>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(out);

        HttpResponse httpResponse = new HttpResponse(dos);

        httpResponse.redirect(file.toString());

        String output = out.toString();
        assertThat(output).contains("HTTP/1.1 302 OK");
    }
}