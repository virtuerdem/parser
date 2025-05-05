package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ContentDateResultRecord {

    private Long flowId;
    private String flowProcessCode;
    private String tableName;
    private String fileName;
    private OffsetDateTime fragmentDate;
    private Long rowCount;

    public static ContentDateResultRecord recordToEntity(ContentDateReaderRecord record,
                                                         OffsetDateTime fragmentDate,
                                                         Long rowCount) {
        ContentDateResultRecord entity = new ContentDateResultRecord();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setTableName(record.getTableName());
        entity.setFileName(record.getFile().getName());
        entity.setFragmentDate(fragmentDate);
        entity.setRowCount(rowCount);
        return entity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContentDateResultRecord)) return false;

        ContentDateResultRecord that = (ContentDateResultRecord) o;
        return Objects.equals(this.flowId, that.flowId) &&
                Objects.equals(this.tableName, that.tableName) &&
                Objects.equals(this.fileName, that.fileName) &&
                Objects.equals(this.fragmentDate, that.fragmentDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(flowId, tableName, fileName, fragmentDate);
    }
}
