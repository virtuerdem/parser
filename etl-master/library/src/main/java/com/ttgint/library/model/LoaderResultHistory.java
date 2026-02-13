package com.ttgint.library.model;

import com.ttgint.library.record.LoaderFileRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_loader_result_history")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoaderResultHistory implements Serializable {

    @Id
    @SequenceGenerator(name = "t_loader_result_history_seq_id", sequenceName = "t_loader_result_history_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_loader_result_history_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private String schemaName;
    private String tableName;
    private String fileName;
    private OffsetDateTime fragmentDate;
    private Long totalRowCount;
    private Long loadedRowCount;
    private Long failedRowCount;
    private Boolean isLoaded;
    private OffsetDateTime createdTime;

    public static LoaderResultHistory getEntity(LoaderFileRecord record,
                                                OffsetDateTime fragmentDate,
                                                Long totalRowCount,
                                                Long loadedRowCount,
                                                Long failedRowCount) {
        LoaderResultHistory entity = new LoaderResultHistory();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setSchemaName(record.getSchemaName());
        entity.setTableName(record.getTableName());
        entity.setFileName(record.getFile().getName());
        entity.setFragmentDate(fragmentDate);
        entity.setTotalRowCount(totalRowCount);
        entity.setLoadedRowCount(loadedRowCount);
        entity.setFailedRowCount(failedRowCount);
        entity.setIsLoaded(totalRowCount > 0);
        entity.setCreatedTime(OffsetDateTime.now());
        return entity;
    }

}
