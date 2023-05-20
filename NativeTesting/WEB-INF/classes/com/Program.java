package com;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;

import java.util.regex.Matcher;

public class Program {

    public static String oldFile = "";
    private LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> inputs = new LinkedHashMap<>();

    public String compile(String filenamewithext, String username, int type) throws Exception {

        ////System.out.println("FILENAME : " + filenamewithext);
        String userDirectory = "D:\\UserFiles";

        int dot = filenamewithext.indexOf(".");
        int filecount = 0;

        String filename = filenamewithext.substring(0, dot);
        ////System.out.println("FILENAME : " + filename);
        String ext = filenamewithext.substring(dot + 1, filenamewithext.length());
        String line = null;
        String output = "";

        ////System.out.println("--------------USERNAME ; " + username);

        String filepath = userDirectory + "\\"
                + username + "\\"
                + filenamewithext;

        File fileCheck = new File(filepath);

        String userFolderPath = userDirectory + "\\"
                + username;

        String originalFolder = userDirectory + "\\"
                + username;

        if (!fileCheck.exists()) {
            filepath = userDirectory + "\\CommonFiles\\" + filenamewithext;
            originalFolder = userDirectory + "\\CommonFiles";
        }

        ////System.out.println("FILE PATH....... " + filepath);

        ////System.out.println("------------PROGRAM SESSION------------ " + type);

        
        File dir = new File(originalFolder);
        File tempFolder = new File(userFolderPath + "\\temp");


        if (type != 1) {
            try {
                ////System.out.println("tempFolder: " + tempFolder);
                if (tempFolder.exists()) {
                    ////System.out.println("temp folder exists!");
                    dir = new File(
                            userFolderPath + "\\temp");

                    String foldername = userFolderPath + "\\temp";
                    File newDir = new File(foldername);

                    String contents[] = newDir.list();

                    StringBuilder program = new StringBuilder();

                    for (int i = 0; i < contents.length; i++) {
                        ////System.out.println("native handle content: " + contents[i]);

                        FileData fileData = new FileData();
                        fileData.clearInput();

                        fileData.file(userFolderPath + "\\temp\\" + contents[i]);
                        inputs.put("\"" + contents[i] + "\"", fileData.getInputs());

                        ////System.out.println("path: " + userFolderPath + "\\temp\\" + contents[i]);

                        String fileContents = new String(
                                Files.readAllBytes(Paths.get(userFolderPath + "\\temp\\" + contents[i])),
                                StandardCharsets.UTF_8) + "\n";

                        program.append(fileContents);

                        ////System.out.println("appendProg: " + program);

                        String newFilenameWithext = contents[i];
                        String newFilename = newFilenameWithext.substring(0, dot);
                        int j = i+1;
                        String programName = "program "+j;
                        ////System.out.println("in loop compile");
                        output += programName + " : "+ compileFile(newDir, newFilenameWithext, newFilename, foldername, ext) + "\n";
                        ////System.out.println("out loop compile");
                    }

                    Files.write(Paths.get(originalFolder + "\\" + filenamewithext),
                            program.toString().getBytes(StandardCharsets.UTF_8));
                    deleteTempFolder(tempFolder);

                } else {
                    FileData fileData = new FileData();
                    fileData.clearInput();
                    fileData.file(filepath);
                    ////System.out.println("without temp filepath: " + filepath);
                    ////System.out.println("files count: " + filecount);

                    output += filename + ": "+ compileFile(dir, filenamewithext, filename, originalFolder, ext) + "\n";
                    inputs.put("\"" + filenamewithext + "\"", fileData.getInputs());
                }

                ////System.out.println("INPUTS HASHMAP : " + inputs);

            } catch (Exception e) {

                e.printStackTrace();
            }
        } else {
            if (tempFolder.exists()) {

                String foldername = userFolderPath + "\\temp";
                File newDir = new File(foldername);

                String contents[] = newDir.list();

                StringBuilder program = new StringBuilder();

                for (int i = 0; i < contents.length; i++) {

                    ////System.out.println("native manual handle content: " + contents[i]);
                    ////System.out.println("path: " + userFolderPath + "\\temp\\" + contents[i]);

                    String newFilenameWithext = contents[i];
                    String newFilename = newFilenameWithext.substring(0, dot);

                    int j = i+1;
                    String programName = "program "+j;

                    ////System.out.println("in loop compile");
                    output += programName+" : "+ compileFile(newDir, newFilenameWithext, newFilename, foldername, ext) + "\n";
                    ////System.out.println("out loop compile");

                    String fileContents = new String(
                            Files.readAllBytes(Paths.get(userFolderPath + "\\temp\\" + contents[i])),
                            StandardCharsets.UTF_8) + "\n";

                    program.append(fileContents);
                }
                Files.write(Paths.get(originalFolder + "\\" + filenamewithext),
                program.toString().getBytes(StandardCharsets.UTF_8));
                System.out.println("deletingg.. manual");
                deleteTempFolder(tempFolder);
            } else {
                ////System.out.println("one manual file..");
                output = filename + ": "+   compileFile(dir, filenamewithext, filename, originalFolder, ext) + "\n";
            }

            // else {
            // ////System.out.println("manual files count: " + filecount);
            // output += compileFile(dir, filenamewithext, filename, folder, ext) + "\n";
            // }

        }

        return output;
    }

    public String compileFile(File newDir, String newFilenameWithext, String newFilename, String foldername, String ext)
            throws IOException {
        String compiler = "";
        String line = "";
        if (ext.equals("cpp")) {
            compiler = "g++";
        } else if (ext.equals("c")) {
            compiler = "gcc";
        }

        Process process = Runtime.getRuntime().exec(
                "cmd /C " + compiler + " " + newFilenameWithext + " -o " + newFilename,
                null, newDir);
        boolean exitValue = false;

        // --------------------------------------ERROR
        // MESSAGE----------------------------------//

        try {
            exitValue = process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (exitValue) {

            ////System.out.println("new file completed successfully");

        } else {
            line = "compile error";
            ////System.out.println("new file did not complete successfully within the timeout");
            // return line;
        }
        String output = "";

        // ----------------------------------------------------------------------------------------//

        ////System.out.println("No compile errors found.");

        try {

            ProcessBuilder run = new ProcessBuilder(foldername + "\\" + newFilename + ".exe");

            // -----------------------READING FROM C and Cpp --------------------------//
            Process runProcess = run.start();

            BufferedReader input = new BufferedReader(new InputStreamReader(runProcess.getInputStream()));

            // line = input.readLine();

            while ((line = input.readLine()) != null) {

                File exeFile = new File(foldername + "\\" + newFilename + ".exe");

                if (exeFile.exists()) {
                    ////System.out.println("file exists deleteinggggg.......");
                    exeFile.delete();

                }
                output += line + "\n";

                ////System.out.println("OUTPUT : " + output);
            }
            runProcess.destroy();
            process.destroy();
            // if (runProcess.waitFor() == 0) {
            // ////System.out.println("Successfully stopped process");
            // } else {
            // ////System.out.println("Error stopping process");
            // }
            input.close();

            // input.close();

        } catch (Exception e) {
            ////System.out.println("Create process 2 error!");
            //////System.out.println("old file: "+oldFile);
            // Files.write(Paths.get(newDir + "\\" + newFilenameWithext),
            // oldFile.getBytes(StandardCharsets.UTF_8));

            Files.write(Paths.get(newDir + "\\" + newFilenameWithext),
                            oldFile.getBytes(StandardCharsets.UTF_8));

           // e.printStackTrace();
        }

        return output;

    }

    public void deleteTempFolder(File file) {
        for (File subFile : file.listFiles()) {
            if (subFile.isDirectory()) {
                deleteTempFolder(subFile);
            } else {
                System.out.println(subFile.delete());
                ////System.out.println("deleting: " + subFile.delete());
            }
        }
        file.delete();
    }

    public LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> getInputs() {
        return this.inputs;
    }

}
