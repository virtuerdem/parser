package com.ttgint.parse.base;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseMapRecord;

import java.util.HashMap;

public interface ParseHandler {

    void preHandler();

    void onHandler();

    void postHandler();

    void syncWriteIntoFile(String fullPath, String line);

    void syncWriteIntoFile(ParseMapRecord parseMap, HashMap<String, String> keyValue);

    void deleteFile(ProgressType progressType);

    String stringDateFormatter(String stringDate, String inputFormat, String outputFormat);

    HashMap<String, String> prepareUniqueCodes(ParseMapRecord parseMap, HashMap<String, String> keyValue);

    HashMap<String, String> prepareGeneratedValues(ParseMapRecord parseMap, HashMap<String, String> keyValue);

    String prepareRecord(ParseMapRecord parseMap, HashMap<String, String> keyValue);

}
