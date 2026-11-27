package webserver.controller;

import webserver.HttpRequest;
import webserver.HttpResponse;

import java.io.IOException;

public class ListController implements  Controller{
    @Override
    public void execute(HttpRequest request, HttpResponse response) throws IOException {
        if("true".equals(request.getCookie("logined"))){
            response.forward("/user/list.html");
        }else{
            response.redirect("/user/login.html");
        }
    }
}
