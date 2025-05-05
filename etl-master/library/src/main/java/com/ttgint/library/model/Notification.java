package com.ttgint.library.model;

import com.ttgint.library.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Table(name = "t_notification")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    @Id
    @SequenceGenerator(name = "t_notification_seq_id", sequenceName = "t_notification_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_notification_seq_id")
    private Long id;
    private String code;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String description;
    private String resolution;
    private String mailTo;
    private String mailCc;
    private Boolean isActive;

}