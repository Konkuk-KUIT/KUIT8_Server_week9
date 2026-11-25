package webserver;

import db.MemoryUserRepository;
import http.HttpRequest;
import http.HttpResponse;
import http.util.HttpRequestUtils;
import http.util.IOUtils;
import model.User;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import http.HttpMethod;
import http.HttpStatus;

public class RequestHandler implements Runnable{
    Socket connection;
    private static final Logger log = Logger.getLogger(RequestHandler.class.getName());

    public RequestHandler(Socket connection) {
        this.connection = connection;
    }

    private boolean isLoggedIn(String cookieHeader) {
        if (cookieHeader == null) {
            return false;
        }

        for (String cookie : cookieHeader.split(";")) {
            String[] pair = cookie.trim().split("=", 2);
            if (pair.length == 2
                    && "logined".equals(pair[0].trim())
                    && "true".equals(pair[1].trim())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void run() {
        log.log(Level.INFO, "New Client Connect! Connected IP : " + connection.getInetAddress() + ", Port : " + connection.getPort());
        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()){
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            HttpResponse response = new HttpResponse(out);

            HttpRequest request = HttpRequest.from(br);
            if (request == null) {
                return;
            }

            HttpMethod method = request.startLine().method();
            String requestPath = request.startLine().path();
            String query = request.startLine().query();

            if (method == HttpMethod.GET && "/user/signup".equals(requestPath)) {
                // 여기서 query를 파싱하고 User를 생성해서 저장
                Map<String, String> queries = HttpRequestUtils.parseQueryParameter(query);
                User user = new User(
                        queries.get("userId"),
                        queries.get("password"),
                        queries.get("name"),
                        queries.get("email")
                );
                MemoryUserRepository.getInstance().addUser(user);
                // 응답을 보낸 뒤 return
                return;
            }

            //요구사항3,4
            if (method == HttpMethod.POST && "/user/signup".equals(requestPath)) {
                if (request.body().isEmpty()) {
                    return;
                }

                Map<String, String> params = request.formParams();

                User user = new User(
                        params.get("userId"),
                        params.get("password"),
                        params.get("name"),
                        params.get("email")
                );
                MemoryUserRepository.getInstance().addUser(user);

                response.redirect("/index.html");
                return;


            }

            if (method == HttpMethod.POST && "/user/login".equals(requestPath)){
                if (request.body().isEmpty()) {
                    return;
                }

                Map<String, String> queries = request.formParams();
                String userId = queries.get("userId");
                String password = queries.get("password");
                User user = MemoryUserRepository.getInstance().findUserById(userId);
                if (user == null || !user.getPassword().equals(password)) {
                    response.redirect("/user/login_failed.html");
                }
                else{
                    response.redirect("/index.html", "logined=true; Path=/");
                }

                return;
            }

            if (method == HttpMethod.GET && ("/user/userList".equals(requestPath) || "/user/list.html".equals(requestPath))){
                if (isLoggedIn(request.headers().get("Cookie"))) {
                    response.forward(Path.of("webapp", "user", "list.html"));
                } else {
                    response.redirect("/user/login.html");
                }
                return;
            }

            if (method == HttpMethod.GET && "/css/styles.css".equals(requestPath)){
                response.forward(Path.of("webapp", "css", "styles.css"));
                return;
            }

            //요구사항 1
            String[] pathDet = requestPath.split("/");

            if(pathDet.length==0){
                response.forward(Path.of("webapp", "index.html"));
            }
            if(pathDet.length==2){
                response.forward(Path.of("webapp", pathDet[1]));
            }
            else if(pathDet.length==3){
                response.forward(Path.of("webapp", pathDet[1], pathDet[2]));
            }

        } catch (IOException e) {
            log.log(Level.SEVERE,e.getMessage());
        }
    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent, String cType) {
        try {
            dos.writeBytes(HttpStatus.OK.statusLine());
            dos.writeBytes("Content-Type: "+cType+";charset=utf-8\r\n");
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos,String location, String cookie) {
        try {
            dos.writeBytes(HttpStatus.FOUND.statusLine());
            dos.writeBytes("Location: "+location+"\r\n");
            if (cookie != null) {
                dos.writeBytes("Set-Cookie: " + cookie + "\r\n");
            }
            dos.writeBytes("Content-Length: 0 \r\n");
            dos.writeBytes("Connection: close \r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.log(Level.SEVERE, e.getMessage());
        }
    }

}