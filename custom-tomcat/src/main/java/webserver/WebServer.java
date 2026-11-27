package webserver;

import controller.Controller;
import controller.HomeController;
import controller.ListController;
import controller.LoginController;
import controller.SignUpController;
import controller.StaticFileController;
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

        // 서버 시작 시 공유할 저장소와 컨트롤러, 매핑을 한 번만 만든다.
        Repository userRepository = MemoryUserRepository.getInstance();
        Controller homeController = new HomeController();
        Controller listController = new ListController();
        RequestMapper requestMapper = new RequestMapper(Map.of(
                new RequestKey(HttpMethod.GET, "/"), homeController,
                new RequestKey(HttpMethod.GET, "/user/userList"), listController,
                new RequestKey(HttpMethod.GET, "/user/list.html"), listController,
                new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(userRepository),
                new RequestKey(HttpMethod.POST, "/user/login"), new LoginController(userRepository)
        ), new StaticFileController());

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

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
