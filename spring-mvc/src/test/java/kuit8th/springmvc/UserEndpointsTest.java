package kuit8th.springmvc;

import jakarta.servlet.http.Cookie;
import kuit8th.springmvc.model.User;
import kuit8th.springmvc.repository.MemoryUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserEndpointsTest {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MemoryUserRepository userRepository;

    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void signupThenLoginThenAccessListWithReturnedCookie() throws Exception {
        String id = "flow-" + UUID.randomUUID();
        String name = "이름-" + id;
        mvc.perform(post("/user/signup").param("userId", id).param("password", "secret")
                        .param("name", name).param("email", "flow@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"));

        Cookie loginCookie = mvc.perform(post("/user/login").param("userId", id).param("password", "secret"))
                .andExpect(status().isFound())
                .andExpect(cookie().path("logined", "/"))
                .andReturn().getResponse().getCookie("logined");

        mvc.perform(get("/api/users").cookie(loginCookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasItem(name)));
    }

    @Test
    void signupStoresFormFieldsAndRedirects() throws Exception {
        String id = "signup-" + UUID.randomUUID();
        mvc.perform(post("/user/signup")
                        .param("userId", id)
                        .param("password", "secret")
                        .param("name", "홍길동")
                        .param("email", "hong@example.com"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"));

        User user = userRepository.findUserById(id);
        assertEquals("secret", user.getPassword());
        assertEquals("홍길동", user.getName());
        assertEquals("hong@example.com", user.getEmail());
    }

    @Test
    void successfulLoginSetsRootCookieAndRedirects() throws Exception {
        User user = addUser();
        mvc.perform(post("/user/login")
                        .param("userId", user.getUserId())
                        .param("password", user.getPassword()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/index.html"))
                .andExpect(cookie().value("logined", "true"))
                .andExpect(cookie().path("logined", "/"));
    }

    @Test
    void wrongPasswordAndMissingUserRedirectWithoutLoginCookie() throws Exception {
        User user = addUser();
        for (String id : new String[] { user.getUserId(), "missing-" + UUID.randomUUID() }) {
            mvc.perform(post("/user/login").param("userId", id).param("password", "wrong"))
                    .andExpect(status().isFound())
                    .andExpect(redirectedUrl("/user/login_failed.html"))
                    .andExpect(cookie().doesNotExist("logined"));
        }
    }

    @Test
    void listRejectsMissingFalseAndUnrelatedCookies() throws Exception {
        mvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(""));
        for (Cookie cookie : new Cookie[] {
                new Cookie("logined", "false"),
                new Cookie("theme", "dark"),
                new Cookie("logined", "TRUE") }) {
            mvc.perform(get("/api/users").cookie(cookie))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Test
    void listReturnsOnlyNamesAsJsonWithLoginCookieAmongOtherCookies() throws Exception {
        User user = addUser();
        mvc.perform(get("/api/users").cookie(new Cookie("theme", "dark"), new Cookie("logined", "true")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasItem(user.getName())))
                .andExpect(jsonPath("$[*]", everyItem(instanceOf(String.class))))
                .andExpect(content().string(not(containsString(user.getPassword()))))
                .andExpect(content().string(not(containsString(user.getEmail()))));
    }

    @Test
    void singleUserReturnsNameOrNotFound() throws Exception {
        User user = addUser();
        mvc.perform(get("/api/users/{userId}", user.getUserId()).cookie(new Cookie("logined", "true")))
                .andExpect(status().isOk())
                .andExpect(content().string(user.getName()));
        mvc.perform(get("/api/users/{userId}", "missing-" + UUID.randomUUID()).cookie(new Cookie("logined", "true")))
                .andExpect(status().isNotFound());
    }

    @Test
    void singleUserAlsoRequiresLoginCookie() throws Exception {
        User user = addUser();
        mvc.perform(get("/api/users/{userId}", user.getUserId()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void redirectTargetsAndCssAreAvailableButListHtmlIsNotPublic() throws Exception {
        for (String path : new String[] { "/", "/index.html", "/user/login.html", "/user/login_failed.html", "/user/form.html" }) {
            mvc.perform(get(path)).andExpect(status().isOk());
        }
        mvc.perform(get("/css/styles.css"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/css"));
        mvc.perform(get("/user/list.html")).andExpect(status().isNotFound());
        mvc.perform(get("/user/userList")).andExpect(status().isNotFound());
    }

    private User addUser() {
        String suffix = UUID.randomUUID().toString();
        User user = new User("student-" + suffix, "secret-" + suffix,
                "김학생-" + suffix, suffix + "@example.com");
        userRepository.addUser(user);
        return user;
    }
}
