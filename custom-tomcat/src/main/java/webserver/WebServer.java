package webserver;

import controller.HomeController;
import controller.LoginController;
import controller.SignupController;
import controller.UserListController;
import db.MemoryUserRepository;
import db.Repository;
import webserver.RequestMapper.Route;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static http.HttpMethod.GET;
import static http.HttpMethod.POST;

public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_THREAD_NUM = 50;
    private static final Logger log = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        RequestMapper mapper = createRequestMapper(MemoryUserRepository.getInstance());
        FrontController frontController = new FrontController(mapper);

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection, frontController));
            }
        }

    }
    static RequestMapper createRequestMapper(Repository repository) {
        HomeController home = new HomeController();
        SignupController signup = new SignupController(repository);
        LoginController login = new LoginController(repository);
        UserListController userList = new UserListController();
        return new RequestMapper(Map.of(
                new Route(GET, "/"), home,
                new Route(GET, "/index.html"), home,
                new Route(GET, "/user/signup"), signup,
                new Route(POST, "/user/signup"), signup,
                new Route(POST, "/user/login"), login,
                new Route(GET, "/user/userList"), userList,
                new Route(GET, "/user/list.html"), userList
        ));
    }
}
