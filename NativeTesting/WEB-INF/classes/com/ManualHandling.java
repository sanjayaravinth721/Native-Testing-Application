package com;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.*;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyStore.Entry;
import java.sql.Time;

public class ManualHandling {
    private LinkedHashMap<String, LinkedList<String>> datas = new LinkedHashMap<>();

    public LinkedHashMap<String, LinkedList<String>> getInputs() {
        return datas;
    }

    public void file(String filename) throws Exception {
        ////System.out.println("in manual handling....");

        String filePath = filename;
        Scanner sc = new Scanner(new File(filePath));
        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        int min = 5;
        int max = 10;

        datas.clear();

        File file = new File(filePath);
        String myfile = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        ////System.out.println("---------------MY FILE---------------");
        ////System.out.println(myfile);
        Pattern commoPattern = Pattern.compile("(\\s*)((\\w+)\\s+)(\\w+)(\\s*)(=)?(\\s*.*?)[;]");

        removeComma(filePath, commoPattern);

        String newFile = "";
        try {
            newFile = new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (

        IOException e) {

            e.printStackTrace();
        }

        // ---------------------------------------------------------//

        // String numberArrayString =
        // "\\s*(int|float|double)\\s+\\w+\\s*\\[\\s*(\\d+\\s*)?\\]\\s*=\\s*\\{(\\s*[+-]?\\d+(\\.\\d+)?(\\s*,\\s*[+-]?\\d+(\\.\\d+)?)*)?\\s*\\}\\s*;";
        // Pattern numberArrayPattern = Pattern.compile(numberArrayString);
        String regex = "\\s*(int|float|double)\\s+\\w+(\\s*\\[\\s*(\\d+\\s*)?\\])?(\\s*=\\s*[+-]?\\d+(\\.\\d+)?\\s*)*(\\s*,\\s*\\w+(\\s*\\[\\s*(\\d+\\s*)?\\])?(\\s*=\\s*[+-]?\\d+(\\.\\d+)?\\s*)*)*\\s*;$";
        Pattern numberDatatypePattern = Pattern.compile(regex);

        String initializedPatternString = "(\\s*)(int|double|float|long)(.*)=\\s*(\".*\"|[^;]+);\\s*";
        Pattern initializedPattern = Pattern.compile(initializedPatternString);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String temp = "";
            String num = "0";

            if (line.trim().indexOf("//") == 0 || line.trim().indexOf("return") == 0) {
                ////System.out.println("command: " + line);
                buffer.append(line + System.lineSeparator());
                continue;
            }

            // Matcher numberArrayMatcher = numberArrayPattern.matcher(line);
            Matcher numberDatatypeMatcher = numberDatatypePattern.matcher(line);
            Matcher initializedMatcher = initializedPattern.matcher(line);

            // ////System.out.println("lines manual: "+line+"matches :
            // "+numberArrayMatcher.matches());
            ////System.out.println("lines: " + line + "matches : " + numberDatatypeMatcher.matches());

            String excludedKeywordString = "^(for|while|if|else)";
            Pattern excludedKeywordPattern = Pattern.compile(excludedKeywordString);
            Matcher excludedKeywordMatcher = excludedKeywordPattern.matcher(line.trim());

            if (numberDatatypeMatcher.matches()
                    || (line.contains("{") && line.contains("}")) && !(excludedKeywordMatcher.find())) {

                ////System.out.println("DATATYPES...");

                Pattern datatypePattern = Pattern.compile("(\\s*)(int|double|float|long)(.*)");
                Matcher datatypeMatcher = datatypePattern.matcher(line);

                if (datatypeMatcher.find()) {
                    ////System.out.println("line.." + line);
                    String matchedDatatype = datatypeMatcher.group(2);
                    ////System.out.println("Matched datatype: " + matchedDatatype);

                    if (line.indexOf("=") != -1) {

                        temp = line.substring(line.indexOf("="), line.indexOf(";"));
                        if (!(temp.contains("[")) && !(temp.contains("]"))) {

                            String isArray = "(?i)^.*\\[.*\\].*\\{.*\\}.*(?!sizeof).*$";
                            Pattern isArrayPattern = Pattern.compile(isArray);
                            Matcher isArrayPatternMatcher = isArrayPattern.matcher(line);

                            if (isArrayPatternMatcher.matches()) {

                                if (datatypeMatcher.matches()) {
                                    addArray(line, temp, matchedDatatype + "Array");
                                    temp = line.replace(matchedDatatype, "");
                                    temp = temp.replaceAll("\\s", "");
                                }

                            } else {

                                temp = line.replace(matchedDatatype, "");
                                temp = temp.replaceAll("\\s", "");
                                ////System.out.println("temp number: " + temp);
                                Matcher m = pattern.matcher(temp);

                                num = "";
                                if (m.find()) {
                                    num = m.group();
                                }

                                addVariables(temp, num, matchedDatatype);

                            }
                        }
                    } else {
                        temp = line.replace(matchedDatatype, "").trim();
                        ////System.out.println("temp: " + temp);

                        if (temp.contains("[") && temp.contains("]")) {
                            addEmptyArray(line, temp);

                            temp = line.substring(line.indexOf("="), line.indexOf(";"));

                            addArray(line, temp, matchedDatatype + "Array");

                        } else {
                            temp = temp.replaceAll("\\s", "");
                            addVariables(temp, "0", matchedDatatype);
                        }
                    }
                }
            } else if (initializedMatcher.matches()) {

                ////System.out.println("not present");
                String matchedVariable = "\"" + initializedMatcher.group(3).trim() + "\"";
                String matchedDatatype = "\"" + initializedMatcher.group(2) + "\"";

                ////System.out.println(matchedVariable + " initialized.");
                if (!datas.containsKey(matchedVariable)) {

                    LinkedList<String> newString = new LinkedList<>();
                    newString.add(matchedDatatype);
                    newString.add("\"\"");
                    datas.put(matchedVariable, newString);
                }
            }

            // ---------------------------character
            // datatype----------------------------------------//

            else if (line.contains("char")) {

                Pattern numPattern = Pattern.compile("[0-9]+");
                Matcher m = numPattern.matcher(line);

                if (m.find()) {
                    if (!m.group().equals("0")) {
                        ////System.out.println("IT IS CHAR []!");
                    }
                }

                singleQuoteCharacter(line, temp);

                temp = line.replace("char", "").trim();

                if (temp.indexOf("[") != -1) {
                    temp = temp.substring(0, temp.indexOf("["));
                } else if (temp.indexOf("*") != -1) {
                    temp = temp.substring(0, temp.indexOf(";"));
                }

                doubleQuoteCharacter(line, temp);
            }

            // --------------------------string
            // datatype----------------------------------------//

            if (line.contains("string")) {
                temp = line.replace("string", "");
                temp = temp.trim();
                ////System.out.println("temp: " + temp);

                int equalIndex = temp.indexOf("=");
                if (equalIndex != -1) {
                    temp = temp.substring(0, equalIndex);
                }

                checkDoubleQuoteString(line, temp, equalIndex);
                ////System.out.println("line from string: " + line);
            }

            // --------------------------cin----------------------------------------//

            if (line.contains("cin")) {
                line = addCin(line, temp);
            }

            // --------------------------scanf----------------------------------------//

            if (line.contains("scanf")) {
                line = addScanf(line, temp);
            }

            // if (line.contains("scanf")) {

            // line = replaceScanf(line, temp, max, min);

            // }

            // ------------------Add If Already Exists---------------------------//

            line = replaceVariables(line, max, min);

            // -------------------------------------------------------------------//

            buffer.append(line + System.lineSeparator());

        }

        String fileContents = buffer.toString();
        ////System.out.println("data map: " + datas);

        for (Map.Entry<String, LinkedList<String>> entry : datas.entrySet()) {
            for (String list : entry.getValue()) {
                ////System.out.println("manual var : " + list + " key : " + entry.getKey());
            }
        }
        sc.close();

        FileWriter writer = new FileWriter(filePath);
        writer.append(fileContents);
        writer.flush();
        writer.close();
    }

    public void addArray(String line, String temp, String type) {

        ////System.out.println("IT IS " + type + " [] ARRAY!");
        int openSquareBracket = line.indexOf("[");
        int closeSquareBracket = line.indexOf("]");

        if (openSquareBracket != -1 || closeSquareBracket != -1) {

            String arraysLeftSub = line.substring(0, openSquareBracket).trim();
            String arrayVar = arraysLeftSub.split(" ")[1];

            temp = line.substring(openSquareBracket + 1, closeSquareBracket).trim();

            int curlyStart = line.indexOf("{");
            int curlyEnd = line.indexOf("}");

            if (curlyStart != -1 && curlyEnd != -1) {
                temp = line.substring(curlyStart, curlyEnd + 1);
                String oldarray = line.substring(curlyStart + 1, curlyEnd);

                ////System.out.println("oldarray: " + oldarray);

                if (!datas.containsKey("\"" + arrayVar + "\"")) {
                    datas.put("\"" + arrayVar + "\"", new LinkedList<>());
                    datas.get("\"" + arrayVar + "\"").add("\"" + type + "\"");
                    datas.get("\"" + arrayVar + "\"").add("\"" + oldarray + "\"");
                }

            }
        }

    }

    public void doubleQuoteCharacter(String line, String temp) {
        int openSquareString = temp.indexOf("[");
        int closeSquareString = temp.indexOf("]");

        int quoteStart = line.indexOf("\"");
        int quoteEnd = line.lastIndexOf("\"");

        if (openSquareString != -1 && closeSquareString != -1) {

            temp = temp.substring(0, temp.indexOf("["));
        } else if (temp.indexOf("*") != -1) {
            String charPointer = "\\*(\\w+)(\\d+)?";
            Pattern charPointerPattern = Pattern.compile(charPointer);
            Matcher charPointerMatcher = charPointerPattern.matcher(temp);

            if (charPointerMatcher.matches()) {
                String charStr = charPointerMatcher.group(1);

                if (!datas.containsKey("\"" + charStr + "\"")) {
                    datas.put("\"" + charStr + "\"", new LinkedList<>());
                    datas.get("\"" + charStr + "\"").add("\"string\"");
                    datas.get("\"" + charStr + "\"").add("\"0\"");
                }
            }
        }

        if (quoteStart != -1 && quoteEnd != -1) {
            String oldString = line.substring(quoteStart + 1, quoteEnd);
            ////System.out.println("oldstring: " + oldString);

            if (!datas.containsKey("\"" + temp + "\"")) {
                datas.put("\"" + temp + "\"", new LinkedList<>());
                datas.get("\"" + temp + "\"").add("\"string\"");
                datas.get("\"" + temp + "\"").add("\"" + oldString.trim() + "\"");
            } else {
                datas.get("\"" + temp + "\"").add("\"" + oldString.trim() + "\"");
            }
            temp = "";
        }

    }

    public void singleQuoteCharacter(String line, String temp) {
        int singleQuoteStart = line.indexOf("\'");
        int singleQuoteEnd = line.lastIndexOf("\'");

        if (singleQuoteStart != -1 && singleQuoteEnd != -1) {

            String character = line.substring(singleQuoteStart + 1, singleQuoteEnd);

            temp = line.replace("char", "");
            temp = temp.trim();
            int equalIndexes = temp.indexOf("=");

            if (equalIndexes != -1) {

                temp = temp.substring(0, equalIndexes).trim();
                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp + "\"", new LinkedList<>());
                    datas.get("\"" + temp + "\"").add("\"char\"");
                    datas.get("\"" + temp + "\"").add("\"" + character.trim() + "\"");
                } else {
                    ////System.out.println("ADDING STRING IN MAP......");
                    datas.get("\"" + temp + "\"").add("\"" + character.trim() + "\"");
                }
            }
            if (singleQuoteStart + 1 != singleQuoteEnd) {
                String oldString = line.substring(singleQuoteStart + 1, singleQuoteEnd);
                datas.get("\"" + temp + "\"").add("\"" + oldString.trim() + "\"");
            }

        }

    }

    public String replaceScanf(String line, String temp, int max, int min) {
        int startQuote = line.indexOf("\"");
        int quote = line.lastIndexOf("\"");

        String typename = line.substring(startQuote + 1, quote);
        String[] setType = typename.split("%");

        for (String var : setType) {
            ////System.out.println("VAR TYPE ........: " + var);
        }

        int close = line.indexOf(")");
        temp = line.substring(quote + 2, close).replaceAll("[^a-zA-Z]", "");
        String[] setVariable = temp.split(",");
        for (String variables : setVariable) {
            ////System.out.println("VARIABLE TEMP: " + variables);
        }
        line = "";
        int k = 1;

        for (String var : setVariable) {
            Random random = new Random();
            if (setType[k].equals("d")) {
                int randInt = random.nextInt(max);
                ////System.out.println("IN INT...." + randInt);
                line += var + "=" + randInt + ";";
                ////System.out.println("VAR ........: " + var);
            } else if (setType[k].equals("f")) {
                float randFloat = random.nextFloat() * (max - min);
                line += var + "=" + String.format("%.2f", randFloat) + ";";
            } else if (setType[k].equals("lf")) {
                double randDouble = random.nextDouble() * (max - min);
                line += var + "=" + randDouble + ";";
            }
            k++;

        }
        return line;
    }

    public String addScanf(String line, String temp) {

        Pattern scanfPattern = Pattern.compile("&\\s*(\\w+)\\s*");
        Matcher scanfMatcher = scanfPattern.matcher(line);

        ////System.out.println("scanf present");
        ////System.out.println("line scanf: " + line);

        while (scanfMatcher.find()) {
            ////System.out.println("scanf handle: " + scanfMatcher.group(1));
            String scanfVariable = scanfMatcher.group(1);
            for (Map.Entry<String, LinkedList<String>> map : datas.entrySet()) {
                ////System.out.println("map key : " + map.getKey());
                ////System.out.println("cin val : " + scanfVariable);

                if (map.getKey().equals("\"" + scanfVariable + "\"")) {

                    datas.get(map.getKey()).add("\"scanf\"");
                }

            }
        }
        return line;
    }

    public String addCin(String line, String temp) {

        temp = line.replace("cin", "");
        temp = temp.replace(";", "");
        ////System.out.println("in cin");

        Pattern cinStringPattern = Pattern.compile(
                "\\s*cin\\s*>>\\s*(\\w+\\d*)\\s*(?:>>(?=\\s*\\w)\\s*(\\w+\\d*)\\s*)*(\\w+\\d*)?\\s*;?");
        Matcher cinStringMatcher = cinStringPattern.matcher(line);
        String cinContent = "";

        if (cinStringMatcher.find()) {
            ////System.out.println("groupcount: " + cinStringMatcher.groupCount());
            if (cinStringMatcher.groupCount() > 2) {
                for (int i = 1; i <= cinStringMatcher.groupCount(); i++) {
                    String cinVariable = cinStringMatcher.group(i);
                    ////System.out.println("cin group: " + cinVariable);
                    if (cinVariable != null) {
                        ////System.out.println("Input variable " + i + ": " + cinVariable);
                        cinContent += "cin>>" + cinVariable + ";" + System.lineSeparator();
                        ////System.out.println("cin datas: "+datas);
                        if (datas.containsKey("\""+cinVariable+"\"")) {
                            datas.get("\""+cinVariable+"\"").add("\"cin\"");
                        }
        
                    }
                }
            }
            else{
                ////System.out.println("matcher cin: "+datas);
                if (datas.containsKey("\""+cinStringMatcher.group(1)+"\"")) {

                    datas.get("\""+cinStringMatcher.group(1)+"\"").add("\"cin\"");
                }
            }
        }
        line = cinContent;

        return line;
    }

    public void checkDoubleQuoteString(String line, String temp, int equalIndex) {
        int quoteStart = line.indexOf("\"");
        int quoteEnd = line.lastIndexOf("\"");
        ////System.out.println("line string  : " + line);

        String stringRegex = "\\s*string\\s+(\\w+)(\\s*=\\s*\".*\")?(\\s*,\\s*\\w+)*\\s*;";
        Pattern stringPattern = Pattern.compile(stringRegex);
        Matcher stringPatternMatcher = stringPattern.matcher(line);

        if (quoteStart != -1 && quoteEnd != -1) {

            String oldString = line.substring(quoteStart + 1, quoteEnd);

            if (quoteStart + 1 != quoteEnd) {

                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp.trim() + "\"", new LinkedList<>());
                    datas.get("\"" + temp.trim() + "\"").add("\"string\"");
                    datas.get("\"" + temp.trim() + "\"").add("\"" + oldString + "\"");
                } else {
                    datas.get("\"" + temp.trim() + "\"").add("\"" + oldString + "\"");
                }
            }

            temp = "";
        } else {
            if (equalIndex != -1) {
                ////System.out.println("equal index: " + equalIndex);

                temp = temp.substring(0, equalIndex).trim();
                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp + "\"", new LinkedList<>());
                    datas.get("\"" + temp + "\"").add("\"string\"");
                    datas.get("\"" + temp + "\"").add("\"\"");
                } else {
                    ////System.out.println("ADDING STRING IN MAP......");
                    datas.get("\"" + temp + "\"").add("\"\"");
                }
            } else {
                if (stringPatternMatcher.matches()) {
                    String stringVar = stringPatternMatcher.group(1);
                    ////System.out.println("stringVar: " + stringVar);

                    if (!datas.containsKey("\"" + stringVar + "\"")) {
                        datas.put("\"" + stringVar + "\"", new LinkedList<>());
                        datas.get("\"" + stringVar + "\"").add("\"string\"");
                        datas.get("\"" + stringVar + "\"").add("\"\"");
                    } else {
                        ////System.out.println("ADDING STRING IN MAP......");
                        datas.get("\"" + stringVar + "\"").add("\"\"");
                    }
                }
            }
        }

    }

    public String replaceVariables(String line, int max, int min) {
        Pattern numberPattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        Matcher matchPattern = numberPattern.matcher(line);

        Pattern stringPattern = Pattern.compile("\\s*[a-zA-Z]*\\s*[a-zA-Z]+\\s*=\\s*(\"[^\"]+\"|'[^']')\\s*;\\s*");
        Matcher stringPatternMatcher = stringPattern.matcher(line);

        String num = "";
        Random random = new Random();

        for (String name : datas.keySet()) {

            String datatype = datas.get(name).get(0);
            // ////System.out.println("data: "+datatype.replace("\"", ""));

            String checkLine = "\\s*(?!\\s*(printf|scanf|for|while|if|else|" + datatype.replace("\"", "") + ")).*";

            // ////System.out.println("checkline: " + checkLine);
            // ////System.out.println("line: "+line);
            // ////System.out.println("name : "+name);

            Pattern pattern = Pattern.compile(checkLine);
            Matcher matcher = pattern.matcher(line);
            String matchedname = name.replace("\"", "").trim();

            if (matcher.matches()) {
                ////System.out.println("matches : " + line + "name : " + name.replace("\"", ""));
                if (line.contains(matchedname)) {
                    ////System.out.println("name matches: " + matchedname);
                } else {
                    ////System.out.println("name not matches: " + matchedname);
                }
            }

            if ((matcher.matches()) && (line.contains(matchedname))) {
                ////System.out.println("line check: " + line);

                String datatypeRegex = "\"(float|double|int|string)\"";
                Pattern datatypePattern = Pattern.compile(datatypeRegex);
                Matcher datatypeMatcher = datatypePattern.matcher(datatype);
                ////System.out.println("datatype: " + datatype);
                ////System.out.println("datatype matcher: " + datatypeMatcher.find());

                if (datatypeMatcher.matches()) {
                    ////System.out.println("datatypeMatcher: matches!");
                    if (matchPattern.find()) {
                        num = matchPattern.group();
                        String newnum = "";
                        String datatypeMatched = datatypeMatcher.group(1);
                        ////System.out.println("datatypematcher: " + datatypeMatcher.group(1));
                        ////System.out.println("line datatypematcher: " + line);

                        if (!line.contains(datatypeMatched)) {
                            String variableNumberRegex = "\\s*[a-z]+\\s*=\\s*(\\d+(?:\\.\\d+)?)\\s*;";
                            Pattern variableNumberPattern = Pattern.compile(variableNumberRegex);
                            Matcher variableNumberMatcher = variableNumberPattern.matcher(line);

                            if (variableNumberMatcher.matches()) {
                                ////System.out.println("pattern newnum matches");
                                String match = variableNumberMatcher.group(1);
                                newnum = match;
                                datas.get(name).add("\"" + newnum + "\"");
                                ////System.out.println("newnum : " + newnum);
                            }

                        }

                    } else if (stringPatternMatcher.matches()) {
                        ////System.out.println("string pattern matches");
                        String matchedString = stringPatternMatcher.group(1);
                        ////System.out.println("matched string: " + matchedString);

                        StringBuffer sb = new StringBuffer();

                        // datas.get(name).add(matchedString);
                    }
                }
            }
        }
        return line;
    }

    public String addEmptyArray(String line, String temp) {
        line = line.replace(";", "");
        String arr = "={};";
        line += arr;
        temp = line.substring(line.indexOf("="), line.indexOf(";"));
        return line;
    }

    public void addVariables(String temp, String num, String datatype) {
        String wordVarString = "\\b[a-zA-Z]+\\d*\\b";
        Pattern wordVarPattern = Pattern.compile(wordVarString);
        Matcher wordMatcher = wordVarPattern.matcher(temp);

        if (wordMatcher.find()) {
            String wordVar = wordMatcher.group();
            ////System.out.println("wordvar: " + wordVar);

            if (!wordVar.equals("sizeof")) {

                if (!datas.containsKey("\"" + wordVar + "\"")) {
                    datas.put("\"" + wordVar + "\"", new LinkedList<String>());
                    datas.get("\"" + wordVar + "\"").add("\"" + datatype + "\"");
                    datas.get("\"" + wordVar + "\"").add("\"" + num + "\"");
                } else {
                    if (num != "0") {
                        datas.get("\"" + wordVar + "\"").add("\"" + num + "\"");
                    }
                }
            }

        }
    }

    public void removeComma(String filepath, Pattern commoPattern) {
        Scanner lines;
        String newFile = "";
        try {
            lines = new Scanner(new File(filepath));
            while (lines.hasNextLine()) {
                String line = lines.nextLine();

                Matcher commoMatcher = commoPattern.matcher(line);

                if (commoMatcher.matches() && line.contains(",") && !(line.contains("printf"))
                        && !(line.contains("scanf")) || line.trim().indexOf("cout") == 0) {

                    line = line.replace(";", "");
                    String dataType = commoMatcher.group(2);
                    line = line.replace(dataType, "");
                    ////System.out.println("comma: " + dataType);
                    ////System.out.println("line: " + line);
                    String[] variables = line.split(",");
                    line = "";
                    if (variables.length > 1) {
                        for (String var : variables) {
                            line += dataType + " " + var + ";\n";
                        }
                    }
                }
                ////System.out.println(line);
                newFile += line + "\n";
            }
            lines.close();
            Files.write(Paths.get(filepath), newFile.getBytes());
        } catch (Exception e) {
            ////System.out.println(e);
        }
    }

}