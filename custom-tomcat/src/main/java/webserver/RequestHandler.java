package webserver;

import java.io.*;
import java.net.Socket;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import db.MemoryUserRepository;
import http.HttpMethod;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.nio.file.Files;
import java.nio.file.Path;

import http.HttpResponse;

public class RequestHandler implements Runnable {
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : "
                + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);
            HttpResponse response = new HttpResponse(dos);

            String requestLine = br.readLine();
            String[] tokens = requestLine.split(" ");

            HttpMethod method = HttpMethod.valueOf(tokens[0]);

            // 메소드 다음 부분을 경로와 쿼리부분으로 나눔
            String url[] = tokens[1].split("\\?", 2);
            String path = url[0];

            String version = tokens[2];
            byte[] body = null;

            int contentLength = 0;
            String Cookie = "";
            String line;
            // 빈 줄이 나올 때까지 헤더 읽기
            while ((line = br.readLine()) != null && !line.isEmpty()) {
                int colon = line.indexOf(':');

                // content-length를 찾는 과정,이때 대소문자를 무시해야함에 유의,
                // 브라우저는 대소문자 신경 안쓰고 보낼 수 있음
                if (colon > 0
                        && line.substring(0, colon)
                                .equalsIgnoreCase("Content-Length")) {
                    contentLength = Integer.parseInt(line.substring(colon + 1).trim());
                }
                if (colon > 0
                        && line.substring(0, colon)
                                .equalsIgnoreCase("Cookie")) {
                    Cookie = line.substring(colon + 1).trim();
                }
            }

            boolean logined = false;

            for (String item : Cookie.split(";")) {
                String[] pair = item.trim().split("=", 2);

                if (pair.length == 2
                        && pair[0].equals("logined")
                        && pair[1].equals("true")) {
                    logined = true;
                }
            }

            if (method == HttpMethod.GET) {
                // "/" 요청이면 index.html 반환
                if (path.equals("/")) {
                    path = "/index.html";
                }

                // 사용자 목록 요청이면 로그인 여부 확인
                if (path.equals("/user/userList")
                        || path.equals("/user/list.html")) {

                    if (!logined) {
                        response.redirect("/user/login.html");
                        return;
                    }

                    path = "/user/list.html";
                }

                // HTML 또는 CSS 파일 반환
                response.forward(path);
                return;
            }

            if (method == HttpMethod.POST) {
                // POST 본문 읽기
                String formData = IOUtils.readData(br, contentLength);

                Map<String, String> params = HttpRequestUtils.parseQueryParameter(formData);

                // 회원가입
                if (path.equals("/user/signup")) {
                    User user = new User(
                            params.get("userId"),
                            params.get("password"),
                            params.get("name"),
                            params.get("email"));

                    MemoryUserRepository.getInstance().addUser(user);

                    response.redirect("/index.html");
                    return;
                }

                // 로그인
                if (path.equals("/user/login")) {
                    String userId = params.get("userId");
                    String password = params.get("password");

                    User user = MemoryUserRepository.getInstance()
                            .findUserById(userId);

                    if (user != null && user.getPassword().equals(password)) {
                        response.setCookie("logined=true; Path=/");
                        response.redirect("/index.html");
                    } else {
                        response.redirect("/user/login_failed.html");
                    }

                    return;
                }
            }
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}
