package webserver;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.HttpRequest;
import http.HttpResponse;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (Socket socket = connection;
             InputStream in = socket.getInputStream(); OutputStream out = socket.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
            HttpResponse response = new HttpResponse(out);

            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }
            HttpMethod method = request.getMethod();
            String path = request.getPath();
            boolean logined = "true".equals(request.getCookie("logined"));
            log.info("Request: " + method + " " + path);

            // 회원가입: GET은 URL의 입력값, POST는 요청 본문의 입력값을 사용합니다.
            if ("/user/signup".equals(path) && (method == HttpMethod.GET || method == HttpMethod.POST)) {
                Map<String, String> parameters = request.getParameters();
                String userId = parameters.get("userId");
                String password = parameters.get("password");
                String name = parameters.get("name");
                String email = parameters.get("email");
                if (userId == null || password == null || name == null || email == null) {
                    throw new IllegalArgumentException("회원가입 항목이 누락되었습니다.");
                }

                User user = new User(userId, password, name, email);
                MemoryUserRepository.getInstance().addUser(user);
                log.info("회원가입 완료: " + user.getUserId());

                // 브라우저가 /index.html을 새로 요청하도록 안내합니다.
                response.redirect("/index.html");
                return;
            }

            // 로그인: 아이디로 사용자를 찾은 뒤 비밀번호를 비교합니다.
            if (method == HttpMethod.POST && "/user/login".equals(path)) {
                Map<String, String> parameters = request.getParameters();
                String userId = parameters.get("userId");
                String password = parameters.get("password");
                if (userId == null || password == null) {
                    response.redirect("/user/login_failed.html");
                    return;
                }

                User user = MemoryUserRepository.getInstance().findUserById(userId);
                if (user != null && password.equals(user.getPassword())) {
                    response.redirect("/index.html", "logined=true; Path=/");
                } else {
                    response.redirect("/user/login_failed.html");
                }
                return;
            }

            // 3. 첫 화면 주소 /는 /index.html로 처리합니다.
            if ("/".equals(path)) {
                path = "/index.html";
            }
            // User List 버튼 주소도 실제 목록 HTML 경로로 연결합니다.
            if (method == HttpMethod.GET && "/user/userList".equals(path)) {
                path = "/user/list.html";
            }

            // 4. 목록 접근 허용 여부를 결정하고, 파일 응답은 HttpResponse에 맡깁니다.
            if (method == HttpMethod.GET && path.startsWith("/")
                    && (path.endsWith(".html") || path.endsWith(".css"))) {
                // ../이 포함된 경로도 정리한 뒤 목록 파일인지 확인합니다.
                Path relativePath = Path.of(path.substring(1)).normalize();
                boolean isUserList = relativePath.equals(Path.of("user", "list.html"));
                if (isUserList && !logined) {
                    response.redirect("/user/login.html");
                    return;
                }
                response.forward(path);
                return;
            }

            // 아직 구현하지 않은 경로는 404(찾을 수 없음)로 응답합니다.
            response.notFound();

        } catch (IOException | IllegalArgumentException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

}
