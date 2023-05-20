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

public class LogOut extends HttpServlet {
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        try {
            response.setContentType("text/plain");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);

            String username = request.getUserPrincipal().getName();
            HttpSession session = request.getSession(false);
       

            Cookie cookie = request.getCookies()[0];
            System.out.println("Cookie name : " + cookie.getName());
            System.out.println("Cookie value : " + cookie.getValue());
            cookie.setMaxAge(0);
            cookie.setValue("");
            System.out.println("Username in logout servlet : " + username);

            session.invalidate();
            
            request.logout();
            out.println(username);

            // request.getRequestDispatcher("index.html").include(request, response);
        } catch (Exception e) {
            System.out.println("session already invalidated!");
        }
    }

}
