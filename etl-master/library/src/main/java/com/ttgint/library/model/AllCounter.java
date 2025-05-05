package com.ttgint.library.model;

import com.ttgint.library.record.CounterDefineRecord;
import com.ttgint.library.record.ParseEngineRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_counter")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllCounter implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_counter_seq_id", sequenceName = "t_all_counter_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_counter_seq_id")
    private Long id;
    private Long companyId;
    private Long domainId;
    private Long organisationId;
    private Long vendorId;
    private Long unitId;
    private Long branchId;
    private Long flowId;

    private String nodeGroupType;
    private String counterGroupType;
    private String counterGroupKey; //objectKey of ParseTable
    private String counterKey; //objectKey of ParseColumn
    private String modelType;

    private String counterGroupLookup;
    private String counterLookup;

    private String counterGroupDescription;
    private String counterDescription;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public static AllCounter getRecord(ParseEngineRecord parseEngineRecord, CounterDefineRecord counterDefineRecord) {
        AllCounter record = new AllCounter();
        record.setCompanyId(parseEngineRecord.getCompanyId());
        record.setDomainId(parseEngineRecord.getDomainId());
        record.setOrganisationId(parseEngineRecord.getOrganisationId());
        record.setVendorId(parseEngineRecord.getVendorId());
        record.setUnitId(parseEngineRecord.getUnitId());
        record.setBranchId(parseEngineRecord.getBranchId());
        record.setFlowId(parseEngineRecord.getFlowId());

        record.setCounterGroupType(counterDefineRecord.getCounterGroupType());
        record.setCounterGroupKey(counterDefineRecord.getCounterGroupKey());
        record.setCounterKey(counterDefineRecord.getCounterKey());

        record.setModelType(
                (!counterDefineRecord.getCounterKey().startsWith("etlApp.") ? "VARIABLE"
                        : counterDefineRecord.getCounterKey().split("\\.")[1].toUpperCase()));

        record.setIsActive(true);
        record.setCreatedTime(OffsetDateTime.now());

        return record;
    }
}
