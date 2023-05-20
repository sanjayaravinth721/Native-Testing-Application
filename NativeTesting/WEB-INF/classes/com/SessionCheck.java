package com;

import java.io.IOException;
import java.io.PrintWriter;

import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class SessionCheck extends HttpServlet {
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try {

            String username = request.getUserPrincipal().getName();

            // System.out.println("username : "+username);

            out.print(username);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
