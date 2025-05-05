package com.ttgint.scheduler.cron;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LogfileCleanupScheduler {

    @Value("${app.scheduler.logPath}")
    private String logPath;

    @Value("${app.scheduler.logThresholdDays}")
    private long logThresholdDays;

    @Scheduled(cron = "0 0 * * * *")
    public void cleanLogsFiles() throws Exception {
        findFilesToDelete().forEach(this::deleteFiles);
    }

    private List<Path> findFilesToDelete() throws Exception {
        List<Path> filesToDelete = new ArrayList<>();
        Path rootPath = Paths.get(logPath);

        if (!Files.exists(rootPath)) {
            log.info("Log path does not exist: {}", logPath);
            return filesToDelete;
        }

        Files.walkFileTree(rootPath, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                try {

                    Path relativePath = rootPath.relativize(file);
                    if (relativePath.getNameCount() >= 4) {
                        int year = Integer.parseInt(relativePath.getName(0).toString());
                        int month = Integer.parseInt(relativePath.getName(1).toString());
                        int day = Integer.parseInt(relativePath.getName(2).toString());
                        int flowId = Integer.parseInt(relativePath.getName(3).toString());
                        String filename = relativePath.getName(4).toString();

                        OffsetDateTime fileTime = OffsetDateTime.now()
                                .withYear(year)
                                .withMonth(month)
                                .withDayOfMonth(day)
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0);

                        OffsetDateTime thresholdTime = OffsetDateTime.now().minusDays(logThresholdDays).withMinute(0).withSecond(0).withNano(0);

                        if (fileTime.isBefore(thresholdTime)) {
                            filesToDelete.add(file);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) {
                return FileVisitResult.CONTINUE;
            }
        });

        return filesToDelete;
    }

    private void deleteFiles(Path file) {
        try {
            Files.delete(file);
            deleteEmptyParentDirectories(file.getParent());
        } catch (Exception e) {
            // dosya zaten silinmiş
        }
    }

    private void deleteEmptyParentDirectories(Path directory) throws IOException {
        Path rootPath = Paths.get(logPath);

        // silinen log dosyasına ait klasör boş ise sil
        while (directory != null && !directory.equals(rootPath) && Files.exists(directory)) {
            try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
                if (!dirStream.iterator().hasNext()) {
                    Files.delete(directory);
                    directory = directory.getParent();
                } else {
                    break;
                }
            } catch (IOException e) {
                break;
            }
        }
    }

}
