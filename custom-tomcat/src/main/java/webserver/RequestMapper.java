package webserver;

import webserver.controller.*;
import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    private static final Map<String, Controller> controllers = new HashMap<>();
    private static final Controller defaultController = new ForwardController();

    static {
        controllers.put("/user/signup", new SignUpController());
        controllers.put("/user/login", new LoginController());
        controllers.put("/user/userList", new ListController());
        controllers.put("/user/list.html", new ListController());
    }

    public static Controller getController(String url) {
        return controllers.getOrDefault(url, defaultController);
    }
}