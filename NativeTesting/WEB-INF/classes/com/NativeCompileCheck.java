package com;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.io.IOException;
import javax.servlet.*;
import javax.servlet.http.*;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FileDeleteStrategy;
import org.apache.commons.io.FilenameUtils;

import com.google.gson.Gson;

import java.util.*;

public class NativeCompileCheck extends HttpServlet {
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String username = (String) session.getAttribute("username");
        String filename = (String) request.getParameter("filename");
        // LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> inputs = (LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>>)session.getAttribute("inputs");
        ////System.out.println("On native console");
        NativeHandling check = new NativeHandling();
        LinkedHashSet<String> compile = check.checkCompileErrors(username, filename);
    
        ////System.out.println(compile);
        

        if (compile.size() == 0) {
            response.getWriter().write("No errors");
        } else {
            ////System.out.println("checking.."+compile);

            LinkedHashSet<String> errors = check.getClearedErrors();
            ////System.out.println("error link: "+errors);

            response.getWriter().print(errors);
        
        }

    }

}
