package com.ttgint.library.exception;

import com.ttgint.library.enums.NotificationQueueStatus;
import com.ttgint.library.enums.NotificationType;
import com.ttgint.library.model.Notification;
import com.ttgint.library.model.NotificationQueue;
import com.ttgint.library.repository.NotificationQueueRepository;
import com.ttgint.library.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Aspect
@Component
public class ExceptionAspect {

    @Value("${app.all.default-mail}")
    private String defaultMail;

    private final NotificationRepository notificationRepository;
    private final NotificationQueueRepository notificationQueueRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @AfterThrowing(pointcut = "execution(* com.ttgint..*(..))", throwing = "ex")
    public void handleException(Exception ex) {

        Notification cfg = notificationRepository.findByCode("RUNTIME").orElseGet(this::getDefaultNotificationConfig);

        NotificationQueue queueEntry = NotificationQueue.builder()
                .code(cfg.getCode())
                .type(cfg.getType())
                .title(cfg.getDescription())
                .mailTo(cfg.getMailTo())
                .mailCc(cfg.getMailCc())
                .mailBody(buildMailBody(ex, cfg))
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
                .build();
    }

    private String buildMailBody(Exception ex, Notification config) {
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
                getStackTrace(ex)
        );
    }

    private String getStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }
}