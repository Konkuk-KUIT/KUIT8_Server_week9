package http.util;

import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class IOUtilsTest {
    @Test
    void readsExactlyTheDeclaredLength() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("abcdefNEXT"));
        assertEquals("abcdef", IOUtils.readData(reader, 6));
        assertEquals('N', reader.read());
    }

    @Test
    void collectsFragmentedInput() throws IOException {
        StringReader chunks = new StringReader("abcdef") {
            @Override
            public int read(char[] buffer, int offset, int length) throws IOException {
                return super.read(buffer, offset, Math.min(2, length));
            }

            @Override
            public boolean ready() {
                return false;
            }
        };
        assertEquals("abcdef", IOUtils.readData(new BufferedReader(chunks), 6));
    }

    @Test
    void rejectsAnIncompleteBody() {
        BufferedReader reader = new BufferedReader(new StringReader("ab"));
        assertThrows(EOFException.class, () -> IOUtils.readData(reader, 6));
    }

    @Test
    void allowsAnEmptyBodyWithoutConsumingInput() throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader("NEXT"));
        assertEquals("", IOUtils.readData(reader, 0));
        assertEquals('N', reader.read());
    }

    @Test
    void rejectsNegativeLengths() {
        BufferedReader reader = new BufferedReader(new StringReader(""));
        assertThrows(IllegalArgumentException.class, () -> IOUtils.readData(reader, -1));
    }

    @Test
    void readsAndDecodesAnEncodedKoreanFormBody() throws IOException {
        String encoded = "name=%ED%99%8D%EA%B8%B8%EB%8F%99&email=student%40example.com";
        BufferedReader reader = new BufferedReader(new StringReader(encoded));
        String body = IOUtils.readData(reader, encoded.getBytes(StandardCharsets.UTF_8).length);
        assertEquals(encoded, body);
        assertEquals("홍길동", HttpRequestUtils.parseQueryParameter(body).get("name"));
        assertEquals("student@example.com", HttpRequestUtils.parseQueryParameter(body).get("email"));
    }

    @Test
    void readsFromTheSameReaderAfterHeaders() throws IOException {
        String body = "userId=student&password=pass";
        String request = "POST /user/signup HTTP/1.1\r\n"
                + "Content-Length: " + body.length() + "\r\n\r\n" + body;
        BufferedReader reader = new BufferedReader(new StringReader(request));
        assertEquals("POST /user/signup HTTP/1.1", reader.readLine());
        reader.readLine();
        assertEquals("", reader.readLine());
        assertEquals(body, IOUtils.readData(reader, body.length()));
    }

    @Test
    void rejectsRawUnicodeOutsideTheSupportedBodyFormat() {
        BufferedReader reader = new BufferedReader(new StringReader("한글"));
        assertThrows(IOException.class, () -> IOUtils.readData(reader, 6));
    }
}
