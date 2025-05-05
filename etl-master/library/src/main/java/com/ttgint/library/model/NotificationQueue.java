package com.ttgint.library.model;

import com.ttgint.library.enums.NotificationQueueStatus;
import com.ttgint.library.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Table(name = "t_notification_queue")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class NotificationQueue {

    @Id
    @SequenceGenerator(name = "t_notification_queue_seq_id", sequenceName = "t_notification_queue_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_notification_queue_seq_id")
    private Long id;
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    private String code;
    private String title;
    private String mailTo;
    private String mailCc;
    @Column(name = "mail_body", columnDefinition = "TEXT")
    private String mailBody;
    private Long retry;
    @Enumerated(EnumType.STRING)
    private NotificationQueueStatus status;

    private OffsetDateTime createdTime;
    private OffsetDateTime updatedTime;

}