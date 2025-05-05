package com.ttgint.library.model;

import com.ttgint.library.record.ContentDateResultRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_content_date_result")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ContentDateResult implements Serializable {

    @Id
    @SequenceGenerator(name = "t_content_date_result_seq_id", sequenceName = "t_content_date_result_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_content_date_result_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private String tableName;
    private String fileName;
    private OffsetDateTime fragmentDate;
    private Long rowCount;

    private OffsetDateTime resultTime;

    public static ContentDateResult recordToEntity(ContentDateResultRecord record) {
        ContentDateResult entity = new ContentDateResult();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setTableName(record.getTableName());
        entity.setFileName(record.getFileName());
        entity.setFragmentDate(record.getFragmentDate());
        entity.setRowCount(record.getRowCount());
        entity.setResultTime(OffsetDateTime.now());
        return entity;
    }

}
