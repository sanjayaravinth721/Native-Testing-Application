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
import java.sql.Time;

public class FileData {
    private LinkedHashMap<String, LinkedList<String>> datas = new LinkedHashMap<>();
    // private static LinkedHashMap<String, LinkedList<String>> currentData = new
    // LinkedHashMap<>();

    public LinkedHashMap<String, LinkedList<String>> getInputs() {
        return datas;
    }

    public void clearInput() {
        datas.clear();
    }

    public void file(String filename) throws Exception {

        String filePath = filename;
        Scanner sc = new Scanner(new File(filePath));

        StringBuffer buffer = new StringBuffer();
        Pattern pattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");
        int min = 5;
        int max = 10;

        // if (datas != null) {
        // datas.clear();
        // }

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

        // String numberArray =
        // "\\s*(int|float|double)\\s+\\w+\\s*\\[\\s*(\\d+\\s*)?\\]\\s*=\\s*\\{(\\s*[+-]?\\d+(\\.\\d+)?(\\s*,\\s*[+-]?\\d+(\\.\\d+)?)*)?\\s*\\}\\s*;$";
        // Pattern numberArrayPattern = Pattern.compile(numberArray);

        String regex = "\\s*(int|float|double)\\s+\\w+(\\s*\\[\\s*(\\d+\\s*)?\\])?(\\s*=\\s*[+-]?\\d+(\\.\\d+)?\\s*)*(\\s*,\\s*\\w+(\\s*\\[\\s*(\\d+\\s*)?\\])?(\\s*=\\s*[+-]?\\d+(\\.\\d+)?\\s*)*)*\\s*;$";
        Pattern numberDatatypePattern = Pattern.compile(regex);

        String initializedPatternString = "(\\s*)(int|double|float|long)(.*)=\\s*(?!\\{|\\s)(\".*\"|[^;}]+);\\s*";
        Pattern initializedPattern = Pattern.compile(initializedPatternString);

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String temp = "";

            String num = "0";
            ////System.out.println("string line : " + line);
            if (line.trim().indexOf("//") == 0 || line.trim().indexOf("return") == 0
                    || line.trim().indexOf("cout") == 0) {
                ////System.out.println("command: " + line);
                buffer.append(line + System.lineSeparator());
                continue;
            }

            // Matcher numberArrayMatcher = numberArrayPattern.matcher(line);
            // ////System.out.println("numberArrayMatcher: "+numberArrayMatcher);
            Matcher numberDatatypeMatcher = numberDatatypePattern.matcher(line);
            Matcher initializedMatcher = initializedPattern.matcher(line);
            Random random = new Random();

            // ////System.out.println("lines: "+line+"matches : "+numberArrayMatcher.matches());
            ////System.out.println("lines: " + line + "matches : " + numberDatatypeMatcher.matches());

            if (numberDatatypeMatcher.matches() || (line.contains("{") && line.contains("}"))) {

                ////System.out.println("NUMBER LINE : " + line);

                ////System.out.println("DATATYPES...");

                Pattern datatypePattern = Pattern.compile("(\\s*)(int|double|float|long)(.*)");
                Matcher datatypeMatcher = datatypePattern.matcher(line);

                if (datatypeMatcher.find()) {
                    ////System.out.println("line.." + line);
                    String matchedDatatype = datatypeMatcher.group(2);
                    ////System.out.println("Matched datatype: " + matchedDatatype);

                    int intRandom = random.nextInt(max - min) + min;
                    float floatRandom = random.nextFloat() * (max - min);
                    Double doubleRandom = random.nextDouble() * (max - min);
                    Long longRandom = random.nextLong() * (max - min);

                    if (line.indexOf("=") != -1) {

                        temp = line.substring(line.indexOf("="), line.indexOf(";"));
                        if (!(temp.contains("[")) && !(temp.contains("]"))) {

                            String isArray = "(?i)^.*\\[.*\\].*\\{.*\\}.*(?!sizeof).*$";
                            Pattern isArrayPattern = Pattern.compile(isArray);
                            Matcher isArrayPatternMatcher = isArrayPattern.matcher(line);
                            ////System.out.println("is array pattern");

                            if (isArrayPatternMatcher.matches()) {
                                ////System.out.println("is array true");
                                int randomArrLength = random.nextInt(7 - 1) + 1;
                                num = Integer.toString(randomArrLength);

                                if (datatypeMatcher.matches()) {
                                    line = addArray(line, temp, matchedDatatype + "Array", randomArrLength);
                                    temp = line.replace(matchedDatatype, "");
                                    temp = temp.replaceAll("\\s", "");
                                }

                            } else {

                                temp = line.replace(matchedDatatype, "");
                                temp = temp.replaceAll("\\s", "");
                                ////System.out.println("temp number: " + temp);
                                Matcher m = pattern.matcher(temp.substring(temp.indexOf("="), temp.indexOf(";")));

                                num = "0";
                                if (m.find()) {
                                    if (!m.group().equals("0")) {
                                        num = m.group();
                                        if (matchedDatatype.equals("float")) {
                                            line = line.replace(num, String.format("%.2f", floatRandom));
                                            num = String.format("%.2f", floatRandom);
                                        } else if (matchedDatatype.equals("double")) {
                                            line = line.replace(num, String.format("%.2f", doubleRandom));
                                            num = String.format("%.2f", doubleRandom);

                                        } else if (matchedDatatype.equals("int")) {
                                            line = line.replace(num, Integer.toString(intRandom));
                                            num = Integer.toString(intRandom);
                                        } else if (matchedDatatype.equals("long")) {
                                            line = line.replace(num, Long.toString(longRandom));
                                            num = Long.toString(longRandom);
                                        }
                                    }
                                }

                                addVariables(temp, num, matchedDatatype);

                            }
                        } else {
                            ////System.out.println("In first arr0");
                            temp = line.replace(matchedDatatype, "").trim();
                            ////System.out.println("temp: " + temp);

                            if (temp.contains("[") && temp.contains("]")) {
                                ////System.out.println("In first arr1");
                                line = addEmptyArray(line, temp);
                                int randomLength = random.nextInt(10 - 1) + 1;
                                temp = line.substring(line.indexOf("="), line.indexOf(";"));
                                ////System.out.println("In first arr2");

                                line = addArray(line, temp, matchedDatatype + "Array", randomLength);

                            } else {
                                temp = temp.replaceAll("\\s", "");
                                addVariables(temp, "0", matchedDatatype);
                            }
                        }
                    } else {
                        line = line.replace(";", "");
                        line += "= 0 ;";

                        temp = line.replace(matchedDatatype, "");
                        temp = temp.replaceAll("\\s", "");

                        addVariables(temp, "0", matchedDatatype);
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
                random = new Random();

                if (m.find()) {
                    if (!m.group().equals("0")) {
                        ////System.out.println("IT IS CHAR []!");
                        int intRandom = random.nextInt(90 - 65) + 65;
                        line = line.replace(m.group(), Integer.toString(intRandom));
                    }
                }

                line = singleQuoteCharacter(line, temp);

                temp = line.replace("char", "").trim();
                ////System.out.println("char temp: " + temp);

                if (temp.indexOf("[") != -1) {
                    temp = temp.substring(0, temp.indexOf("["));

                } else if (temp.indexOf("*") != -1) {
                    temp = temp.substring(0, temp.indexOf(";"));
                }

                line = doubleQuoteCharacter(line, temp);
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

                line = checkDoubleQuoteString(line, temp, equalIndex);
                ////System.out.println("line from string: " + line);
            }

            // --------------------------cin----------------------------------------//

            if (line.contains("cin")) {
                line = replaceCin(line, temp);
            }

            // --------------------------scanf----------------------------------------//

            if (line.contains("scanf")) {

                line = replaceScanf(line, temp, max, min);

            }

            // ------------------Add If Already Exists---------------------------//

            line = replaceVariables(line, max, min);

            // -------------------------------------------------------------------//

            buffer.append(line + System.lineSeparator());

            // previousLine = line;

        }

        String fileContents = buffer.toString();

        for (Map.Entry<String, LinkedList<String>> entry : datas.entrySet()) {
            for (String list : entry.getValue()) {
                ////System.out.println("var : " + list);
            }
        }

        sc.close();
        FileWriter writer = new FileWriter(filePath);
        writer.append(fileContents);
        writer.flush();
        writer.close();
    }

    public String addArray(String line, String temp, String type, int randomArrLength) {

        Random random = new Random();

        int intRandom = 0;
        float floatRandom = 0;
        double doubleRandom = 0;

        ////System.out.println("IT IS " + type + " [] ARRAY!");
        int openSquareBracket = line.indexOf("[");
        int closeSquareBracket = line.indexOf("]");

        String arraysLeftSub = line.substring(0, openSquareBracket).trim();
        String arrayVar = arraysLeftSub.split(" ")[1];

        temp = line.substring(openSquareBracket + 1, closeSquareBracket).trim();

        if (openSquareBracket + 1 != closeSquareBracket) {
            if (temp.matches("\\d+")) {
                line = line.replace(temp, Integer.toString(randomArrLength));
            } else {
                for (Map.Entry<String, LinkedList<String>> map : datas.entrySet()) {
                    if (map.getKey().equals("\"" + temp + "\"")) {
                        int size = map.getValue().size() - 1;
                        randomArrLength = Integer.parseInt(map.getValue().get(size));
                        break;
                    }
                }
            }
        }

        int curlyStart = line.indexOf("{");
        int curlyEnd = line.indexOf("}");

        if (curlyStart != -1 && curlyEnd != -1) {
            temp = line.substring(curlyStart, curlyEnd + 1);
            String oldarray = line.substring(curlyStart + 1, curlyEnd);

            String newArr = "{";

            for (int i = 0; i < randomArrLength; i++) {
                if (i != randomArrLength - 1) {
                    if (type.equals("intArray")) {
                        intRandom = random.nextInt(400 - 1) + 1;
                        newArr += Integer.toString(intRandom) + ",";
                    } else if (type.equals("floatArray")) {
                        floatRandom = random.nextFloat() * (400 - 1);
                        newArr += String.format("%.2f", floatRandom) + ",";
                    } else if (type.equals("doubleArray")) {
                        doubleRandom = random.nextDouble() * (400 - 1);
                        newArr += String.format("%.2f", doubleRandom) + ",";
                    } else {
                        newArr += "0,";
                    }

                } else {
                    if (type.equals("intArray")) {
                        intRandom = random.nextInt(400 - 1) + 1;
                        newArr += Integer.toString(intRandom);
                    } else if (type.equals("floatArray")) {
                        floatRandom = random.nextFloat() * (400 - 1);
                        newArr += String.format("%.2f", floatRandom);
                    } else if (type.equals("doubleArray")) {
                        doubleRandom = random.nextDouble() * (400 - 1);
                        newArr += String.format("%.2f", doubleRandom);
                    } else {
                        newArr += "0";
                    }
                }
            }

            newArr += "}";

            if (!datas.containsKey("\"" + arrayVar + "\"")) {
                datas.put("\"" + arrayVar + "\"", new LinkedList<>());
                datas.get("\"" + arrayVar + "\"").add("\"" + type + "\"");
                String arr = newArr.substring(newArr.indexOf("{") + 1, newArr.indexOf("}"));
                datas.get("\"" + arrayVar + "\"").add("\"" + arr + "\"");
            }

            line = line.replace(temp, newArr);
        }
        // else{
        // String arrayInitializing =
        // "^\\s*(int|float|double|long|string|char)\\s+([a-z]+\\d*(?:\\d*)?)\\s*(?:\\[\\s*\\d*\\s*\\]|\\[\\s*[a-z]+\\s*\\])?\\s*;\\s*$";
        // Pattern arrayInitializingPattern = Pattern.compile(arrayInitializing);

        // Matcher arrayInitializingMatcher = arrayInitializingPattern.matcher(line);

        // }
        return line;
    }

    public String doubleQuoteCharacter(String line, String temp) {
        int openSquareString = temp.indexOf("[");
        int closeSquareString = temp.indexOf("]");

        Random rands = new Random();

        int quoteStart = line.indexOf("\"");
        int quoteEnd = line.lastIndexOf("\"");

        if (openSquareString != -1 && closeSquareString != -1) {
            if (openSquareString + 1 != closeSquareString) {
                String arrayLength = temp.substring(openSquareString + 1, closeSquareString).trim();
                int randomLength = rands.nextInt(90 - 65) + 65;
                line = line.replace(arrayLength, Integer.toString(randomLength));
            }
            temp = temp.substring(0, temp.indexOf("["));
        } else if (temp.indexOf("*") != -1) {
            ////System.out.println("char pointer!");
            
            String charPointer = "\\*(\\w+)(\\d+)?";
            Pattern charPointerPattern = Pattern.compile(charPointer);
            Matcher charPointerMatcher = charPointerPattern.matcher(temp);
            if (charPointerMatcher.matches()) {
                String charStr = charPointerMatcher.group(1);
                 if (!datas.containsKey("\"" + charStr + "\"")) {
                    ////System.out.println("temp char : " + charStr);
                    datas.put("\"" + charStr + "\"", new LinkedList<>());
                    datas.get("\"" + charStr + "\"").add("\"string\"");
                    datas.get("\"" + charStr + "\"").add("\"0\"");
                }
            }
        }

        if (quoteStart != -1 && quoteEnd != -1) {
            String oldString = line.substring(quoteStart + 1, quoteEnd);

            Random rand = new Random();
            int randomCharLength = rand.nextInt(10 - 2) + 10;

            StringBuffer sb = new StringBuffer();

            for (int j = 0; j < randomCharLength; j++) {
                int characters = rand.nextInt(90 - 65) + 65;
                sb.append((char) characters);
            }
            line = line.replace(oldString, sb);
            if (!datas.containsKey("\"" + temp + "\"")) {
                datas.put("\"" + temp + "\"", new LinkedList<>());
                datas.get("\"" + temp + "\"").add("\"string\"");
                datas.get("\"" + temp + "\"").add("\"" + sb.toString() + "\"");
            } else {
                datas.get("\"" + temp + "\"").add("\"" + sb.toString() + "\"");
            }
            temp = "";
        }

        return line;
    }

    public String singleQuoteCharacter(String line, String temp) {
        int singleQuoteStart = line.indexOf("\'");
        int singleQuoteEnd = line.lastIndexOf("\'");

        temp = line.replace("char", "");
        temp = temp.trim();
        int equalIndexes = temp.indexOf("=");
        Random rands = new Random();
        int character = rands.nextInt(90 - 65) + 65;

        if (singleQuoteStart != -1 && singleQuoteEnd != -1) {

            if (equalIndexes != -1) {

                temp = temp.substring(0, equalIndexes);
                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp + "\"", new LinkedList<>());
                    datas.get("\"" + temp + "\"").add("\"char\"");
                    datas.get("\"" + temp + "\"").add("\"" + String.valueOf((char) character) + "\"");
                } else {
                    ////System.out.println("ADDING STRING IN MAP......");
                    datas.get("\"" + temp + "\"").add("\"" + String.valueOf((char) character) + "\"");
                }
            }
            if (singleQuoteStart + 1 == singleQuoteEnd) {
                line = line.replace("\'\'", '\'' + String.valueOf((char) character) + '\'');
            } else {
                String oldString = line.substring(singleQuoteStart + 1, singleQuoteEnd);
                line = line.replace('\'' + oldString + '\'', '\'' + String.valueOf((char) character) + '\'');
            }

        } else {
            if (equalIndexes != -1) {

                temp = temp.substring(0, equalIndexes);
                if (!(temp.contains("[") && temp.contains("]"))) {
                    if (!datas.containsKey("\"" + temp + "\"")) {
                        datas.put("\"" + temp + "\"", new LinkedList<>());
                        datas.get("\"" + temp + "\"").add("\"char\"");
                        datas.get("\"" + temp + "\"").add("\"0\"");
                    } else {
                        datas.get("\"" + temp + "\"").add("\"" + String.valueOf((char) character) + "\"");
                    }
                }
            }
        }
        return line;
    }

    public String replaceScanf(String line, String temp, int max, int min) {
        int startQuote = line.indexOf("\"");
        int quote = line.lastIndexOf("\"");

        String typename = line.substring(startQuote + 1, quote);
        String[] setType = typename.split("%");

        Pattern scanfPattern = Pattern.compile("&\\s*(\\w+)\\s*");
        ////System.out.println("scanf line: " + line);
        Matcher scanfMatcher = scanfPattern.matcher(line);

        ////System.out.println("scanf present");

        for (String var : setType) {
            // line += var+"="+(Math.random()*(100-5+1)+5)+";";
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

        while (scanfMatcher.find()) {
            String var = scanfMatcher.group(1);
            ////System.out.println("var: " + var);
            Random random = new Random();
            if (setType[k].equals("d")) {
                int randInt = random.nextInt(max);
                ////System.out.println("IN INT...." + randInt);
                line += var + "=" + randInt + ";\n";
                ////System.out.println("VAR ........: " + var);
            } else if (setType[k].equals("f")) {
                float randFloat = random.nextFloat() * (max - min);
                line += var + "=" + String.format("%.2f", randFloat) + ";\n";
            } else if (setType[k].equals("lf")) {
                double randDouble = random.nextDouble() * (max - min);
                line += var + "=" + randDouble + ";\n";
            } else if (setType[k].equals("s")) {
                int randomCharLength = random.nextInt(10 - 2) + 10;
                StringBuffer sb = new StringBuffer();
                sb.append("\"");

                for (int j = 0; j < randomCharLength; j++) {
                    int characters = random.nextInt(90 - 65) + 65;

                    sb.append((char) characters);
                }
                datas.get("\"" + var + "\"").add(sb.toString() + "\"");
                sb.append("\";");

                line += var + " = " + sb.toString() + System.lineSeparator();
            } else if (setType[k].equals("c")) {
                int randomCharLength = random.nextInt(20 - 5);
                line += var + " = \'" + (char) randomCharLength + "\'" + System.lineSeparator();
            }
            k++;

        }
        return line;
    }

    public String replaceCin(String line, String temp) {
        Random rand = new Random();
        int randomCharLength = rand.nextInt(15 - 5) + 15;
        temp = line.replace("cin", "");
        temp = temp.replace(";", "");
        ////System.out.println("in cin");

        for (String in : temp.trim().split(">>")) {
            String cinVariable = in.trim();
            ////System.out.println("cin>> " + cinVariable);
            if (!cinVariable.equals("")) {

                for (Map.Entry<String, LinkedList<String>> map : datas.entrySet()) {
                    ////System.out.println("map key : " + map.getKey());
                    ////System.out.println("cin val : " + cinVariable);

                    if (map.getKey().equals("\"" + cinVariable + "\"")) {
                        ////System.out.println("equal");
                        if (map.getValue().get(0).equals("\"int\"")) {
                            int intRand = rand.nextInt(10 - 1) + 1;
                            line = cinVariable + "=" + Integer.toString(intRand) + ";";
                            // datas.get("\""+cinVariable+"\"").add("\""+intRand+"\"");
                        } else if (map.getValue().get(0).equals("\"double\"")) {
                            double doubleRand = rand.nextDouble() * (10 - 1);
                            line = cinVariable + "=" + Double.toString(doubleRand) + ";";
                            // datas.get("\""+cinVariable+"\"").add("\""+doubleRand+"\"");
                        } else if (map.getValue().get(0).equals("\"float\"")) {
                            float floatRand = rand.nextFloat() * (10 - 1);
                            line = cinVariable + "=" + Float.toString(floatRand) + ";";
                            // datas.get("\""+cinVariable+"\"").add("\""+floatRand+"\"");
                        }

                        else if (map.getValue().get(0).equals("\"string\"")) {
                            line = "";
                            StringBuffer sb = new StringBuffer();
                            sb.append("\"");

                            for (int j = 0; j < randomCharLength; j++) {
                                int characters = rand.nextInt(90 - 65) + 65;

                                sb.append((char) characters);
                            }
                            datas.get("\"" + cinVariable + "\"").add(sb.toString() + "\"");
                            sb.append("\";");

                            line += cinVariable + " = " + sb.toString() + System.lineSeparator();
                            ////System.out.println("FILE DATA LINE: " + line);

                        }
                    }
                }
            }
        }
        return line;
    }

    public String checkDoubleQuoteString(String line, String temp, int equalIndex) {
        int quoteStart = line.indexOf("\"");
        int quoteEnd = line.lastIndexOf("\"");
        ////System.out.println("line string  : " + line);

        String stringRegex = "\\s*string\\s+(\\w+)(\\s*=\\s*\".*\")?(\\s*,\\s*\\w+)*\\s*;";
        Pattern stringPattern = Pattern.compile(stringRegex);
        Matcher stringPatternMatcher = stringPattern.matcher(line);

        if (quoteStart != -1 && quoteEnd != -1) {

            Random rand = new Random();
            int randomCharLength = rand.nextInt(10 - 2) + 10;
            StringBuffer stringBuffer = new StringBuffer();

            if (quoteStart + 1 == quoteEnd) {
                for (int j = 0; j < randomCharLength; j++) {
                    int characters = rand.nextInt(90 - 65) + 65;

                    stringBuffer.append((char) characters);
                }
                ////System.out.println("LINE BEFORE : " + line);
                line = line.replace("\"\"", "\"" + stringBuffer + "\"");

                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp + "\"", new LinkedList<>());
                    datas.get("\"" + temp + "\"").add("\"string\"");
                    datas.get("\"" + temp + "\"").add("\"" + stringBuffer.toString() + "\"");
                } else {
                    datas.get("\"" + temp + "\"").add("\"" + stringBuffer.toString() + "\"");
                }

            } else {

                String oldString = line.substring(quoteStart + 1, quoteEnd);
                StringBuffer sb = new StringBuffer();

                for (int j = 0; j < randomCharLength; j++) {
                    int characters = rand.nextInt(90 - 65) + 65;

                    sb.append((char) characters);
                }
                line = line.replace("\"" + oldString + "\"", "\"" + sb + "\"");

                if (!datas.containsKey("\"" + temp + "\"")) {
                    datas.put("\"" + temp + "\"", new LinkedList<>());
                    datas.get("\"" + temp + "\"").add("\"string\"");
                    datas.get("\"" + temp + "\"").add("\"" + sb.toString() + "\"");
                } else {
                    datas.get("\"" + temp + "\"").add("\"" + sb.toString() + "\"");
                }

            }

            temp = "";
        } else {
            if (equalIndex != -1) {
                ////System.out.println("equal index: " + equalIndex);

                temp = temp.substring(0, equalIndex);
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
                        datas.get("\"" + stringVar + "\"").add("\"0\"");
                    } else {
                        ////System.out.println("ADDING STRING IN MAP......");
                        datas.get("\"" + stringVar + "\"").add("\"0\"");
                    }
                }
            }
        }
        return line;
    }

    public String replaceVariables(String line, int max, int min) {
        Pattern numberPattern = Pattern.compile("[-+]?[0-9]*\\.?[0-9]+");

        String temp = line.trim();

        Matcher numberMatchPattern = numberPattern.matcher(line);

        Pattern stringPattern = Pattern.compile("\\s*[a-zA-Z]*\\s*[a-zA-Z]+\\s*=\\s*(\"[^\"]+\"|'[^']')\\s*;\\s*");
        Matcher stringPatternMatcher = stringPattern.matcher(line);

        String num = "";
        Random random = new Random();

        for (String name : datas.keySet()) {

            String datatype = datas.get(name).get(0);
            // ////System.out.println("data: "+datatype.replace("\"", ""));

            String checkLine = "\\s*(?!\\s*(printf|scanf|if|for|while|" + datatype.replace("\"", "") + ")).*";

            ////System.out.println("checkline: " + checkLine);
            ////System.out.println("line: " + line);
            ////System.out.println("name : " + name);

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

                String datatypeRegex = "\"(float|double|int|char|string)\"";
                Pattern datatypePattern = Pattern.compile(datatypeRegex);
                Matcher datatypeMatcher = datatypePattern.matcher(datatype);
                ////System.out.println("datatype: " + datatype);
                ////System.out.println("datatype matcher: " + datatypeMatcher.find());

                if (datatypeMatcher.matches()) {
                    ////System.out.println("datatypeMatcher: matches!");
                    if (numberMatchPattern.find()) {
                        num = numberMatchPattern.group();
                        String newnum = "";
                        String datatypeMatched = datatypeMatcher.group(1);
                        ////System.out.println("datatypematcher: " + datatypeMatcher.group(1));
                        ////System.out.println("line datatypematcher: " + line);

                        if (!line.contains(datatypeMatched)) {
                            ////System.out.println("not line datatypematcher: " + line);
                            if (datatypeMatched.equals("float") || datatypeMatched.equals("double")) {
                                float floatRandom = random.nextFloat() * (max - min);
                                ////System.out.println("it is float: " + num);
                                newnum = String.format("%.2f", floatRandom);

                            } else if (datatypeMatched.equals("int")) {
                                ////System.out.println("integer: " + num);
                                int intRandom = random.nextInt(max - min) + min;
                                newnum = Integer.toString(intRandom);
                            }
                        }
                        if (!newnum.equals("")) {
                            line = line.replace(num, newnum);
                            ////System.out.println("line replace: " + line);
                            datas.get(name).add("\"" + newnum + "\"");
                        }
                    } else if (stringPatternMatcher.matches()) {
                        ////System.out.println("string pattern matches");
                        String matchedString = stringPatternMatcher.group(1);
                        int randomCharLength = random.nextInt(10 - 2) + 10;
                        // matchedString = matchedString.substring(0,matchedString.length()-1);

                        StringBuffer sb = new StringBuffer();

                        for (int j = 0; j < randomCharLength; j++) {
                            int characters = random.nextInt(90 - 65) + 65;

                            sb.append((char) characters);
                        }
                        line = line.replace(matchedString, "\"" + sb + "\"");

                        datas.get(name).add("\"" + sb.toString() + "\"");
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
                        && !(line.contains("scanf")) && !(line.contains("{")) && !(line.contains("}"))) {

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