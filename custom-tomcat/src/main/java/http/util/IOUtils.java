package http.util;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.util.Objects;

public class IOUtils {

    //request body data 읽어옴.
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
