package com;

// import java.io.File;
// import java.io.IOException;
// import java.io.PrintWriter;
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
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileUpload extends HttpServlet {
    // ArrayList<String> datatypes = new ArrayList<>();

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        try {

            HttpSession session = request.getSession();
            String username = (String) session.getAttribute("username");
            System.out.println("username servlet : "+username);

            UserDAO userDAO = new UserDAO();
            String userDirectory = "D:\\UserFiles";

            PrintWriter out = response.getWriter();
            List<String> users = new UserDAO().getUsers();

            //// System.out.println("REQUEST: " + request.getParameter("indivitual"));
            //// System.out.println("REQUEST remove: " +
            //// request.getParameterValues("removeUsers"));
            if (request.getParameter("single") != null && request.getParameter("single") != "") {
                System.out.println("single servlet!");
                
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                FileItem fileItem = multiparts.get(0);
                //// System.out.println(fileItem);
                String itemname = fileItem.getName();
           

                String filepath = userDirectory + "\\"
                        + username + "\\"
                        + itemname;

                File file = new File(filepath);

                if (!file.exists()) {
                    userDAO.insertUserFiles(username, itemname);
                    fileItem.write(new File(filepath));
                }

            }

            else if (request.getParameter("removeUsers") != null && request.getParameter("removeUsers") != "") {
                String[] list = request.getParameterValues("removeUsers");
                //// System.out.println("removeUsers : " + list);

                for (String user : list) {
                    //// System.out.println("var: " + user);
                    File programFile = new File(
                            userDirectory + "\\"
                                    + user);
                    try {
                        deleteFolder(programFile);
                        // FileUtils.deleteDirectory(programFile);
                        userDAO.removeUser(user);
                        //// System.out.println("Directory deleted successfully");
                    } catch (Exception e) {
                        //// System.out.println("Failed to delete the directory: " + e.getMessage());
                    }
                }

            } else {
                List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                FileItem fileItem = multiparts.get(0);
                //// System.out.println(fileItem);
                String itemname = fileItem.getName();
                //// System.out.println("IN FILE UPLOAD!!!!" + itemname);

                if (request.getParameter("indivitual") != null && request.getParameter("indivitual") != "") {

                    String[] list = request.getParameterValues("indivitual");
                    //// System.out.println("indivitiual : " + list);
                    for (String user : list) {
                        //// System.out.println("users: " + user);

                        String filepath = userDirectory + "\\"
                                + user + "\\"
                                + itemname;
                        File file = new File(filepath);

                        if (!file.exists()) {
                            userDAO.insertUserFiles(user, itemname);
                            fileItem.write(new File(filepath));
                        }

                    }
                    users = (List<String>) Arrays.asList(list);
                    out.println("Uploaded successfully!");
                }

                else {

                    String filePath = userDirectory + "\\CommonFiles\\" + itemname;

                    File folderPath = new File(userDirectory + "\\CommonFiles");
                    File programFile = new File(filePath);

                    if (!folderPath.exists()) {
                        if (folderPath.mkdir()) {
                            //// System.out.println("Directory is created!");
                        } else {
                            //// System.out.println("Failed to create directory!");
                        }
                    } else {
                        //// System.out.println("s Directory already exists.");
                    }

                    if (!programFile.exists()) {
                        if (FilenameUtils.getExtension(itemname).equals("c")
                                || FilenameUtils.getExtension(itemname).equals("cpp")) {
                            String filepath = userDirectory + "\\CommonFiles\\" + itemname;

                            //// System.out.println("in upload!");
                            File file = new File(filepath);
                            userDAO.uploadCommon(itemname);
                            fileItem.write(new File(filepath));
                            fileItem = null;
                            System.out.println("user common!");
                            System.out.println("users: " + userDAO.getUsers());
                            for (String user : userDAO.getUsers()) {
                                String name = user.replace("\"", "");
                                Path userFilePath = Paths.get("D:\\UserFiles\\" + name + "\\" + itemname);
                                Files.createSymbolicLink(userFilePath, file.toPath());
                                System.out.println("symbolic");

                            }
                            out.println("Uploaded successfully!");
                            // clear(filepath);
                            // userDAO.insertUserFiles(username, itemname);

                        }
                    } else {
                        out.println(itemname + " already exists!");
                    }

                }

            }

        } catch (

        Exception e) {
            //// System.out.println(e);
        }
    }

    public void deleteFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteFolder(subFile);
            } else {
                subFile.delete();
            }
        }
        file.delete();
    }

    public void clear(String filepath) throws IOException {

        Scanner edit = new Scanner(new File(filepath));
        boolean iscleared = false;

        StringBuffer editBuffer = new StringBuffer();
        StringBuilder sb = new StringBuilder();
        boolean insideForLoop = false;
        String st;

        while (edit.hasNextLine()) {
            String line = edit.nextLine();

            // if (!iscleared) {
            if (!(line.contains("for") || line.contains("while")) && !(line.contains("printf"))
                    && !(line.contains("cout")) && line.contains(";")) {
                line = line.replace(";", ";" + System.lineSeparator());
            }

            if (line.contains("for")) {
                insideForLoop = true;
            }

            if (insideForLoop) {
                if (line.contains("cin")) {
                    line = line
                            .replaceAll("for\\s*\\(.*\\)\\s*\\{[\\s\\S]*?cin.*?[\\s\\S]*?\\}", "");

                    continue;
                } else if (line.contains("scanf")) {
                    line = line
                            .replaceAll("for\\s*\\(.*\\)\\s*\\{[\\s\\S]*?scanf.*?[\\s\\S]*?\\}", "");
                    continue;
                }
            }
            if (line.contains("}")) {
                insideForLoop = false;
            }
            editBuffer.append(line + System.lineSeparator());
        }

        String editedfileContents = editBuffer.toString();

        FileWriter editedwriter = new FileWriter(filepath);
        //// System.out.println("");
        //// System.out.println("new data: " + editedfileContents);

        editedwriter.append(editedfileContents);
        editedwriter.flush();
        edit.close();
        iscleared = true;

    }

}