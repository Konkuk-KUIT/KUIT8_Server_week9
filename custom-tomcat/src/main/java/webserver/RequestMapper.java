package webserver;

import controller.Controller;
import controller.HomeController;
import controller.SignUpController;
import controller.LoginController;
import controller.ListController;
import controller.StaticFileController;

import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import http.HttpStatus;

import java.io.IOException;
import java.util.Map;

public class RequestMapper {

    private final Map<RequestKey, Controller> controllers;
    private final StaticFileController staticFileController;

    public RequestMapper(
            HomeController homeController,
            SignUpController signUpController,
            LoginController loginController,
            ListController listController,
            StaticFileController staticFileController) {

        this.staticFileController = staticFileController;

        // 요청과 컨트롤러 연결
        this.controllers = Map.of(
                new RequestKey(HttpMethod.GET, "/"),
                homeController,

                new RequestKey(HttpMethod.GET, "/index.html"),
                homeController,

                new RequestKey(HttpMethod.POST, "/user/signup"),
                signUpController,

                new RequestKey(HttpMethod.POST, "/user/login"),
                loginController,

                new RequestKey(HttpMethod.GET, "/user/userList"),
                listController,

                new RequestKey(HttpMethod.GET, "/user/list.html"),
                listController
        );
    }

    // 요청에 해당하는 컨트롤러 찾기
    public Controller findController(HttpRequest request) {

        RequestKey key = new RequestKey(
                request.getMethod(),
                request.getPath()
        );

        return controllers.get(key);
    }

    // 컨트롤러 선택 및 실행
    public void dispatch(HttpRequest request, HttpResponse response)
            throws IOException {

        Controller controller = findController(request);

        if (controller != null) {
            controller.execute(request, response);
            return;
        }

        // 매핑에 없는 GET 요청은 정적 파일 처리
        if (request.getMethod() == HttpMethod.GET) {
            staticFileController.execute(request, response);
            return;
        }

        response.error(HttpStatus.NOT_FOUND);
    }
}
