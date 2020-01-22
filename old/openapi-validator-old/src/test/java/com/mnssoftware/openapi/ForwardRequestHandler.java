package com.mnssoftware.openapi;

import com.mnssoftware.body.BodyHandler;
import com.mnssoftware.config.Config;
import com.mnssoftware.handler.LightHttpHandler;
import com.mnssoftware.httpstring.ContentType;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;

import java.util.List;
import java.util.stream.Collectors;

public class ForwardRequestHandler implements LightHttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        String responseBody = null;
        if(exchange.getAttachment(BodyHandler.REQUEST_BODY) != null) {
            responseBody = Config.getInstance().getMapper().writeValueAsString(exchange.getAttachment(BodyHandler.REQUEST_BODY));
        }

        List<HttpString> headerNames = exchange.getRequestHeaders().getHeaderNames().stream()
                .filter( s -> s.toString().startsWith("todo"))
                .collect(Collectors.toList());
        for(HttpString headerName : headerNames) {
            String headerValue = exchange.getRequestHeaders().get(headerName).getFirst();
            exchange.getResponseHeaders().put(headerName, headerValue);
        }
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, ContentType.APPLICATION_JSON.value());
        exchange.getResponseSender().send(responseBody);
    }
}
