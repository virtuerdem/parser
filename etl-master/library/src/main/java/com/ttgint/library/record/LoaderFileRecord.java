package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoaderFileRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;
    private Boolean needLoaderResult;

    private File file;
    private String fileExtension;
    private String fileDelimiter;

    private Integer fragmentDateIndex;
    private String fragmentDateFormat;

    private String schemaName;
    private String tableName;
    private List<SqlLdrColumnPatternRecord> columnRecord;
    private LoaderEnvironmentRecord loaderEnvironment;
    private List<ContentDateResultRecord> contentDates;

    private String loaderTarget;

    public LoaderFileRecord getRecord(File file,
                                      ParseEngineRecord engineRecord,
                                      ParseMapRecord parseMapRecord,
                                      List<ContentDateResultRecord> contentDates) {
        LoaderFileRecord record = new LoaderFileRecord();
        record.setFile(file);
        record.setFileExtension(file.getName().substring(file.getName().lastIndexOf(".")));
        record.setFlowId(engineRecord.getFlowId());
        record.setFlowCode(engineRecord.getFlowCode());
        record.setFlowProcessCode(engineRecord.getFlowProcessCode());
        record.setNeedLoaderResult(engineRecord.getNeedLoaderResult());
        record.setFileDelimiter(parseMapRecord.getParseTable().getResultFileDelimiter());
        record.setFragmentDateIndex(parseMapRecord.getParseTable().getDateColumnIndex());
        record.setFragmentDateFormat(
                parseMapRecord.getParseColumns().stream()
                        .filter(e -> e.getColumnOrderId().equals(record.getFragmentDateIndex()))
                        .map(ParseColumnRecord::getColumnFormula)
                        .findFirst().get());
        record.setSchemaName(parseMapRecord.getParseTable().getSchemaName());
        record.setTableName(parseMapRecord.getParseTable().getTableName());
        record.setColumnRecord(SqlLdrColumnPatternRecord.getRecords(parseMapRecord.getParseColumns()));
        record.setLoaderTarget(parseMapRecord.getParseTable().getLoaderTarget());
        record.setContentDates(contentDates);
        return record;
    }
}
