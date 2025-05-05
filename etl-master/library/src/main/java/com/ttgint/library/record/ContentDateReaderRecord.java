package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ContentDateReaderRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;
    private Long allTableId;
    private Long parseTableId;
    private String schemaName;
    private String tableName;
    private Integer dateColumnIndex;
    private String dbDateFormat;
    private File file;
    private String resultFileDelimiter;
    private Boolean needResult;

    public ContentDateReaderRecord getRecord(File file,
                                             ParseEngineRecord parseEngineRecord,
                                             ParseMapRecord parseMapRecord) {
        ContentDateReaderRecord record = new ContentDateReaderRecord();
        record.setFile(file);

        record.setFlowId(parseEngineRecord.getFlowId());
        record.setFlowCode(parseEngineRecord.getFlowCode());
        record.setFlowProcessCode(parseEngineRecord.getFlowProcessCode());
        record.setNeedResult(parseEngineRecord.getNeedContentDateResult());

        record.setAllTableId(parseMapRecord.getParseTable().getAllTableId());
        record.setParseTableId(parseMapRecord.getParseTable().getParseTableId());
        record.setSchemaName(parseMapRecord.getParseTable().getSchemaName());
        record.setTableName(parseMapRecord.getParseTable().getTableName());
        record.setDateColumnIndex(parseMapRecord.getParseTable().getDateColumnIndex());
        record.setDbDateFormat(
                parseMapRecord.getParseColumns()
                        .stream()
                        .filter(e -> e.getColumnOrderId().equals(record.getDateColumnIndex()))
                        .map(ParseColumnRecord::getColumnFormula)
                        .findFirst()
                        .orElse(null));
        record.setResultFileDelimiter(parseMapRecord.getParseTable().getResultFileDelimiter());
        return record;
    }
}
