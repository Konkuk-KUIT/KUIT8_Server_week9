
package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import controller.HomeController;
import controller.SignUpController;
import controller.LoginController;
import controller.ListController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import db.Repository;
import http.HttpMethod;

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

        // Repository 생성 및 Controller 의존성 주입
        Repository repository = MemoryUserRepository.getInstance();

        HomeController homeController = new HomeController();
        SignUpController signUpController = new SignUpController(repository);
        LoginController loginController = new LoginController(repository);
        ListController listController = new ListController();
        StaticFileController staticFileController = new StaticFileController();

        // RequestMapper 생성 및 요청 경로 등록
        RequestMapper requestMapper = new RequestMapper(staticFileController);

        requestMapper.register(HttpMethod.GET, "/", homeController);
        requestMapper.register(HttpMethod.GET, "/index.html", homeController);

        requestMapper.register(HttpMethod.POST, "/user/signup", signUpController);
        requestMapper.register(HttpMethod.POST, "/user/login", loginController);

        requestMapper.register(HttpMethod.GET, "/user/userList", listController);
        requestMapper.register(HttpMethod.GET, "/user/list.html", listController);

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
