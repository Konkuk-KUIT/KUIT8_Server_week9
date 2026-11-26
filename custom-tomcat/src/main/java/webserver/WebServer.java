//8080번을 열고 브라우저를 메서드에 넘김

package webserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import controller.Controller;
import controller.HomeController;
import controller.ListController;
import controller.LoginController;
import controller.SignUpController;
import controller.StaticFileController;
import db.MemoryUserRepository;
import db.Repository;
import http.constant.HttpMethod;

public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_THREAD_NUM = 50;
    
    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        // 1. 저장소 준비
        Repository repository = MemoryUserRepository.getInstance();

        // 2. 일꾼 고용 + 3. 메뉴판 작성
        ListController listController = new ListController();
        Map<RequestKey, Controller> controllers = Map.of(
                new RequestKey(HttpMethod.GET, "/"), new HomeController(),
                new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(repository),
                new RequestKey(HttpMethod.POST, "/user/login"), new LoginController(repository),
                new RequestKey(HttpMethod.GET, "/user/userList"), listController,
                new RequestKey(HttpMethod.GET, "/user/list.html"), listController
        );

        // 4. 안내 데스크 설치
        RequestMapper requestMapper = new RequestMapper(controllers, new StaticFileController());

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
