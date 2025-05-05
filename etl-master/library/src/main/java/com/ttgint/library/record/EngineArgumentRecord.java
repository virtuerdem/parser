package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.boot.ApplicationArguments;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class EngineArgumentRecord {

    private Long flowId;
    private String flowProcessCode;
    private ArrayList<String> flags = new ArrayList<>();

    public static EngineArgumentRecord getRecord(ApplicationArguments args) {
        EngineArgumentRecord argument = new EngineArgumentRecord();
        try {
            argument.setFlowId(Long.valueOf(args.getSourceArgs()[0]));
            argument.setFlowProcessCode(args.getSourceArgs()[1]);

            ArrayList<String> flags = new ArrayList<>();
            for (String arg : args.getSourceArgs()) {
                if (arg.trim().startsWith("--")) {
                    flags.add(arg);
                }
            }
            argument.setFlags(flags);
        } catch (Exception e) {
        }

        //Use For Local Test
        Long flowId = 9L;
        argument.setFlowId(flowId);
        argument.setFlowProcessCode(OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%1$" + 6 + "s", flowId).replace(' ', '0'));

        return argument;
    }
}
