/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_U2000_new;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_U2000_new.mediaCapacityInterfaceWrongStringForMail;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
public class HWU2000NewParserHandler extends CsvFileHandler {

    //2015-01-07 05:30:00
    private static final int resourceNameOrder = 2;
    private static final int deviceIdOrder = 0;
    private static final int deviceNameOrder = 1;
    private static final int collectionTimeOrder = 3;
    private final SimpleDateFormat dateFormatterFromFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    boolean flag = true;

    public HWU2000NewParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    private boolean isValuesStarted = false;
    private int headerSize = -1;
    private boolean lineCheck = false;
    private String header;
    private Date dateObject;
    private String functionSubsetId;
    private RawTableObject tableObject;

    @Override
    public void onStartParseOperation() {
        String fileName = currentFileProgress.getName();
        functionSubsetId = fileName.split("\\_")[1];
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(functionSubsetId);

    }

    public boolean isMatched(String counterHeaderName) {

        for (RawCounterObject rawCounterObject : tableObject.getCounterObjectList()) {
            if (counterHeaderName.equals(rawCounterObject.getCounterNameFile())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void lineProgress(String[] splitted) {

        String line = CommonLibrary.joinString(splitted, "$");

        if (line.startsWith("DeviceID")) {
            header = line + "$OBJECT_ID";

            //String[] splitted = line.split("\\,");
            headerSize = splitted.length;
            isValuesStarted = true;

            String[] headerArr = header.split("\\$");
            String[] headerNew = new String[headerArr.length];

            int index = 0;
            for (String head : headerArr) {
                if (head.equals("CollectionTime") || head.equals("GranularityPeriod")) {
                    headerNew[index++] = head;

                } else {
                    boolean flag = false;
                    head = head.trim();

                    if (isMatched(head)) {
                        headerNew[index++] = head;
                    } else {
                        headerNew[index++] = head.replace(" ", "").toUpperCase();
                    }
                }
            }

            header = "";
            for (String head : headerNew) {
                header += head + "$";
            }
            header = header.substring(0, header.length() - 1);

            return;
        }

        lineCheck = false;

        if (isValuesStarted) {

            line = fixLine(line);

            //String[] splitted = line.split("\\,");
            if (headerSize != -1
                    && splitted.length == headerSize) {

                if (dateObject == null) {
                    try {
                        dateObject = dateFormatterFromFile.parse(splitted[collectionTimeOrder]);
                    } catch (ParseException ex) {

                    }
                }
                String resourceMedia = "";
                String resourceCapacity = "";
                String resourceInterface = "";
                int phoneCode = 0;

                if (splitted[deviceNameOrder].contains("-PTN")) {
                    try {
                        phoneCode = Integer.parseInt(splitted[deviceNameOrder].split("-PTN")[1].substring(0, 3));
                    } catch (Exception ex) {
                    }
                } else if (splitted[deviceNameOrder].contains("-ATN")) {
                    try {
                        phoneCode = Integer.parseInt(splitted[deviceNameOrder].split("-ATN")[1].substring(0, 3));
                    } catch (Exception ex) {
                    }
                }

                String resourceName = splitted[resourceNameOrder];

                char[] charRed = resourceName.toCharArray();
                int leftBranket = 0;
                int rightBranket = 0;
                for (char each : charRed) {
                    String stringValueChar = String.valueOf(each);
                    if (stringValueChar.equals("(")) {
                        leftBranket = leftBranket + 1;
                    }

                    if (stringValueChar.equals(")")) {
                        rightBranket = rightBranket + 1;
                    }
                }

                resourceName = resourceName.trim();
                for (int i = 0; i < leftBranket - rightBranket; i++) {
                    resourceName += ")";
                }
                if (rightBranket > leftBranket) {
                    mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], "Check ( and )");
                    return;
                }

                if (resourceName.contains("||")) {
                    Pattern pat = Pattern.compile("\\|\\|[\\w,/\\-]+");
                    Matcher mat = pat.matcher(resourceName);

                    if (mat.find()) {
                        String match = mat.group();

                        resourceName = resourceName.split("\\" + match)[0].trim();

                        match = match.replace("|", "").replace("MB", "").replace("mb", "").replace("Mb", "");
                        String mediaCapacityInterface[] = match.split("[\\-,]");

                        switch (mediaCapacityInterface.length) {
                            case 0:
                                mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], "NULL info");
                                break;
                            case 1:
                                try {
                                    resourceCapacity = String.valueOf(Integer.parseInt(mediaCapacityInterface[0].replace("M", "")));
                                } catch (Exception ee) {
                                    resourceMedia = mediaCapacityInterface[0];
                                }
                                break;
                            case 2:
                                try {
                                    if (Integer.parseInt(mediaCapacityInterface[0].replace("M", ""))
                                            <= Integer.parseInt(mediaCapacityInterface[1].replace("M", ""))) {
                                        resourceCapacity = String.valueOf(Integer.parseInt(mediaCapacityInterface[0].replace("M", "")));
                                        resourceInterface = String.valueOf(Integer.parseInt(mediaCapacityInterface[1].replace("M", "")));
                                    } else {
                                        mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], match);
                                    }
                                } catch (Exception e) {
                                    resourceMedia = mediaCapacityInterface[0];
                                    try {
                                        resourceCapacity = String.valueOf(Integer.parseInt(mediaCapacityInterface[1]));
                                    } catch (Exception ee) {
                                        mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], match);
                                        return;
                                    }
                                }

                                break;
                            case 3:
                                try {
                                    resourceMedia = mediaCapacityInterface[0];
                                    resourceCapacity = String.valueOf(Integer.parseInt(mediaCapacityInterface[1].replace("M", "")));
                                    resourceInterface = String.valueOf(Integer.parseInt(mediaCapacityInterface[2].replace("M", "")));
                                } catch (Exception e) {
                                    mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], match);
                                    return;
                                }

                                break;
                            default:
                                mediaCapacityInterfaceWrongStringForMail.put(splitted[resourceNameOrder], match);
                        }
                        resourceName += ")";

                    } else {
                        System.out.println("2-" + currentFileProgress.getName() + " " + resourceName);
                        return;
                    }
                }

                String portName = getPortNameFromResourceName(resourceName);
                String objectId = ParserEngine_pm_csv_U2000_new.resourceNameToObjectId.get(resourceName);

                HWU2000NewObjectListener.addObject(splitted[deviceIdOrder], splitted[deviceNameOrder],
                        fixLine(resourceName), portName, resourceMedia, resourceCapacity, resourceInterface,
                        String.valueOf(phoneCode), splitted[resourceNameOrder]);

                if (objectId != null) {
                    line = line + "$" + objectId;
                    String resut = CommonLibrary.get_RecordValue(header, line, tableObject.getFullColumnOrderUsingCounterNameFil("$"), "0", "$", "$");
                    resut += "\n";
                    String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                    try {
                        writeIntoFilesWithController(fileName, resut);
                    } catch (ParserIOException ex) {
                        ex.printStackTrace();
                    }

                }
            } else {
                System.out.println("1-" + currentFileProgress.getName() + " " + splitted.length + " " + headerSize);
            }

        }
    }

    @Override
    public void onstopParseOperation() {
        if (lineCheck) {
            System.out.println("Corrupted " + currentFileProgress.getName());
        }
        currentFileProgress.delete();
    }

    public static String getPortNameFromResourceName(String resourceName) {
        if (!resourceName.endsWith(")")) {
            return "";
        }
        char[] charArray = resourceName.toCharArray();
        int endIndex = 0;
        int startIndex = 0;
        int numberOfOpenedBracket = 0;
        for (int i = charArray.length - 1; i >= 0; i--) {
            String valueOfChar = String.valueOf(charArray[i]);
            if (i == charArray.length - 1 && valueOfChar.equals(")")) {
                endIndex = charArray.length;
                numberOfOpenedBracket++;
                continue;
            }
            if (valueOfChar.equals(")")) {
                numberOfOpenedBracket++;
            }
            if (valueOfChar.equals("(")) {
                numberOfOpenedBracket--;
            }
            if (numberOfOpenedBracket == 0) {
                startIndex = i;
                break;
            }
        }
        return resourceName.substring(startIndex, endIndex);
    }

    private String fixLine(String line) {
        line = discard(line);
        if (line.endsWith(",")) {
            line += 0;
        }
        if (line.contains("\"")) {
            line = line.replace("\"", "");
        }
        if (line.contains("'")) {
            line = line.replace("'", "");
        }
        if (line.contains("?")) {
            line = line.replace("?", "");
        }
        return line;
    }

    private String discard(String line) {
        StringBuilder st = new StringBuilder();
        char[] charlist = line.toCharArray();
        for (char each : charlist) {
            if (Character.UnicodeBlock.of(each)
                    == Character.UnicodeBlock.BASIC_LATIN) {
                st.append(each);
            }
        }
        return st.toString();
    }
}
