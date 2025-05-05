package com.ttgint.library.util;

import com.ttgint.library.record.ParseColumnRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.library.repository.ParseColumnRepository;
import com.ttgint.library.repository.ParseTableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ParseMapper {

    private final ParseTableRepository parseTableRepository;
    private final ParseColumnRepository parseColumnRepository;

    private List<ParseMapRecord> parserMaps;

    public ParseMapper(ParseTableRepository parseTableRepository, ParseColumnRepository parseColumnRepository) {
        this.parseTableRepository = parseTableRepository;
        this.parseColumnRepository = parseColumnRepository;
    }

    public List<ParseMapRecord> getTables(Long flowId) {
        parserMaps
                = parseTableRepository.findAllByFlowIdAndIsActive(flowId, true)
                .stream()
                .map(ParseMapRecord::getRecord)
                .collect(Collectors.toList());

        Map<Long, List<ParseColumnRecord>> columns
                = parseColumnRepository.findAllByFlowIdAndIsActive(flowId, true)
                .stream()
                .map(ParseColumnRecord::getRecord)
                .collect(Collectors.groupingBy(ParseColumnRecord::getParseTableId));

        parserMaps
                .forEach(e -> {
                    if (columns.containsKey(e.getParseTable().getParseTableId())) {
                        e.setParseColumns(
                                columns.get(e.getParseTable().getParseTableId())
                                        .stream()
                                        .sorted(Comparator.comparing(ParseColumnRecord::getColumnOrderId))
                                        .collect(Collectors.toList())
                        );
                    }
                });
        return parserMaps;
    }

    public ParseMapRecord getMapByTableName(String tableName) {
        return parserMaps
                .stream()
                .filter(e -> e.getParseTable().getTableName().equals(tableName))
                .findFirst()
                .orElse(null);
    }

    public ParseMapRecord getMapByObjectKey(String objectKey) {
        return parserMaps
                .stream()
                .filter(e -> e.getParseTable().getObjectKey().equals(objectKey))
                .findFirst()
                .orElse(null);
    }

}

