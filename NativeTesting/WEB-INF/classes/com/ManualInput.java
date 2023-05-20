package com;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ManualInput extends HttpServlet {

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ////System.out.println("in manual servlet");
        String filenamewithext = (String) request.getParameter("filename");
        ////System.out.println("file: " + filenamewithext);
        String output = "";
        HttpSession session = request.getSession();
        String username = (String) session.getAttribute("username");
        LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> inputs = (LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>>) session
                .getAttribute("manualInput");

        String[] values = request.getParameterValues("values");
        ArrayList<String> myListValues = new ArrayList<>(Arrays.asList(values));
        ////System.out.println("values: " + values);

        String userDirectory = "D:\\UserFiles";

        File tempFolder = new File(userDirectory + "\\" + username + "\\temp");

        ////System.out.println("tempFolder: " + tempFolder);

        String filepath = userDirectory + "\\"
                + username + "\\"
                + filenamewithext;

        File file = new File(filepath);
        if (!file.exists()) {
            filepath = userDirectory + "\\CommonFiles\\"
                    + filenamewithext;
        }

        int i = 0;
        String excludedKeywordPattern = "(for|while|if|else)";
        Pattern pattern = Pattern.compile("(?i).*" + excludedKeywordPattern + ".*");

        if (tempFolder.exists()) {

            String[] contents = tempFolder.list();

            int j = 0;

            while (j < contents.length) {
                ////System.out.println("myListValues length : " + myListValues.size());
                filepath = userDirectory + "\\" + username + "\\temp\\" + contents[j];
                replaceFile(filepath, myListValues, inputs.get("\"" + contents[j] + "\""));
                ////System.out.println("j count : " + j);
                j++;
            }
        } else {
            ////System.out.println("filename: " + filenamewithext);
            ////System.out.println("inputs : " + inputs);
            replaceFile(filepath, myListValues, inputs.get("\"" + filenamewithext + "\""));
        }

    }

    public void replaceFile(String filepath, ArrayList<String> values,
            LinkedHashMap<String, LinkedList<String>> inputs) {
        int count = 0;

        ////System.out.println("replacefile: " + inputs);
        ////System.out.println("replace input length: " + values.size());
        LinkedHashSet<Integer> modifiedLines = new LinkedHashSet<Integer>();

        for (String val : values) {
            boolean flag = false;

            for (Entry<String, LinkedList<String>> data : inputs.entrySet()) {

                ////System.out.println("Variable name : " + data.getKey());
                int valueLength = data.getValue().size() - 1;

                String value = data.getValue().get(valueLength).replace("\"", "");
                String datatype = data.getValue().get(0).replace("\"", "");

                String excludedKeywordPattern = "^(for|while|if|else)";
                Pattern pattern = Pattern.compile(excludedKeywordPattern);

                String excludedDatatypePattern = "\\s*(int|float|double|char|string)\\s*.*";
                Pattern datatypePattern = Pattern.compile(excludedDatatypePattern);

                ////System.out.println("value : " + value);
                ////System.out.println("key: " + data.getKey());
                String key = data.getKey().replace("\"", "");

                Scanner lines;
                String newFile = "";

                try {
                    int lineNumber = 0;
                    lines = new Scanner(new File(filepath));
                    while (lines.hasNextLine()) {
                        lineNumber++;
                        String line = lines.nextLine();

                        if (modifiedLines.contains(lineNumber)) {
                            newFile += line + "\n";
                            continue;
                        }

                        if (flag == true) {
                            newFile += line + "\n";
                            continue;
                        }

                        Matcher datatypeMatcher = datatypePattern.matcher(line);

                        String temp = line.trim();
                        ////System.out.println("temp trim: " + temp);

                        if (temp.indexOf("//") == 0) {
                            newFile += line + "\n";
                            continue;
                        }
                        Matcher matcher = pattern.matcher(temp);

                        if (temp.contains("cin")) {
                            ////System.out.println("line cin: " + line);
                            String cinRegex = "\\s*cin\\s*>>\\s*(\\w+\\d*)\\s*;";
                            Pattern cinPattern = Pattern.compile(cinRegex);
                            Matcher cinMatcher = cinPattern.matcher(line);

                            if (cinMatcher.matches()) {
                                String cinMatchString = cinMatcher.group(1);
                                ////System.out.println("cinMatchString : " + cinMatchString);
                                ////System.out.println("keyMatchString : " + key);

                                if (key.equals(cinMatchString)) {
                                    ////System.out.println("cin match str equal : " + key);
                                    if (datatype.equals("string")) {
                                        newFile += cinMatchString + " = \"" + val + "\";\n";
                                        
                                    } else if (datatype.equals("char")) {
                                        newFile += cinMatchString + " = \'" + val + "\'';\n";
                                    } else {
                                        newFile += cinMatchString + " = " + val + ";\n";
                                    }
                                    ////System.out.println("newfile data cin: "+newFile);
                                    flag = true;
                                }
                               
                            }

                        }

                        // if (flag == true) {
                        // break;
                        // }

                        else if (temp.contains("scanf")) {
                            Pattern scanfPattern = Pattern.compile("&\\s*(\\w+)\\s*");
                            Matcher scanfMatcher = scanfPattern.matcher(temp);
                            List<String> variables = new ArrayList<String>();
                            ////System.out.println("scanf present");

                            while (scanfMatcher.find()) {

                                String variable = scanfMatcher.group(1);
                                ////System.out.println("scanf variable: " + scanfMatcher.group(1));

                                if (variable.equals(key)) {
                                    if (datatype.equals("string")) {
                                        newFile += variable + " = \"" + val + "\";\n";
                                    } else if (datatype.equals("char")) {
                                        newFile += variable + " = \'" + val + "\'';\n";
                                    } else {
                                        newFile += variable + " = " + val + ";\n";
                                    }

                                    ////System.out.println("new file scanf: " + newFile);
                                    flag = true;

                                }

                                if (flag == true) {
                                    break;
                                }
                            }
                        } else {
                          
                            if (datatypeMatcher.matches()) {

                                int datatypeIndex = temp.indexOf(" ");
                                ////System.out.println("temp line data before: " + temp);
                                temp = temp.substring(datatypeIndex + 1, temp.length());
                                ////System.out.println("temp line data: " + temp);
                            }

                            ////System.out.println("temp data: " + temp + " value: " + value + " key: " + key);

                            if (temp.contains(value) && !(value.equals(""))
                                    && !(value.equals("cin")) && !(value.equals("scanf"))
                                    && (temp.contains(key))
                                    && !(matcher.find())) {
                                flag = true;
                                ////System.out.println("replace value: " + value);
                                ////System.out.println("replace input val : " + val);
                                line = line.replace(value, val);
                                ////System.out.println("replaced line: " + line);

                            }
                            newFile += line + "\n";

                           
                        }

                        if (flag == true) {
                            modifiedLines.add(lineNumber);
                            ////System.out.println("modified lines: " + lineNumber);
                            count++;
                        }

                    }
                    Files.write(Paths.get(filepath), newFile.getBytes());
                    lines.close();

                } catch (Exception e) {
                    ////System.out.println(e);
                }
                if (flag == true) {
                    inputs.remove(data.getKey());
                    ////System.out.println("input length: " + inputs);
                    break;
                }

            }
        }
        ////System.out.println("counttt: " + count);

        int indexToDelete = 0;
        int numElementsToDelete = count;

        // ArrayList<Integer> myList = new ArrayList<>(Arrays.asList(values));

        values.subList(indexToDelete, indexToDelete + numElementsToDelete).clear();

        ////System.out.println("values fn length : " + values.size());

        ////System.out.println("old map: " + inputs);

        String cppFile = "";
        ////System.out.println("filepath: " + filepath);

        try {
            cppFile = new String(Files.readAllBytes(Paths.get(filepath)));
        } catch (IOException e) {
            e.printStackTrace();
        }

        ////System.out.println("----------------------------CPPFILE------------------------------");
        ////System.out.println(cppFile);
        ////System.out.println("----------------------------CPP FILE------------------------------");
    }

}

// public void replaceFile(String filepath, ArrayList<String> values,
// LinkedHashMap<String, LinkedHashMap<String, LinkedList<String>>> inputs) {

// int count = 0;

// for (String val : values) {
// boolean flag = false;

// ////System.out.println("val: " + val);

// for (Map.Entry<String, LinkedHashMap<String, LinkedList<String>>> map :
// inputs.entrySet()) {

// ////System.out.println("Manual Program name : " + map.getKey());

// LinkedHashMap<String, LinkedList<String>> datas = map.getValue();

// for (Entry<String, LinkedList<String>> data : datas.entrySet()) {

// ////System.out.println("Variable name : " + data.getKey());
// int valueLength = data.getValue().size() - 1;

// String value = data.getValue().get(valueLength).replace("\"", "");
// String datatype = data.getValue().get(0).replace("\"", "");

// String excludedKeywordPattern = "^(for|while|if|else)";
// Pattern pattern = Pattern.compile(excludedKeywordPattern);

// String excludedDatatypePattern = "\\s*(int|float|double|char|string)\\s*.*";
// Pattern datatypePattern = Pattern.compile(excludedDatatypePattern);

// ////System.out.println("value : " + value);
// ////System.out.println("key: " + data.getKey());
// String key = data.getKey().replace("\"", "");

// Scanner lines;
// String newFile = "";
// try {
// int lineNumber = 0;
// lines = new Scanner(new File(filepath));
// while (lines.hasNextLine()) {
// lineNumber++;
// String line = lines.nextLine();

// if(flag == true){
// newFile += line + "\n";
// continue;
// }

// Matcher datatypeMatcher = datatypePattern.matcher(line);

// String temp = line.trim();

// if (temp.indexOf("//") == 0) {
// newFile += line + "\n";
// continue;
// }
// Matcher matcher = pattern.matcher(temp);

// if (temp.contains("cin")) {
// temp = temp.replace("cin", "");
// for (String in : temp.split(">>")) {
// ////System.out.println("var >> : " + in + "h");
// if (!(in.equals(""))) {
// for (Entry<String, LinkedList<String>> cinVar : datas.entrySet()) {
// ////System.out.println("cin in : " + in);
// String cinVariable = cinVar.getKey().replace("\"", "");
// if (in.contains(cinVariable)) {
// newFile += cinVariable + " = " + val + ";\n";
// flag = true;
// count++;
// }
// }
// }
// }
// } else if (temp.contains("scanf")) {
// Pattern scanfPattern = Pattern.compile("&\\s*(\\w+)\\s*");
// Matcher scanfMatcher = scanfPattern.matcher(temp);
// List<String> variables = new ArrayList<String>();
// ////System.out.println("scanf present");

// while (scanfMatcher.find()) {

// String variable = scanfMatcher.group(1);
// ////System.out.println("scanf variable: " + scanfMatcher.group(1));
// for (Entry<String, LinkedList<String>> cinVar : datas.entrySet()) {
// String scanfVariable = cinVar.getKey().replace("\"", "");
// ////System.out.println("scanf variable key: " +scanfVariable);
// if (variable.contains(scanfVariable)) {
// newFile += variable + " = " + val + ";\n";
// ////System.out.println("new file scanf: "+newFile);
// flag = true;
// count++;
// }

// }
// }
// } else {

// if (datatypeMatcher.matches()) {

// int datatypeIndex = temp.indexOf(" ");
// ////System.out.println("temp line data before: " + temp);
// temp = temp.substring(datatypeIndex + 1, temp.length());
// ////System.out.println("temp line data: " + temp);
// }

// if (temp.contains(value) && !(value.equals("")) && !(value.equals("cin"))
// && (temp.contains(key))
// && !(matcher.find())) {

// flag = true;
// ////System.out.println("replace value: " + value);
// ////System.out.println("replace input val : " + val);
// line = line.replace(value, val);
// ////System.out.println("replaced line: " + line);
// count++;
// }
// newFile += line + "\n";
// }

// }
// Files.write(Paths.get(filepath), newFile.getBytes());
// lines.close();
// if (flag == true) {
// break;
// }
// } catch (Exception e) {
// ////System.out.println(e);
// }

// }
// }

// }

// ////System.out.println("counttt: " + count);

// int indexToDelete = 0;
// int numElementsToDelete = count;

// // ArrayList<Integer> myList = new ArrayList<>(Arrays.asList(values));

// values.subList(indexToDelete, indexToDelete + numElementsToDelete).clear();

// ////System.out.println("values fn length : " + values.size());

// ////System.out.println("old map: " + inputs);

// String cppFile = "";
// ////System.out.println("filepath: " + filepath);

// try {
// cppFile = new String(Files.readAllBytes(Paths.get(filepath)));
// } catch (IOException e) {
// e.printStackTrace();
// }

// ////System.out.println("----------------------------CPP
// FILE------------------------------");
// ////System.out.println(cppFile);
// ////System.out.println("----------------------------CPP
// FILE------------------------------");
// }
