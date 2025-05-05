package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_parse_engine")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseEngine implements Serializable {

    @Id
    @SequenceGenerator(name = "t_parse_engine_seq_id", sequenceName = "t_parse_engine_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_parse_engine_seq_id")
    private Long id;
    private Long flowId;
    private Long parseComponentId;

    private Boolean isActiveFetchTables;
    private Boolean isActivePreParse;
    private Integer preParseThreadCount;
    private Boolean isActiveOnParse;
    private Integer onParseThreadCount;
    private Boolean isActivePostParse;
    private Integer postParseThreadCount;
    private Boolean isActiveAutoCounter;
    private Boolean isActiveDiscoverContentDate;
    private Integer discoverContentDateThreadCount;
    private Boolean needContentDateResult;
    private Integer loaderThreadCount;
    private Boolean needLoaderResult;

    private String resultFileExtension;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}