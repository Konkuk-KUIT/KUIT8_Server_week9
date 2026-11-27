package webserver;

import db.MemoryUserRepository;
import db.Repository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
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

        Path webRoot = Path.of("webapp");
        Path listPage = webRoot.resolve("user/list.html");
        Repository repository = MemoryUserRepository.getInstance();
        Controller home = new HomeController(webRoot.resolve("index.html"));
        Controller signUp = new SignUpController(repository);
        Controller login = new LoginController(repository);
        Controller list = new ListController(listPage);
        Controller staticFiles = new StaticFileController(webRoot, listPage);
        RequestMapper mapper = new RequestMapper(Map.of(
                new RequestKey(HttpMethod.GET, "/"), home,
                new RequestKey(HttpMethod.POST, "/user/signup"), signUp,
                new RequestKey(HttpMethod.POST, "/user/login"), login,
                new RequestKey(HttpMethod.GET, "/user/userList"), list,
                new RequestKey(HttpMethod.GET, "/user/list.html"), list
        ), staticFiles);

        // TCP 환영 소켓
        try (ServerSocket welcomeSocket = new ServerSocket(port)){

            // 연결 소켓
            Socket connection;
            while ((connection = welcomeSocket.accept()) != null) {
                // 스레드에 작업 전달
                service.submit(new RequestHandler(connection, mapper));
            }
        }

    }
}
