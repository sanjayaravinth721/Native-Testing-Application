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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

public class ManualServlet extends HttpServlet {

    private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> inputs = new LinkedHashMap<>();

    public void service(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        String filename = (String) request.getParameter("filename");
        HttpSession session = request.getSession(false);
        String username = (String) session.getAttribute("username");

        String userDirectory = "D:\\UserFiles";

        String filepath = userDirectory + "\\"
                + username + "\\"
                + filename;
        File file = new File(filepath);

        if (!file.exists()) {
            filepath = userDirectory + "\\CommonFiles\\"
                    + filename;
        }
        try {
           Program.oldFile = new String(Files.readAllBytes(Paths.get(filepath)));

        } catch (

        IOException e) {
            e.printStackTrace();
        }

        inputs.clear();
        int dot = filename.indexOf(".");
        String ext = filename.substring(dot + 1, filename.length());

        String folderPath = userDirectory + "\\"
                + username;

        int filecount = checkMultiFiles(folderPath, filename, ext);

        if (filecount == 1) {
            ////System.out.println("files count: " + filecount);

            ManualHandling manualHandling = new ManualHandling();
            try {
                manualHandling.file(filepath);
            } catch (Exception e) {
                ////System.out.println();
            }

            inputs.put("\"" + filename + "\"", manualHandling.getInputs());

        } else {

            File directoryPath = new File(folderPath + "\\temp");

            String contents[] = directoryPath.list();
            ////System.out.println("List of files and directories in the specified directory:");

            String foldername = folderPath + "\\temp";
            File newDir = new File(foldername);

            for (int i = 0; i < contents.length; i++) {
                ////System.out.println("servlet content: " + contents[i]);

                String newFilenameWithext = contents[i];

                filepath = userDirectory + "\\"
                        + username + "\\temp\\"
                        + contents[i];

                ManualHandling manualHandling = new ManualHandling();
                try {
                    manualHandling.file(folderPath + "\\temp\\" + contents[i]);
                } catch (Exception e) {
                    ////System.out.println();
                }

                inputs.put("\"" + contents[i] + "\"", manualHandling.getInputs());
            }

        }

        ////System.out.println("manual hashmap: " + inputs);
        session.setAttribute("manualInput", inputs);

        String output = null;

        try {

            response.getWriter().print(inputs);

        } catch (Exception e) {
            ////System.out.println(e);

        }

    }

    public int checkMultiFiles(String folderPath, String itemname, String ext) {

        int fileCounter = 1;
        ////System.out.println("folderPath: " + folderPath + "itemname: " + itemname + "ext: " + ext);
        String userDirectory = "D:\\UserFiles";

        try {

            String pattern = "#include\\s*<\\s*\\w+(\\.\\w+)?\\s*>.*\\s+int\\s+main\\s*\\(\\)\\s*\\{[\\s\\S]+?\\}";
            Pattern p = Pattern.compile(pattern);

            File fileItem = new File(folderPath + "\\" + itemname);
            String fileContent = "";
            if (fileItem.exists()) {
                fileContent = new String(Files.readAllBytes(Paths.get(folderPath + "\\" + itemname)));
            } else {
                fileContent = new String(Files.readAllBytes(Paths.get(userDirectory + "\\CommonFiles\\" + itemname)));
            }

            Matcher m = p.matcher(fileContent);

            File tempFile = new File(folderPath + "\\temp");

            if (tempFile.mkdir()) {
                ////System.out.println("created folder");
            } else {
                ////System.out.println("error creating folder!");
            }

            int matchCount = 0;
            while (m.find()) {
                matchCount++;
            }

            m.reset();

            ////System.out.println("match count: " + matchCount);

            while (m.find() && matchCount > 1) {
                ////System.out.println("match found!");
                try {
                    FileWriter writer = new FileWriter(tempFile + "\\" + itemname + "_" + fileCounter + "." + ext);
                    writer.write(m.group());
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    ////System.out.println("An error occurred while writing the file.");
                }
                fileCounter++;
            }

            if (matchCount <= 1) {
                deleteTempFolder(tempFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileCounter;
    }

    public void deleteTempFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteTempFolder(subFile);
            } else {
                ////System.out.println("delete temp: " + subFile.delete());
            }
        }
        file.delete();
    }
}
