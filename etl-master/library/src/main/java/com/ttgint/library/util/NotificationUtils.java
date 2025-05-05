package com.ttgint.library.util;

import com.ttgint.library.enums.NotificationQueueStatus;
import com.ttgint.library.enums.NotificationType;
import com.ttgint.library.model.Notification;
import com.ttgint.library.model.NotificationQueue;
import com.ttgint.library.repository.NotificationQueueRepository;
import com.ttgint.library.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationUtils {

    @Value("${app.all.default-mail}")
    private String defaultMail;

    public static void create(String code) {
        new NotificationUtils().persistNotification(code, null);
    }

    public static void create(String code, String detail) {
        new NotificationUtils().persistNotification(code, detail);
    }

    private void persistNotification(String code, String detail) {

        NotificationRepository notificationRepository = ApplicationContextUtils.getSingleBeanOfType(NotificationRepository.class);
        NotificationQueueRepository notificationQueueRepository = ApplicationContextUtils.getSingleBeanOfType(NotificationQueueRepository.class);

        Notification config = notificationRepository.findByCode(code).orElseGet(this::getDefaultNotificationConfig);

        if (!config.getIsActive()) {
            return;
        }

        NotificationQueue queueEntry = NotificationQueue.builder()
                .code(code)
                .type(config.getType())
                .title(config.getDescription())
                .mailTo(config.getMailTo())
                .mailCc(config.getMailCc())
                .mailBody(buildMailBody(detail, config))
                .retry(0L)
                .status(NotificationQueueStatus.PENDING)
                .createdTime(OffsetDateTime.now())
                .updatedTime(OffsetDateTime.now())
                .build();

        notificationQueueRepository.save(queueEntry);
    }

    private Notification getDefaultNotificationConfig() {
        return Notification.builder()
                .code("RUNTIME_0000")
                .type(NotificationType.UNKNOWN)
                .description("System Error Occurred")
                .mailTo(defaultMail)
                .isActive(true)
                .build();
    }

    private String buildMailBody(String detail, Notification config) {
        return String.format(
                "<h2>%s</h2>" +
                        "<table style='border-collapse: collapse; width: 100%%;'>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Error Code:</td><td style='padding: 6px;'>%s</td></tr>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Type:</td><td style='padding: 6px;'>%s</td></tr>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Description:</td><td style='padding: 6px;'>%s</td></tr>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Resolution:</td><td style='padding: 6px;'>%s</td></tr>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Time:</td><td style='padding: 6px;'>%s</td></tr>" +
                        "<tr><td style='text-align: right; vertical-align: top; padding: 6px; font-weight: bold;'>Detail:</td><td style='padding: 6px;'><pre style='margin:0;'>%s</pre></td></tr>" +
                        "</table>",
                config.getDescription(),
                config.getCode(),
                config.getType().getValue(),
                config.getDescription(),
                config.getResolution() != null ? config.getResolution() : "Please check system logs!",
                OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss (O)")),
                detail
        );
    }

}
