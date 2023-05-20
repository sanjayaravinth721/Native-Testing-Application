package com;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input extends HttpServlet {
    public static int testcount = 0;

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String filenamewithext = (String) request.getParameter("filename");
        String output = "";
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        LinkedHashMap<String, LinkedList<String>> inputs = (LinkedHashMap<String, LinkedList<String>>)session.getAttribute("inputs");

        response.getWriter().print(inputs);

    }
}
