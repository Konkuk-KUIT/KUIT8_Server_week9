package webserver;

import db.MemoryUserRepository;
import db.Repository;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
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
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            DataOutputStream dos = new DataOutputStream(out);

            Repository userRepository = MemoryUserRepository.getInstance();

            HttpRequest request = HttpRequest.from(br);

            byte[] body = "Not Found".getBytes();
            String baseUrl = "./webapp";

            HttpResponse httpResponse = new HttpResponse(dos);

            switch (request.getHttpMethod()) {
                case GET -> {
                    switch (request.getPath()) {
                        case "/" -> httpResponse.redirect("/index.html");
                        case "/user/signup" -> {
                            String userId = request.getQueryParameter().get("userId");
                            String password = request.getQueryParameter().get("password");
                            String name = request.getQueryParameter().get("name");
                            String email = request.getQueryParameter().get("email");

                            User user = new User(userId, password, name, email);

                            userRepository.addUser(user);

                            User findUser = userRepository.findUserById(user.getUserId());
                            System.out.println(findUser.getName());

                            httpResponse.redirect("/index.html");
                        }
                        case "/user/userList" -> {
                            if (request.isLogin()) {
                                httpResponse.serveResource(baseUrl + "/user/list.html");
                            } else {
                                httpResponse.redirect("/user/login.html");
                            }
                        }
                        default -> httpResponse.serveResource(baseUrl + request.getPath());
                    }
                }
                case POST -> {
                    if (request.getPath().equals("/user/signup")) {
                        Map<String, String> parseRequestBody = HttpRequestUtils.parseQueryParameter(request.getRequestBody());

                        String userId = parseRequestBody.get("userId");
                        String password = parseRequestBody.get("password");
                        String name = parseRequestBody.get("name");
                        String email = parseRequestBody.get("email");

                        User user = new User(userId, password, name, email);

                        userRepository.addUser(user);

                        httpResponse.redirect("/index.html");

                    } else if (request.getPath().equals("/user/login")) {
                        Map<String, String> parseRequestBody = HttpRequestUtils.parseQueryParameter(request.getRequestBody());

                        String loginUserId = parseRequestBody.get("userId");
                        String loginUserPw = parseRequestBody.get("password");

                        List<User> userList = userRepository.findAll().stream().toList();

                        boolean isLoginSuccess = false;
                        for (var usr : userList) {
                            if (usr.getUserId().equals(loginUserId) && usr.getPassword().equals(loginUserPw)) {
                                isLoginSuccess = true;
                                break;
                            }
                        }

                        if (isLoginSuccess) {
                            httpResponse.redirectLogin("/index.html", isLoginSuccess);

                        } else {
                            httpResponse.redirectLogin("/login_failed.html", isLoginSuccess);
                        }
                    }

                }
            }

        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }
}