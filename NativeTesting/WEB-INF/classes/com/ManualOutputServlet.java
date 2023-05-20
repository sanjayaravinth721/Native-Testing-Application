package com;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class ManualOutputServlet extends HttpServlet {

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String filenamewithext = (String) request.getParameter("filename");
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        String output = "";

        try {

            Program program = new Program();
            ////System.out.println("manual compile!");

            output = program.compile(filenamewithext, username, 1);

            response.getWriter().print(output);

        } catch (Exception e) {
            ////System.out.println(e);

        }
    }
}
