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

import java.util.*;

public class MyFiles extends HttpServlet {
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");

        if(request.getParameter("username")!=null){
            
            username = request.getParameter("username");
            ////System.out.println("username dropdown: "+username);
        }

        String userDirectory = "D:\\UserFiles";
      
        String directoryPath = userDirectory+"\\"
                + username;

        UserDAO userDAO = new UserDAO();
        ////System.out.println(request.getParameter("removeFile"));

        if(request.getParameter("registeredUsers")!=null){

            ArrayList<String> users  = userDAO.getUsers();
            ArrayList<String> allUsers = new ArrayList<>();

            for(String user: users){
                System.out.println("user: "+user);
                System.out.println("username: "+username);
                if(!user.replace("\"", "").equals(username)){
                    allUsers.add(user);
                }
            }
            System.out.println("alluser: "+allUsers);

            if (allUsers.size() != 0) {
                ////System.out.println("list of users: "+users);
                response.getWriter().print(new ArrayList<>(allUsers));
            }
            else {
                response.getWriter().print(0);
            }
        }

        else if (request.getParameter("removeFile") != null) {
            ////System.out.println("remove file");
            String fileToRemove = request.getParameter("removeFile");
        
            
            userDAO.removeUserFile(username, fileToRemove);

            response.getWriter().write(fileToRemove+" is deleted from "+username);
        } else {
            ArrayList<String> userFiles = userDAO.getUserFiles(username);

            if (userFiles.size() != 0) {
                response.getWriter().print(new ArrayList<>(userFiles));
            }

            else {
                response.getWriter().print(0);
            }
        }

       

    }
}
