package http.util;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpRequestUtilsTest {
    @Test
    void parsesOrdinaryFormFields() {
        assertEquals(Map.of("userId", "student", "password", "pass"),
                HttpRequestUtils.parseQueryParameter("userId=student&password=pass"));
    }

    @Test
    void decodesKoreanSpaceEmailAndLiteralPlus() {
        assertEquals(Map.of("name", "홍길동 Student", "email", "student+tag@example.com"),
                HttpRequestUtils.parseQueryParameter(
                        "name=%ED%99%8D%EA%B8%B8%EB%8F%99+Student&email=student%2Btag%40example.com"));
    }

    @Test
    void decodesParameterNames() {
        assertEquals(Map.of("userId", "student"),
                HttpRequestUtils.parseQueryParameter("user%49d=student"));
    }

    @Test
    void preservesEmptyValuesAndOtherFields() {
        assertEquals(Map.of("userId", "student", "name", "", "flag", ""),
                HttpRequestUtils.parseQueryParameter("userId=student&name=&flag"));
    }

    @Test
    void splitsBeforeDecodingAndKeepsEqualsInsideValues() {
        assertEquals(Map.of("password", "a=b&c=d"),
                HttpRequestUtils.parseQueryParameter("password=a=b%26c%3Dd"));
    }

    @Test
    void decodesOnlyOnce() {
        assertEquals(Map.of("value", "%26"),
                HttpRequestUtils.parseQueryParameter("value=%2526"));
    }

    @Test
    void acceptsNoParametersAndIgnoresEmptySegments() {
        assertTrue(HttpRequestUtils.parseQueryParameter(null).isEmpty());
        assertTrue(HttpRequestUtils.parseQueryParameter("").isEmpty());
        assertTrue(HttpRequestUtils.parseQueryParameter("&&").isEmpty());
        assertEquals(Map.of("name", "Student"),
                HttpRequestUtils.parseQueryParameter("&name=Student&&"));
    }

    @Test
    void rejectsDuplicateDecodedNames() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequestUtils.parseQueryParameter("userId=a&userId=b"));
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequestUtils.parseQueryParameter("userId=a&user%49d=b"));
    }

    @Test
    void rejectsEmptyNames() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequestUtils.parseQueryParameter("=value"));
    }

    @Test
    void rejectsMalformedPercentEscapesInsteadOfReturningAnEmptyMap() {
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequestUtils.parseQueryParameter("userId=student&name=%GG"));
        assertThrows(IllegalArgumentException.class,
                () -> HttpRequestUtils.parseQueryParameter("name=%"));
    }
}
