package webserver;

import controller.IndexController;
import controller.LoginController;
import controller.SignupController;
import controller.UserListController;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

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

        Repository repository = MemoryUserRepository.getInstance();
        IndexController indexController = new IndexController();
        SignupController signupController = new SignupController(repository);
        LoginController loginController = new LoginController(repository);
        UserListController userListController = new UserListController();
        RequestMapper requestMapper = new RequestMapper(Map.of(
                new RequestMapper.Route(HttpMethod.GET, "/"), indexController,
                new RequestMapper.Route(HttpMethod.GET, "/index.html"), indexController,
                new RequestMapper.Route(HttpMethod.GET, "/user/signup"), signupController,
                new RequestMapper.Route(HttpMethod.POST, "/user/signup"), signupController,
                new RequestMapper.Route(HttpMethod.POST, "/user/login"), loginController,
                new RequestMapper.Route(HttpMethod.GET, "/user/userList"), userListController,
                new RequestMapper.Route(HttpMethod.GET, "/user/list.html"), userListController
        ));

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection, requestMapper));
            }
        }

    }
}
