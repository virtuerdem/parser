package com.ttgint.scheduler.utils;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

@Slf4j
public class LogListenerUtils implements Runnable {

    private InputStream inputStream;
    private Consumer<String> logMethod;
    private String logFile;
    private ConcurrentLinkedQueue<String> lineQueue;
    private volatile boolean close = false;

    public LogListenerUtils(InputStream inputStream, Consumer<String> logMethod, String logFile) {
        this.lineQueue = new ConcurrentLinkedQueue<>();
        this.inputStream = inputStream;
        this.logMethod = logMethod;
        this.logFile = logFile;
    }

    @Override
    public void run() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {

            Thread readerThread = Thread.ofVirtual().start(() -> {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        lineQueue.add(line);
                        logMethod.accept(line);
                    }
                    Thread.sleep(1000);
                    close = true;
                } catch (Exception e) {
                    log.error("Error reading logs: {}", e.getMessage());
                }
            });

            while (true) {
                while (!lineQueue.isEmpty()) {
                    String line = lineQueue.poll();
                    if (line != null) {
                        writer.write(line);
                        writer.newLine();
                        writer.flush();
                    }
                }

                Thread.sleep(500);

                if (close && lineQueue.isEmpty()) {
                    log.info("{}: Log writing completed.", logFile);
                    break;
                }
            }
            readerThread.join();

        } catch (Exception e) {
            log.error("Error writing logs: {}", e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
