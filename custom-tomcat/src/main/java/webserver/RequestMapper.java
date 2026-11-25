package webserver;

import db.Repository;
import webserver.controller.*;

import java.util.HashMap;
import java.util.Map;

public class RequestMapper {
    private final Map<RequestKey, Controller> router;
    private final Controller staticController = new StaticFileController();

    public RequestMapper(Repository repository) {
        this.router = new HashMap<>();
        RequestKey GetHome = new RequestKey(HttpMethod.GET, "/");
        router.put(GetHome, new HomeController());

        RequestKey GetSingUp = new RequestKey(HttpMethod.GET, "/user/singup");
        router.put(GetSingUp, new SignupController(repository));

        RequestKey GetUserList = new RequestKey(HttpMethod.GET, "/user/userList");
        router.put(GetUserList, new ListController());

        RequestKey PostSignUp = new RequestKey(HttpMethod.POST, "/user/signup");
        router.put(PostSignUp, new SignupController(repository));

        RequestKey PostLogin = new RequestKey(HttpMethod.POST, "/user/login");
        router.put(PostLogin, new LoginController(repository));
    }

    public Controller mapping(HttpRequest req) {
        return router.getOrDefault(new RequestKey(req.getHttpMethod(), req.getPath())
                , staticController);
    }
}
