package com.ljheee.api.core;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by lijianhua04 on 2018/7/27.
 */
public class ApiGatewayServlet extends HttpServlet {


    ApplicationContext context;

    @Autowired
    ApiGatewayHandler apiGatewayHandler;

    @Override
    public void init( ) throws ServletException {
        context = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        apiGatewayHandler = context.getBean(ApiGatewayHandler.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        apiGatewayHandler.handle(req, resp);
        System.out.println("=======in=doGet===");

    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        apiGatewayHandler.handle(req, resp);
    }
}
