package com.ttgint.library.model;

import com.ttgint.library.record.DecompressRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_decompress_error")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DecompressError implements Serializable {

    @Id
    @SequenceGenerator(name = "t_decompress_error_seq_id", sequenceName = "t_decompress_error_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_decompress_error_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private String errorCode;
    private String sourceFile;
    private String targetFile;
    private String errorMessage;
    private String errorDetail;
    private OffsetDateTime errorTime;

    public static DecompressError recordToEntity(DecompressRecord record,
                                                 String errorCode,
                                                 String targetFile,
                                                 String errorMessage) {
        DecompressError entity = new DecompressError();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setErrorCode(errorCode);
        entity.setSourceFile(record.getSourceFile().getName().replace(record.getFileId() + "^^", ""));
        entity.setTargetFile(targetFile.replace(record.getFileId() + "^^", ""));
        entity.setErrorMessage(errorMessage);
        entity.setErrorTime(OffsetDateTime.now());
        return entity;
    }

}