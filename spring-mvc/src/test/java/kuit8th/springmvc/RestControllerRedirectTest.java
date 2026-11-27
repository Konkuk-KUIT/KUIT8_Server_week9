package kuit8th.springmvc;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RestControllerRedirectTest {
    @Test
    void redirectStringInRestControllerIsResponseBodyRatherThanRedirect() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new RedirectExperimentController()).build();

        mvc.perform(get("/redirect-experiment"))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Location"))
                .andExpect(content().string("redirect:/index.html"));
    }

    // 실험용 컨트롤러는 테스트 안에서만 등록한다.
    @RestController
    static class RedirectExperimentController {
        @GetMapping("/redirect-experiment")
        public String redirect() {
            return "redirect:/index.html";
        }
    }
}
