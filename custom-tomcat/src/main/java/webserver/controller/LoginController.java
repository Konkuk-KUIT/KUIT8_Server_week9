package webserver.controller;

import db.MemoryUserRepository;
import db.Repository;
import http.HttpHeader;
import model.User;
import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;
import java.util.Map;

public class LoginController implements Controller{

    private final Repository repository;
    public LoginController(Repository repository){
        this.repository = repository;
    }
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        Map<String, String> parameter = request.getParameter();
        String loginUserID = parameter.get("userId");
        String loginUserPassword = parameter.get("password");
        User findedUser = repository.findUserById(loginUserID);
        if(findedUser != null &&findedUser.getPassword().equals(loginUserPassword)){
            //찾은 유저에 대한 비밀번호가 일치할 때
            response.addHeader(HttpHeader.SET_COOKIE, "logined=true; Path=/");
            response.redirect("/index.html");
        }else {
            //없는 아이디 혹은 틀린 비밀번호 일 때
            response.redirect("/user/login_failed.html");
        }
    }
}
