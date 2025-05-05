package com.ttgint.library.model;

import com.ttgint.library.enums.ConnectionProtocol;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_server")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Server implements Serializable {

    @Id
    @SequenceGenerator(name = "t_server_seq_id", sequenceName = "t_server_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_server_seq_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    private ConnectionProtocol connectionProtocol;
    private String ip;
    private String userName;
    private String userPass;
    private Integer connectionPort;
    private String serverTag;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}