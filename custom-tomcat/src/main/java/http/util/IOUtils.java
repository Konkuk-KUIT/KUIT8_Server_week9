package http.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

public class IOUtils {
    /**
     * Reads an ASCII application/x-www-form-urlencoded body before URL decoding.
     * In this restricted format each encoded character occupies one byte, so the
     * character count matches Content-Length. This is NOT a general HTTP body reader:
     * raw Unicode, JSON, multipart and chunked bodies are outside the mission scope.
     * Use the same reader used for the headers, positioned just after the empty line.
     *
     * @param br reader positioned at the first body character
     * @param contentLength non-negative Content-Length of the encoded body
     * @throws EOFException if the stream ends before the declared length
     * @throws IOException if the body contains a non-ASCII character
     */
    public static String readData(BufferedReader br, int contentLength) throws IOException {
        Objects.requireNonNull(br, "reader");
        if (contentLength < 0) {
            throw new IllegalArgumentException("Content-Length must not be negative");
        }
        char[] body = new char[contentLength];
        int offset = 0;
        while (offset < contentLength) {
            int read = br.read(body, offset, contentLength - offset);
            if (read == -1) {
                throw new EOFException("Request body ended before Content-Length bytes were read");
            }
            for (int i = offset; i < offset + read; i++) {
                if (body[i] > 0x7f) {
                    throw new IOException("Expected an ASCII URL-encoded form body");
                }
            }
            offset += read;
        }
        return new String(body);
    }
}
