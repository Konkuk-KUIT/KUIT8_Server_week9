package webserver.controller;

import webserver.HttpMethod;

public record RequestKey(HttpMethod method, String path) {
}
