package com;

import java.io.*;
import java.nio.charset.StandardCharsets;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.util.*;

public class DisplayFile extends HttpServlet {
    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String filename = (String) request.getParameter("filename");
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        String userDirectory = "D:\\UserFiles";

        String filepath = userDirectory+"\\"+username+"\\"
                + filename;


        File file = new File(filepath);
        String myfile = "";
        if(file.exists()){
             myfile = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        }
        else{
            ////System.out.println("in common!");
            try{
                myfile =  checkCommonFile(filename);
            }
            catch(Exception e){
                ////System.out.println(e);
            }
           
        }
  
   
        ////System.out.println("MY FILE : "+myfile);
        ////System.out.println("---------------MY FILE---------------");
        ////System.out.println("------------DISPLAY FILE SESSION---------- "+username);
        ////System.out.println(myfile);

        response.getWriter().write(myfile);
        
        ////System.out.println("OLDDDDD FILE....");
        ////System.out.println( Program.oldFile);


    }

    public String checkCommonFile(String filename) throws Exception{
        String userDirectory = "D:\\UserFiles";
        String filePath = userDirectory + "\\CommonFiles\\" + filename;
        return FileUtils.readFileToString(new File(filePath), StandardCharsets.UTF_8);
    }

}
