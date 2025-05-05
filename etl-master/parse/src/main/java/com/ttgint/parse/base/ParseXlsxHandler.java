package com.ttgint.parse.base;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.context.ApplicationContext;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.Iterator;

@Slf4j
public abstract class ParseXlsxHandler extends ParseBaseHandler {

    private Integer sheetIndex;

    public ParseXlsxHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, Integer sheetIndex) {
        super(applicationContext, handlerRecord);
        this.sheetIndex = sheetIndex;
    }

    @Override
    public void preHandler() {
    }

    public abstract void lineProgress(Long lineIndex, XSSFRow xssfRow);

    @Override
    public void onHandler() {
        long lineIndex = 0L;
        try (FileInputStream fileInputStream
                     = new FileInputStream(getHandlerRecord().getFile());
             BufferedInputStream bufferedInputStream
                     = new BufferedInputStream(fileInputStream);
             XSSFWorkbook xssfWorkbook
                     = new XSSFWorkbook(bufferedInputStream)) {
            XSSFSheet xssfSheet = xssfWorkbook.getSheetAt(sheetIndex);
            Iterator<Row> rows = xssfSheet.rowIterator();
            while (rows.hasNext()) {
                lineProgress(lineIndex, (XSSFRow) rows.next());
                lineIndex++;
            }
            deleteFile(getHandlerRecord().getProgressType());
        } catch (Exception exception) {
            log.error("xlsx parse error lineNumber: {}", lineIndex, exception);
            deleteFile(ProgressType.PRODUCT);
        }
    }

    @Override
    public void postHandler() {
    }

}
