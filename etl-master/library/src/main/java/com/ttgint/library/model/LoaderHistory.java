package com.ttgint.library.model;

import com.ttgint.library.record.LoaderFileRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_loader_history")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoaderHistory implements Serializable {

    @Id
    @SequenceGenerator(name = "t_loader_history_seq_id", sequenceName = "t_loader_history_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_loader_history_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private String schemaName;
    private String tableName;
    private String fileName;
    private OffsetDateTime loadStartTime;
    private Long totalRowCount;
    private Long loadedRowCount;
    private Long failedRowCount;
    private OffsetDateTime loadEndTime;
    private String loadMessage;
    private Boolean isLoaded;
    private OffsetDateTime createdTime;

    public static LoaderHistory getEntity(LoaderFileRecord record) {
        LoaderHistory entity = new LoaderHistory();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setSchemaName(record.getSchemaName());
        entity.setTableName(record.getTableName());
        entity.setFileName(record.getFile().getName());
        entity.setCreatedTime(OffsetDateTime.now());
        return entity;
    }

}
