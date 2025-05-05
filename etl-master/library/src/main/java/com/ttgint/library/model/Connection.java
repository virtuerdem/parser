package com.ttgint.library.model;

import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.enums.PathWalkMethod;
import com.ttgint.library.enums.TimeUnit;
import com.ttgint.library.enums.TransferWorkType;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_connection")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Connection implements Serializable {

    @Id
    @SequenceGenerator(name = "t_connection_seq_id", sequenceName = "t_connection_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_connection_seq_id")
    private Long id;
    private Long flowId;
    @ManyToOne
    private Server server;
    @ManyToOne
    private Path path;

    @Enumerated(EnumType.STRING)
    private TransferWorkType transferWorkType;
    @Enumerated(EnumType.STRING)
    private PathWalkMethod pathWalkMethod;
    @Enumerated(EnumType.STRING)
    private FileTimeFilter readFileTimeFilter;
    @Enumerated(EnumType.STRING)
    private FileTimeFilter transferFileTimeFilter;

    private Integer transferTryCountMinLimit;
    private Integer transferTryCountMaxLimit;
    @Enumerated(EnumType.STRING)
    private TimeUnit fragmentTimeMinLimitUnit;
    private Long fragmentTimeMinLimit;
    @Enumerated(EnumType.STRING)
    private TimeUnit fragmentTimeMaxLimitUnit;
    private Long fragmentTimeMaxLimit;
    private OffsetDateTime lastModifiedTime;
    private String connectionTag;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}