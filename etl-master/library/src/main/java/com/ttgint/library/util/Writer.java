package com.ttgint.library.util;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.FileOutputStream;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Getter
@Component
@Scope("singleton")
public class Writer {

    private final ConcurrentHashMap<String, FileOutputStream> outputStreams = new ConcurrentHashMap<>();

    public void sync(String fileName, String line) {
        synchronized (outputStreams) {
            try {
                if (!outputStreams.containsKey(fileName)) {
                    outputStreams.put(fileName, new FileOutputStream(fileName, true));
                }
                outputStreams.get(fileName).write(line.getBytes());
                outputStreams.get(fileName).flush();
            } catch (Exception exception) {
                log.error("! Writer sync fileName: {} ", fileName, exception);
            }
        }
    }

    public void async(String fileName, String line) {
        try (FileOutputStream output = new FileOutputStream(fileName, true)) {
            output.write(line.getBytes());
            output.flush();
        } catch (Exception exception) {
            log.error("! Writer async fileName: {} ", fileName, exception);
        }
    }

    public void closeStream(String fileName) {
        try {
            outputStreams.get(fileName).flush();
            outputStreams.get(fileName).close();
            outputStreams.remove(fileName);
        } catch (Exception exception) {
            log.error("! Writer closeStream fileName: {} ", fileName, exception);
        }
    }

    public void closeAllStreams() {
        outputStreams.keySet()
                .forEach(e -> {
                    try {
                        outputStreams.get(e).flush();
                        outputStreams.get(e).close();
                    } catch (Exception exception) {
                    }
                });
        outputStreams.clear();
    }

}
