package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import webserver.controller.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class WebServer {
    private static final int DEFAULT_PORT = 8080;
    private static final int DEFAULT_THREAD_NUM = 50;
    private static final Logger log = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {

        MemoryUserRepository repository = MemoryUserRepository.getInstance();
        Map<RequestKey, Controller> controllers = new HashMap<>();
        controllers.put(new RequestKey(HttpMethod.POST, "/user/signup"), new SignUpController(repository));
        controllers.put(new RequestKey(HttpMethod.POST, "/user/login"), new LoginController(repository));
        controllers.put(new RequestKey(HttpMethod.GET, "/user/userList"), new ListController());
        controllers.put(new RequestKey(HttpMethod.GET, "/user/list.html"), new ListController());
        RequestMapper mapper = new RequestMapper(controllers, new StaticFileController());

        int port = DEFAULT_PORT;
        ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection ,mapper));
            }
        }

    }
}
