package webserver;

import controller.HomeController;
import controller.SignUpController;
import controller.LoginController;
import controller.ListController;
import controller.StaticFileController;

import db.MemoryUserRepository;
import db.Repository;

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
        HomeController homeController = new HomeController();

        SignUpController signUpController =
                new SignUpController(repository);

        LoginController loginController =
                new LoginController(repository);

        ListController listController = new ListController();

        StaticFileController staticFileController =
                new StaticFileController();

        // 3. 컨트롤러를 Mapper에 연결
        RequestMapper requestMapper = new RequestMapper(
                homeController,
                signUpController,
                loginController,
                listController,
                staticFileController
        );

        // 4. 서버 실행
        try (ServerSocket welcomeSocket = new ServerSocket(port)) {

            log.info("서버 실행: http://localhost:" + port);

            Socket connection;

            while ((connection = welcomeSocket.accept()) != null) {

                service.submit(
                        new RequestHandler(connection, requestMapper)
                );
            }
        }
    }
}
