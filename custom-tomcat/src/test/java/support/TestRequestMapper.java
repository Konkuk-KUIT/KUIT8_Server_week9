package support;

import controller.IndexController;
import controller.LoginController;
import controller.SignupController;
import controller.UserListController;
import db.Repository;
import http.HttpMethod;
import webserver.RequestMapper;

import java.util.Map;

public final class TestRequestMapper {
    private TestRequestMapper() {
    }

    public static RequestMapper create(Repository repository) {
        var index = new IndexController();
        var signup = new SignupController(repository);
        var login = new LoginController(repository);
        var list = new UserListController();
        return new RequestMapper(Map.of(
                new RequestMapper.Route(HttpMethod.GET, "/"), index,
                new RequestMapper.Route(HttpMethod.GET, "/index.html"), index,
                new RequestMapper.Route(HttpMethod.GET, "/user/signup"), signup,
                new RequestMapper.Route(HttpMethod.POST, "/user/signup"), signup,
                new RequestMapper.Route(HttpMethod.POST, "/user/login"), login,
                new RequestMapper.Route(HttpMethod.GET, "/user/userList"), list,
                new RequestMapper.Route(HttpMethod.GET, "/user/list.html"), list
        ));
    }
}
