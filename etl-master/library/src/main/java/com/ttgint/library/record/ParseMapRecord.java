package com.ttgint.library.record;

import com.ttgint.library.model.ParseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseMapRecord {

    private ParseTableRecord parseTable;
    private List<ParseColumnRecord> parseColumns = new ArrayList<>();

    public static ParseMapRecord getRecord(ParseTable table) {
        ParseMapRecord record = new ParseMapRecord();
        record.setParseTable(ParseTableRecord.getRecord(table));
        return record;
    }
}
