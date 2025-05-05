package com.ttgint.library.model;

import com.ttgint.library.record.LoaderFileRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_loader_result")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoaderResult implements Serializable {

    @Id
    @SequenceGenerator(name = "t_loader_result_seq_id", sequenceName = "t_loader_result_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_loader_result_seq_id")
    private Long id;
    private Long flowId;
    private String schemaName;
    private String tableName;
    private String fileName;
    private OffsetDateTime fragmentDate;
    private OffsetDateTime loadedTime;
    private Long rowCount;
    private Integer loadTryCount;

    private OffsetDateTime createdTime;

    public static LoaderResult getEntity(LoaderFileRecord record, OffsetDateTime fragmentDate, Long rowCount) {
        LoaderResult entity = new LoaderResult();
        entity.setFlowId(record.getFlowId());
        entity.setSchemaName(record.getSchemaName());
        entity.setTableName(record.getTableName());
        entity.setFileName(record.getFile().getName());
        entity.setFragmentDate(fragmentDate);
        entity.setLoadedTime(OffsetDateTime.now());
        entity.setRowCount(rowCount);
        entity.setLoadTryCount(1);
        entity.setCreatedTime(OffsetDateTime.now());
        return entity;
    }

}
