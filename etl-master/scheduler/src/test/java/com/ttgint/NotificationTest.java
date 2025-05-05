package com.ttgint;

import com.ttgint.library.enums.NotificationType;
import com.ttgint.library.exception.EtlException;
import com.ttgint.library.model.Notification;
import com.ttgint.library.repository.NotificationRepository;
import com.ttgint.scheduler.SchedulerApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy
@ComponentScan("com.ttgint.*")
@SpringBootTest(classes = SchedulerApplication.class)
public class NotificationTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Test
    void generateNotificationTest() {

        Notification n = new Notification();
        n.setCode("0002");
        n.setType(NotificationType.INFO);
        n.setDescription("Flow manager changed!");
        n.setResolution("No need action.");
        n.setMailTo("ttgint@gmail.com");

        notificationRepository.save(n);
    }


    @Test
    void createExceptionTest() {

        try {
            int result = 10 / 0;
        } catch (Exception e) {
            throw new EtlException("0000", e.getMessage());
        }
    }

}
