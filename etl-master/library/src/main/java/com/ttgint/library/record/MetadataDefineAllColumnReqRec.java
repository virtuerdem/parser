package com.ttgint.library.record;

import com.ttgint.library.model.AllCounter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllColumnReqRec {

    private String columnName;
    private String objectKey;
    private String objectKey2;
    private String columnNameLookup;
    private String columnDescription;

    private String columnType;
    private String columnFormula;
    private Integer columnLength;
    private String modelType;
    private Boolean isColumnGen;
    private String columnGenFormula;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    public MetadataDefineAllColumnReqRec(AllCounter counter) {
        this.columnName = toColumnName(counter.getCounterKey());
        this.objectKey = counter.getCounterKey();
        this.columnNameLookup = counter.getCounterLookup();
        this.columnDescription = counter.getCounterDescription();
        this.modelType = counter.getModelType();
    }

    private static String toColumnName(String counterKey) {
        if (counterKey == null) return null;
        String raw = counterKey.startsWith("etlApp.")
                ? counterKey.replaceFirst("etlApp\\.\\w+?_", "")
                : counterKey;
        return raw
                .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                .replaceAll("([a-z])([A-Z])", "$1_$2")
                .toLowerCase();
    }

}
