package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CounterDefineRecord {

    private String elementType;
    private String counterGroupType;
    private String counterGroupKey; //objectKey of ParseTable
    private String counterKey; //objectKey of ParseColumn

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CounterDefineRecord)) return false;

        CounterDefineRecord that = (CounterDefineRecord) o;
        return Objects.equals(this.elementType, that.elementType) &&
                Objects.equals(this.counterGroupType, that.counterGroupType) &&
                Objects.equals(this.counterGroupKey, that.counterGroupKey) &&
                Objects.equals(this.counterKey, that.counterKey);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elementType, counterGroupType, counterGroupKey, counterKey);
    }
}
