package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_THREAD_NUM = 50;
    private static final Logger log =
            Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        ExecutorService service =
                Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        // 1. 저장소 생성
        Repository repository = MemoryUserRepository.getInstance();

        // 2. 컨트롤러 생성
        SignUpController signUpController = new SignUpController(repository);
        LoginController loginController = new LoginController(repository);
        ListController listController = new ListController();
        StaticFileController staticFileController = new StaticFileController();

        // 3. RequestMapper 생성 및 매핑 등록
        RequestMapper requestMapper = new RequestMapper();

        requestMapper.register(HttpMethod.POST, "/user/signup", signUpController);
        requestMapper.register(HttpMethod.POST, "/user/login", loginController);
        requestMapper.register(HttpMethod.GET, "/user/userList", listController);
        requestMapper.register(HttpMethod.GET, "/user/list.html", listController);

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)) {
            Socket connection;

            while ((connection = welcomeSocket.accept()) != null) {
                service.submit(new RequestHandler(
                        connection,
                        requestMapper,
                        staticFileController
                ));
            }
        }
    }
}