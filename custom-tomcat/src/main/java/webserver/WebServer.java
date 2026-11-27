package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;
import java.util.Map;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
        HomeController homeController = new HomeController();
        SignUpController signUpController = new SignUpController(repository);
        LoginController loginController = new LoginController(repository);
        ListController listController = new ListController();
        StaticFileController staticFileController = new StaticFileController();
        NotFoundController notFoundController = new NotFoundController();
        RequestMapper requestMapper = new RequestMapper(Map.of(
                new RequestKey(HttpMethod.GET, "/"), homeController,
                new RequestKey(HttpMethod.GET, "/index.html"), homeController,
                new RequestKey(HttpMethod.POST, "/user/signup"), signUpController,
                new RequestKey(HttpMethod.POST, "/user/login"), loginController,
                new RequestKey(HttpMethod.GET, "/user/userList"), listController,
                new RequestKey(HttpMethod.GET, "/user/list.html"), listController
        ), staticFileController, notFoundController);

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
