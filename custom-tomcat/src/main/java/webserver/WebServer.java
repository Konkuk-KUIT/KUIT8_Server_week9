package webserver;

import controller.*;
import db.MemoryUserRepository;
import db.Repository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static http.HttpMethod.*;

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

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            Repository repository = MemoryUserRepository.getInstance();
            Controller list = new ListController();
            RequestMapper mapper = new RequestMapper(Map.of(
                    new RequestKey(GET, "/"), new HomeController(),
                    new RequestKey(POST, "/user/signup"), new SignUpController(repository),
                    new RequestKey(POST, "/user/login"), new LoginController(repository),
                    new RequestKey(GET, "/user/userList"), list,
                    new RequestKey(GET, "/user/list.html"), list), new StaticFileController());
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection, mapper));
            }
        }

    }
}
