package com.ttgint.library.validation;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class Validation implements Runnable {

    protected final String sourceFilePath;

    public Validation(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    @Override
    public void run() {
        validate();
    }

    protected abstract void validate();

    protected String removeInvalidXMLCharacters(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        StringBuilder output = new StringBuilder(input.length());
        for (char current : input.toCharArray()) {
            if (isValidXMLCharacter(current)) {
                output.append(current);
            }
        }
        return output.toString();
    }

    protected boolean isValidXMLCharacter(char ch) {
        return (ch == 0x9) ||
                (ch == 0xA) ||
                (ch == 0xD) ||
                (ch >= 0x20 && ch <= 0xD7FF) ||
                (ch >= 0xE000 && ch <= 0xFFFD) ||
                (ch >= 0x10000 && ch <= 0x10FFFF);
    }
}
