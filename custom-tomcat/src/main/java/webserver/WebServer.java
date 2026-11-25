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
    private static final Logger log = Logger.getLogger(WebServer.class.getName());

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        ExecutorService service = Executors.newFixedThreadPool(DEFAULT_THREAD_NUM);

        if (args.length != 0) {
            port = Integer.parseInt(args[0]);
        }

        Repository repository= MemoryUserRepository.getInstance();

        HomeController home = new HomeController();
        SignUpController signUp = new SignUpController(repository);
        ListController list = new ListController();
        StaticFileController staticFiles = new StaticFileController();

        RequestMapper mapper = new RequestMapper();

        mapper.register(HttpMethod.GET, "/", home);
        mapper.register(HttpMethod.GET, "/user/signup", signUp);
        mapper.register(HttpMethod.POST, "/user/signup", signUp);
        mapper.register(HttpMethod.POST, "/user/login", new LoginController(repository));
        mapper.register(HttpMethod.GET, "/user/userList", list);
        mapper.register(HttpMethod.GET, "/user/list.html", list);


        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection, mapper, staticFiles));
            }
        }

    }
}
