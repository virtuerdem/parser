package com.ttgint.library.record;

import jakarta.persistence.ParameterMode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class StoredProcedureParamRecord {

    Class classType;
    ParameterMode parameterMode;
    String parameter;

}
