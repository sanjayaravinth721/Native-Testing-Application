package com;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;

public class OutputServlet extends HttpServlet {
    public static int testcount = 0;
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {


        String filenamewithext = (String) request.getParameter("filename");
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        String output = "";

        try {

            Program program = new Program();

            output = program.compile(filenamewithext,username,2);

            ////System.out.println("OUTPUTSERVLET HASHMAP: "+program.getInputs());

            session.setAttribute("inputs",program.getInputs());

            response.getWriter().print(output);

        } catch (Exception e) {
            ////System.out.println(e);
    
        }

    }
}
