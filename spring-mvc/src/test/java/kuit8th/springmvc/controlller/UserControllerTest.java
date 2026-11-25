package kuit8th.springmvc.controlller;

import jakarta.servlet.http.Cookie;
import kuit8th.springmvc.dto.UserDTO;
import kuit8th.springmvc.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp(){
        userRepository.addUser(new UserDTO("user1", "user1", "user1", "user1"));
        userRepository.addUser(new UserDTO("user2", "user2", "user2", "user2"));
        userRepository.addUser(new UserDTO("user3", "user3", "user3", "user3"));
        userRepository.addUser(new UserDTO("user4", "user4", "user4", "user4"));

    }

    @Test
    void 회원가입() throws Exception {
        // given

        // when
        ResultActions result = mockMvc.perform(post("/user/signup")
                .param("userId", "user5")
                .param("password", "user5")
                .param("name", "user5")
                .param("email", "user5"));

        // then
        result.andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
        assertThat(userRepository.findUserById("user5")).isPresent();
    }

    @Test
    void 로그인() throws Exception {
        //when
        ResultActions result = mockMvc.perform(post("/user/login")
                .param("userId", "user4")
                .param("password", "user4"));

        //then
        result.andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(header().string(HttpHeaders.SET_COOKIE,
                        containsString("login-success=true")));
    }


    @Test
    void 로그인_전체_회원_조회() throws Exception {
        // given: 로그인했다는 표시로 쿠키를 준비
        Cookie loginCookie = new Cookie("login-success", "true");

        // when: 쿠키를 실어서 GET 요청
        ResultActions result = mockMvc.perform(get("/user/userList")
                .cookie(loginCookie));

        // then: 200이고, 응답 JSON 배열에 user1~4가 들어 있는지
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[*].userId", hasItems("user1", "user2", "user3", "user4")));
    }

    @Test
    void 로그인_실패시_전체_회원_조회() throws Exception {

        //when
        mockMvc.perform(get("/user/userList"))
                .andExpect(status().isUnauthorized());

        //then

    }

    @Test
    void 특정_회원_조회() throws Exception {
        mockMvc.perform(get("/api/user/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("user1"));

    }

}
