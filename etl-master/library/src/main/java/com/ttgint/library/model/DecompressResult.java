package com.ttgint.library.model;

import com.ttgint.library.record.DecompressRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.File;
import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_decompress_result")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DecompressResult implements Serializable {

    @Id
    @SequenceGenerator(name = "t_decompress_result_seq_id", sequenceName = "t_decompress_result_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_decompress_result_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private String fileId;
    private String sourceFileName;
    private OffsetDateTime fragmentTime;
    private String fileName;
    private Long fileSize;
    private OffsetDateTime decompressTime;

    public static DecompressResult recordToEntity(DecompressRecord record, File targetFile) {
        DecompressResult entity = new DecompressResult();
        entity.setFlowId(record.getFlowId());
        entity.setFlowProcessCode(record.getFlowProcessCode());
        entity.setFileId(record.getFileId());
        entity.setSourceFileName(record.getSourceFile().getName().replace(record.getFileId() + "^^", ""));
        entity.setFragmentTime(record.getFragmentTime());
        entity.setFileName(targetFile.getName().replace(record.getFileId() + "^^", ""));
        entity.setFileSize(targetFile.length());
        entity.setDecompressTime(OffsetDateTime.now());
        return entity;
    }

}