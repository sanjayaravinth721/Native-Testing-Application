package com;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class NativeHandling {
    private LinkedHashSet<String> listOfErrors = new LinkedHashSet<>();

    private LinkedHashSet<String> clearedErrors = new LinkedHashSet<>();

    public LinkedHashSet<String> getClearedErrors() {
        return this.clearedErrors;
    }

    public int checkMultiFiles(String folderPath, String itemname, String ext) {

        int fileCounter = 1;
        ////System.out.println("folderPath: " + folderPath + "itemname: " + itemname + "ext: " + ext);
        String userDirectory = "D:\\UserFiles";


        try {

            String pattern = "#include\\s*<\\s*\\w+(\\.\\w+)?\\s*>.*\\s+int\\s+main\\(\\)\\s*(\\{|[\\r\\n]+\\s*\\{)[\\s\\S]+?\\}";
            Pattern p = Pattern.compile(pattern);
            System.out.println("splitting..");

            File fileItem = new File(folderPath + "\\" + itemname);
            String fileContent = "";
            if(fileItem.exists()){
                fileContent = new String(Files.readAllBytes(Paths.get(folderPath + "\\" + itemname)));
            }
            else{
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

            ////System.out.println("match count: "+matchCount);

            while (m.find() && matchCount>1) {
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
                ////System.out.println("delete temp: "+subFile.delete());
            }
        }
        file.delete();
    }

    public LinkedHashSet<String> checkCompileErrors(String username, String filenamewithext) throws IOException {

        String userDirectory = "D:\\UserFiles";

        String folderPath = userDirectory + "\\"
                + username;

        int dot = filenamewithext.indexOf(".");
        String ext = filenamewithext.substring(dot + 1, filenamewithext.length());

        String filepath = userDirectory + "\\"
                + username + "\\"
                + filenamewithext;

        String folder = userDirectory + "\\"
                + username;

        File dir = new File(folder);

        int filecount = checkMultiFiles(folderPath, filenamewithext, ext);

        if (filecount == 1) {

            ////System.out.println("files count: " + filecount);

            listOfErrors.addAll(checkErrors(filenamewithext, filepath, dir));

        } else {

            File directoryPath = new File(folderPath + "\\temp");

            String contents[] = directoryPath.list();
            ////System.out.println("List of files and directories in the specified directory:");

            String foldername = folderPath + "\\temp";
            File newDir = new File(foldername);

            for (int i = 0; i < contents.length; i++) {
                ////System.out.println("native content: " + contents[i]);

                String newFilenameWithext = contents[i];

                filepath = userDirectory + "\\"
                        + username + "\\temp\\"
                        + contents[i];

                listOfErrors.addAll(checkErrors(newFilenameWithext, filepath, newDir));
            }

        }

        return listOfErrors;

    }

    public LinkedHashSet<String> checkErrors(String filenamewithext, String filepath, File dir) throws IOException {

        int dot = filenamewithext.indexOf(".");

        String filename = filenamewithext.substring(0, dot);
        ////System.out.println("FILENAME : " + filename);
        String ext = filenamewithext.substring(dot + 1, filenamewithext.length());
        String line = null;

        String userDirectory = "D:\\UserFiles";

        File file = new File(filepath);
        if(!file.exists()){
            filepath = userDirectory+"\\CommonFiles\\"+filenamewithext;
            ////System.out.println("filepath: "+filepath);
            dir = new File(userDirectory+"\\CommonFiles");
            ////System.out.println("dir file: "+dir);
        }

        ////System.out.println("dir out: "+dir);

        String compiler = "";
        if (ext.equals("cpp")) {
            compiler = "g++";
        } else if (ext.equals("c")) {
            compiler = "gcc";
        }

        Process process = Runtime.getRuntime().exec("cmd /C " + compiler + " " + filenamewithext + " -o " + filename,
                null, dir);
        boolean exitValue = false;

        try {
            exitValue = process.waitFor(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (exitValue) {

            ////System.out.println("native completed successfully");

        } else {
            line = "compile error";
            ////System.out.println("native did not complete successfully within the timeout");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
            String error;
            while ((error = reader.readLine()) != null) {
                ////System.out.println("error exists!");
                listOfErrors.add(error);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        ////System.out.println("NEW LIST OF ERROR : " + listOfErrors.size());
        if (!listOfErrors.isEmpty()) {
            ////System.out.println("Compile errors found:");
            for (String errorMessage : listOfErrors) {

                ////System.out.println("error: " + errorMessage);
            }
            ////System.out.println("ABOVE LIST OF ERROR : " + listOfErrors.size());
            errorCorrection(listOfErrors, filepath);

            return listOfErrors;
        } else {

            return listOfErrors;
        }
    }

    public void errorCorrection(LinkedHashSet<String> listOfErrors, String filepath) throws IOException {
        String unDeclaredErrorPattern = "(\\w+).(\\w+):(\\d+):.*'(\\w+)' undeclared.*";
        Pattern unDeclaredpattern = Pattern.compile(unDeclaredErrorPattern);

        String unDeclaredErrorPattern2 = "(.*):(\\d+):(\\d*): error: '(\\w+)' was not declared in this scope";
        Pattern unDeclaredPattern2 = Pattern.compile(unDeclaredErrorPattern2);

        String semicolonErrorPattern = ".*error: expected ',' or ';' before '(.*)'";
        Pattern semicolonPattern = Pattern.compile(semicolonErrorPattern);

        String intToStringErrorPattern = "(\\w+).(c|cpp):(\\d+):(\\d+): warning: initialization makes integer from pointer without a cast \\[-Wint-conversion]";
        Pattern intToStringPattern = Pattern.compile(intToStringErrorPattern);

        String decimalToStringErrorPattern = "(.*):(\\d+):\\d*: error: incompatible types when initializing type '(\\w+)' using type '(.*)'";
        Pattern decimalToStringPattern = Pattern.compile(decimalToStringErrorPattern);

        String decimalToStringErrorPatternCpp = "(.*):(\\d+):(\\d*): error: cannot convert '(.*)' to '(.*)' in initialization";
        Pattern decimalToStringPatternCpp = Pattern.compile(decimalToStringErrorPatternCpp);

        String intToStringErrorPatternCpp = "(.*):(\\d+):(\\d*): error: invalid conversion from '(.*)' to 'int' \\[-fpermissive\\]";
        Pattern intToStringPatternCpp = Pattern.compile(intToStringErrorPatternCpp);

        String alreadyDeclaredErrorPattern = "(\\w+)\\.(c|cpp):(\\d*):(\\d*): error: redeclaration of '(.*(\\w+))'";
        Pattern alreadyDeclaredPattern = Pattern.compile(alreadyDeclaredErrorPattern);

        String redefinitionDeclaredErrorPattern = "(\\w+).(c|cpp):(\\d*):(\\d*): error: redefinition of '(\\w+)'";
        Pattern redefinitionDeclaredPattern = Pattern.compile(redefinitionDeclaredErrorPattern);

        String stringToNumberErrorPattern = "(.*):(\\d+):(\\d*): error: conversion from '(.*)' to (.*) '(.*)basic_string<char>}' (.*)";
        Pattern stringToNumberPattern = Pattern.compile(stringToNumberErrorPattern);

        String unknownTypeErrorPattern = "(\\w+).(c|cpp):(\\d*):(\\d*): error: unknown type name '(.*)'";
        Pattern unknownTypePattern = Pattern.compile(unknownTypeErrorPattern);

        String streamErrorPattern = "(\\w+).(c|cpp):(\\d*):(\\d*): error: no match for 'operator(<<|>>)' .*";
        Pattern streamPattern = Pattern.compile(streamErrorPattern);

        boolean isDeclared = false;

        for (String errorMessage : listOfErrors) {

            Matcher unDeclaredMatcher = unDeclaredpattern.matcher(errorMessage);
            Matcher unDeclaredMatcher2 = unDeclaredPattern2.matcher(errorMessage);

            Matcher semicolonMatcher = semicolonPattern.matcher(errorMessage);

            Matcher intToStringMatcher = intToStringPattern.matcher(errorMessage);
            Matcher decimalToStringMatcher = decimalToStringPattern.matcher(errorMessage);
            Matcher intToStringMatcherCpp = intToStringPatternCpp.matcher(errorMessage);
            Matcher decimalToStringMatcherCpp = decimalToStringPatternCpp.matcher(errorMessage);

            Matcher StringToNumberMatcherCpp = stringToNumberPattern.matcher(errorMessage);

            Matcher alreadyDeclaredMatcher = alreadyDeclaredPattern.matcher(errorMessage);
            Matcher redefinitionDeclaredMatcher = redefinitionDeclaredPattern.matcher(errorMessage);

            Matcher unknownTypeMatcher = unknownTypePattern.matcher(errorMessage);

            Matcher streamMatcher = streamPattern.matcher(errorMessage);

            if (unDeclaredMatcher.matches()) {
                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(unDeclaredMatcher.group(3));
                String variableName = unDeclaredMatcher.group(4);

                addDeclaration(variableName, filepath, lineNumber, "c");

            } else if (unDeclaredMatcher2.matches()) {
                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(unDeclaredMatcher2.group(2));
                String variableName = unDeclaredMatcher2.group(4);

                addDeclaration(variableName, filepath, lineNumber, "cpp");

            } else if (semicolonMatcher.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                checkSemicolon(filepath);
            } else if (intToStringMatcher.matches() && !isDeclared) {

                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(intToStringMatcher.group(3));
                castCorrection(filepath, lineNumber);

                clearedErrors.add(
                        "\"cleared : int datatype initialized with integer or ascii value at lineNumber " + lineNumber+"\"");
            } else if (intToStringMatcherCpp.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(intToStringMatcherCpp.group(2));
                castCorrection(filepath, lineNumber);

                clearedErrors
                        .add("\"cleared : int datatype initialized with 0 or ascii value at lineNumber " + lineNumber+"\"");
            } else if (decimalToStringMatcher.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");
                int lineNumber = Integer.parseInt(decimalToStringMatcher.group(2));
                castCorrection(filepath, lineNumber);

                clearedErrors.add("\"cleared : decimals datatype initialized with 0 at lineNumber " + lineNumber+"\"");
            } else if (decimalToStringMatcherCpp.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(decimalToStringMatcherCpp.group(2));

                castCorrection(filepath, lineNumber);

                clearedErrors.add("\"cleared : decimals datatype initialized with 0 at lineNumber " + lineNumber+"\"");
            } else if (StringToNumberMatcherCpp.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");
                int lineNumber = Integer.parseInt(StringToNumberMatcherCpp.group(2));
                castCorrection(filepath, lineNumber);

            } else if (alreadyDeclaredMatcher.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(alreadyDeclaredMatcher.group(3));
                String variableName = alreadyDeclaredMatcher.group(6);

                alreadyDeclared(filepath, lineNumber, variableName);
                clearedErrors.add("\"cleared: redeclared " + variableName + " at lineNumber " + lineNumber+"\"");
            } else if (redefinitionDeclaredMatcher.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                int lineNumber = Integer.parseInt(redefinitionDeclaredMatcher.group(3));
                String variableName = redefinitionDeclaredMatcher.group(5);
                alreadyDeclared(filepath, lineNumber, variableName);

                clearedErrors.add("\"cleared: redefined " + variableName + " at lineNumber " + lineNumber+"\"");
            } else if (unknownTypeMatcher.matches()) {

                clearedErrors.add("\""+errorMessage+"\"");

                String type = unknownTypeMatcher.group(2);

                int lineNumber = Integer.parseInt(unknownTypeMatcher.group(3));

                String variableName = "";

                addDeclaration(variableName, filepath, lineNumber, type);

                isDeclared = true;
            } else if (streamMatcher.matches()) {

                int lineNumber = Integer.parseInt(streamMatcher.group(3));
                String operator = streamMatcher.group(5);

                clearedErrors.add("\""+errorMessage+"\"");
                streamCheck(lineNumber, filepath, operator);

            }

        }

    }

    public void alreadyDeclared(String filepath, int errorline, String variableName) {
        Scanner lines;
        String newFile = "";
        try {
            int lineNumber = 0;
            lines = new Scanner(new File(filepath));
            while (lines.hasNextLine()) {
                lineNumber++;
                String line = lines.nextLine();
                if (lineNumber == errorline) {

                    line = line.trim().replaceFirst("\\w+\\s+", "");

                }
                newFile += line + "\n";
            }
            Files.write(Paths.get(filepath), newFile.getBytes());
            lines.close();
        } catch (Exception e) {
            ////System.out.println(e);
        }
    }

    public void addDeclaration(String variableName, String filepath, int errorline, String type) {
        Scanner lines;
        String newFile = "";
        try {
            lines = new Scanner(new File(filepath));
            int lineNumber = 0;
            String declaredWithoutTypeErrorPattern = "\\s*(\\w+)\\s*=\\s*(\".*\"|[^;]+);\\s*";
            Pattern declaredWithoutTypePattern = Pattern.compile(declaredWithoutTypeErrorPattern);

            String declaredWithTypeErrorPattern = "\\s*(\\w+)\\s+(\\w+)\\s*=\\s*(.*);";
            Pattern declaredWithTypePattern = Pattern.compile(declaredWithTypeErrorPattern);

            while (lines.hasNextLine()) {
                lineNumber++;
                String line = lines.nextLine();

                if (lineNumber == errorline && !(line.contains("<<")) && !(line.contains(">>"))) {
                    Matcher declaredWithoutTypeMatcher = declaredWithoutTypePattern.matcher(line);
                    Matcher declaredWithTypeMatcher = declaredWithTypePattern.matcher(line);
                    String datatype = "";

                    if (declaredWithoutTypeMatcher.matches()) {
                        String value = declaredWithoutTypeMatcher.group(2);

                        try {
                            datatype = "int ";
                            line = datatype + line;

                        } catch (NumberFormatException e) {
                            try {

                                datatype = "double ";
                                line = datatype + line;

                            } catch (NumberFormatException e2) {
                                if (value.contains("\'") && value.contains("\'")
                                        || value.contains("\"") && value.contains("\"")) {
                                    if (value.replace("\'", "").length() == 1
                                            || value.replace("\"", "").length() == 1) {

                                        datatype = "char ";
                                        line = datatype + line;

                                    } else {

                                        if (type == "c") {
                                            datatype = "char* ";
                                            line = datatype + line;
                                        } else {
                                            datatype = "string ";
                                            line = datatype + line;
                                        }
                                    }

                                }

                            }
                        }
                        if (datatype != "") {
                            String errorCleared = variableName + " is replaced with type " + datatype;
                            clearedErrors.add("\"cleared: " + errorCleared+"\"");
                        } else {
                            clearedErrors.add("\"cleared: " + variableName + " already declared!\"");
                        }

                    } else if (declaredWithTypeMatcher.matches()) {

                        String value = declaredWithTypeMatcher.group(3);
                        String unknown = declaredWithTypeMatcher.group(1);

                        datatype = "";
                        try {

                            datatype = "int";
                            line = line.replace(unknown, datatype);

                        } catch (NumberFormatException e) {
                            try {

                                datatype = "double";
                                line = line.replace(unknown, datatype);

                            } catch (NumberFormatException e2) {
                                if (value.contains("\'") && value.contains("\'")
                                        || value.contains("\"") && value.contains("\"")) {
                                    if (value.replace("\'", "").length() == 1
                                            || value.replace("\"", "").length() == 1) {

                                        datatype = "char";
                                        line = line.replace(unknown, datatype);
                                    } else {

                                        if (type.equals("c")) {
                                            datatype = "char*";
                                            line = line.replace(unknown, datatype);
                                        } else {
                                            datatype = "string";
                                            line = line.replace(unknown, datatype);
                                        }

                                    }
                                }
                            }
                        }
                    }

                    if (datatype != "") {
                        String errorCleared = variableName + " is replaced with type " + datatype;
                        clearedErrors.add("\"cleared: " + errorCleared+"\"");
                    } else {
                        clearedErrors.add("\"cleared: " + variableName + " already declared!\"");
                    }

                    newFile += line + "\n";

                } else {
                    newFile += line + "\n";
                }
            }
            lines.close();
        } catch (

        FileNotFoundException e) {

            e.printStackTrace();
        }
        try {
            Files.write(Paths.get(filepath), newFile.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void castCorrection(String filepath, int errorline) throws IOException {
        Scanner lines;
        String newFile = "";

        try {
            int lineNumber = 0;
            lines = new Scanner(new File(filepath));
            while (lines.hasNextLine()) {
                lineNumber++;
                String line = lines.nextLine();

                if (lineNumber == errorline) {
                    ////System.out.println("nextline cast: " + line);
                    int squareQuoteStart = line.indexOf("\"");
                    int squareQuoteEnd = line.lastIndexOf("\"");
                    int equalIndex = line.indexOf("=");

                    if (equalIndex != -1) {

                        if (squareQuoteStart != -1 && squareQuoteEnd != -1) {
                            String content = line.substring(squareQuoteStart + 1, squareQuoteEnd);

                            if (equalIndex < squareQuoteStart) {

                                if (squareQuoteStart + 1 == squareQuoteEnd) {
                                    line = line.replace("\"" + content + "\"", "0");

                                } else if (content.length() > 1) {
                                    line = line.replace("\"" + content + "\"", "0");

                                } else {
                                    line = line.replace("\"", "\'");

                                }
                            }

                        } else {

                            Pattern pattern = Pattern.compile("(.*)\\s*\\w+\\s*\\w+\\s*=\\s*(\\d+)(\\.\\d+)?\\s*;");
                            Matcher matcher = pattern.matcher(line);

                            if (matcher.find()) {
                                String value = matcher.group(2) + (matcher.group(3) == null ? "" : matcher.group(3));
                                ////System.out.println("value of string: " + value);
                                line = line.replace(value, "\"" + value + "\"");
                            }
                            clearedErrors.add("\"cleared: Value is replaced with string at Line Number: " + lineNumber+"\"");

                        }
                    }

                }
                newFile += line + "\n";
            }
            lines.close();
            Files.write(Paths.get(filepath), newFile.getBytes());
        } catch (

        Exception e) {
            ////System.out.println(e);
        }
    }

    public void streamCheck(int errorline, String filepath, String operator) {
        Scanner lines;
        String newFile = "";
        try {
            int lineNumber = 0;
            lines = new Scanner(new File(filepath));
            while (lines.hasNextLine()) {
                lineNumber++;
                String newOperator = "";
                String line = lines.nextLine();
                if (lineNumber == errorline) {

                    if (operator.equals("<<")) {
                        newOperator = ">>";
                    } else if (operator.equals(">>")) {
                        newOperator = "<<";
                    }

                    if (!newOperator.equals("")) {
                        line = line.replace(operator, newOperator);
                        clearedErrors.add("\"cleared : Operator changed from " + operator + " to " + newOperator+"\"");
                    }
                }

                newFile += line + "\n";
            }
            lines.close();
            Files.write(Paths.get(filepath), newFile.getBytes());
        } catch (Exception e) {
            ////System.out.println(e);
        }
    }

    public void checkSemicolon(String filepath) throws IOException {
        Scanner edit = new Scanner(new File(filepath));

        StringBuffer editBuffer = new StringBuffer();

        Pattern initialPattern = Pattern.compile("^int\\s+main\\s*\\(\\s*\\)\\s*\\{?$");
        Pattern headerPattern = Pattern.compile("^\\s*#\\s*include\\s*<[\\w.]+>\\s*");
        List<String> errorLines = new ArrayList<>();
        int lineNumber = 1;
        String syntaxCheck = "^((?!for|while|else|if).)*$";
        Pattern syntaxPattern = Pattern.compile(syntaxCheck);
        String semicolonsAdded = "cleared : Added semicolon at the end at lineNumber: ";
        boolean semicolonNotPresent = false;

        while (edit.hasNextLine()) {
            String line = edit.nextLine();
            String trimLine = line.trim();

            int linelength = trimLine.length();
            Matcher syntaxMatcher = syntaxPattern.matcher(line);

            if ((syntaxMatcher.matches()) && !(headerPattern.matcher(trimLine).matches())
                    && !(initialPattern.matcher(trimLine).matches()) && (linelength != 0) && (linelength != 1)
                    && !(line.contains(";"))) {
                String error = line + " \"; not present\"";
                errorLines.add(error);
                line += ";";
                semicolonsAdded += lineNumber + " ";
                semicolonNotPresent = true;

            }

            editBuffer.append(line + System.lineSeparator());
            lineNumber++;
        }

        for (String e : errorLines) {
            ////System.out.println(e);
        }
        String editedfileContents = editBuffer.toString();

        FileWriter editedwriter = new FileWriter(filepath);

        if (semicolonNotPresent) {
            clearedErrors.add("\""+semicolonsAdded+"\"");
        }

        editedwriter.append(editedfileContents);
        edit.close();
        editedwriter.flush();
        editedwriter.close();
    }

}
