package kuit8th.springmvc.controller;

import jakarta.servlet.http.Cookie;
import kuit8th.springmvc.db.MemoryUserRepository;
import kuit8th.springmvc.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

class UserControllerTest {
    private MemoryUserRepository repository;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        repository = new MemoryUserRepository();
        mvc = MockMvcBuilders.standaloneSetup(
                new UserController(repository), new UserApiController(repository)
        ).build();
    }

    @Test
    void signupStoresUserAndRedirects() throws Exception {
        var response = mvc.perform(post("/user/signup")
                .param("userId", "test").param("password", "secret")
                .param("name", "Tester").param("email", "test@example.com"))
                .andReturn().getResponse();
        assertEquals(302, response.getStatus());
        assertEquals("/index.html", response.getHeader("Location"));
        assertEquals(new User("test", "secret", "Tester", "test@example.com"), repository.findUserById("test"));
    }

    @Test
    void loginSetsRootCookieOnlyForMatchingPassword() throws Exception {
        repository.addUser(new User("test", "secret", "Tester", "test@example.com"));
        var success = mvc.perform(post("/user/login").param("userId", "test").param("password", "secret"))
                .andReturn().getResponse();
        assertEquals(302, success.getStatus());
        assertEquals("/index.html", success.getHeader("Location"));
        assertEquals("true", success.getCookie("logined").getValue());
        assertEquals("/", success.getCookie("logined").getPath());
        for (String userId : new String[]{"test", "unknown"}) {
            var failure = mvc.perform(post("/user/login").param("userId", userId).param("password", "wrong"))
                    .andReturn().getResponse();
            assertEquals(302, failure.getStatus());
            assertEquals("/user/login_failed.html", failure.getHeader("Location"));
            assertNull(failure.getCookie("logined"));
        }
    }

    @Test
    void listRejectsMissingAndInvalidLoginCookies() throws Exception {
        assertEquals(401, mvc.perform(get("/api/users")).andReturn().getResponse().getStatus());
        assertEquals(401, mvc.perform(get("/api/users").cookie(new Cookie("logined", "false")))
                .andReturn().getResponse().getStatus());
    }

    @Test
    void listReturnsOnlyNamesWithLoginCookie() throws Exception {
        repository.addUser(new User("test", "secret", "Tester", "test@example.com"));
        var response = mvc.perform(get("/api/users").cookie(new Cookie("logined", "true")))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("application/json"));
        assertEquals("[\"Tester\"]", response.getContentAsString());
    }

    @Test
    void restControllerWritesRedirectStringAsBody() throws Exception {
        MockMvc experiment = MockMvcBuilders.standaloneSetup(new RedirectExperimentController()).build();
        var response = experiment.perform(get("/redirect-experiment")).andReturn().getResponse();
        assertEquals(200, response.getStatus());
        assertNull(response.getHeader("Location"));
        assertEquals("redirect:/index.html", response.getContentAsString());
    }

    @RestController
    static class RedirectExperimentController {
        @GetMapping("/redirect-experiment")
        public String redirect() {
            return "redirect:/index.html";
        }
    }
}
