package webserver;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestHandlerTest {

    @Test
    void 정상적인_로그인_쿠키를_인식한다() {
        String cookie = "theme=dark; logined=true; lang=ko";

        assertTrue(RequestHandler.hasLoginCookie(cookie));
    }

    @Test
    void 잘못된_로그인_쿠키는_거부한다() {
        assertFalse(RequestHandler.hasLoginCookie(null));
        assertFalse(RequestHandler.hasLoginCookie("theme=dark"));
        assertFalse(RequestHandler.hasLoginCookie("logined=false"));
        assertFalse(RequestHandler.hasLoginCookie("logined=true123"));
        assertFalse(RequestHandler.hasLoginCookie("not_logined=true"));
    }
}

