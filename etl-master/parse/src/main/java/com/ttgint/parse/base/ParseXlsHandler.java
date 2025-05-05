package com.ttgint.parse.base;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public abstract class ParseXlsHandler extends ParseBaseHandler {

    private Integer sheetIndex;

    public ParseXlsHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, Integer sheetIndex) {
        super(applicationContext, handlerRecord);
        this.sheetIndex = sheetIndex;
    }

    @Override
    public void preHandler() {
    }

    public abstract void lineProgress(Long lineIndex, Cell[] cell);

    @Override
    public void onHandler() {
        long lineIndex = 0L;
        Workbook workBook = null;
        try {
            workBook = Workbook.getWorkbook(getHandlerRecord().getFile());
            Sheet sheet = workBook.getSheet(sheetIndex);
            for (lineIndex = 0L; lineIndex < sheet.getRows(); lineIndex++) {
                lineProgress(lineIndex, sheet.getRow(Integer.parseInt(Long.toString(lineIndex))));
            }
            deleteFile(getHandlerRecord().getProgressType());
        } catch (Exception exception) {
            log.error("xls parse error lineNumber: {}", lineIndex, exception);
            deleteFile(ProgressType.PRODUCT);
        }
        try {
            if (workBook != null) {
                workBook.close();
            }
        } catch (Exception exception) {
            log.error("xls parse close error", exception);
        }
    }

    @Override
    public void postHandler() {
    }

}