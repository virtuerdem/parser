package com.ttgint.library.record;

import com.ttgint.library.model.Branch;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.ParseEngine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseEngineRecord {

    private Long companyId;
    private Long domainId;
    private Long organisationId;
    private Long vendorId;
    private Long unitId;
    private Long branchId;
    private Long flowId;
    private String flowCode;
    private String flowProcessCode;

    private Long id;

    private Boolean isActiveFetchTables;

    private Boolean isActivePreParse;
    private Integer preParseThreadCount;

    private Boolean havePrepareFileFeature;
    private Boolean decompress;
    private Boolean needDecompressResult;
    private Boolean validation;

    private Boolean isActiveOnParse;
    private Integer onParseThreadCount;

    private Boolean isActivePostParse;
    private Integer postParseThreadCount;

    private Boolean isActiveAutoCounter;

    private Boolean isActiveDiscoverContentDate;
    private Integer discoverContentDateThreadCount;
    private Boolean needContentDateResult;

    private Boolean isActiveCleanDuplicateApp;

    private Boolean isActiveLoader;
    private Integer loaderThreadCount;
    private Boolean needLoaderResult;

    private Boolean isActiveCleanDuplicateProc;

    private Boolean isActiveCallProcedure;
    private Boolean isActiveCallAggregate;
    private Boolean isActiveCallExport;

    private String resultFileExtension;

    private String rawPath;

    public static ParseEngineRecord getRecord(Branch branch, Flow flow, String flowProcessCode, ParseEngine engine) {
        ParseEngineRecord record = new ParseEngineRecord();
        record.setCompanyId(branch.getCompanyId());
        record.setDomainId(branch.getDomainId());
        record.setOrganisationId(branch.getOrganisationId());
        record.setVendorId(branch.getVendorId());
        record.setUnitId(branch.getUnitId());
        record.setBranchId(branch.getId());
        record.setFlowId(flow.getId());
        record.setFlowCode(flow.getFlowCode());
        record.setFlowProcessCode(flowProcessCode);

        record.setId(engine.getId());

        record.setIsActiveFetchTables(engine.getIsActiveFetchTables());

        record.setIsActivePreParse(engine.getIsActivePreParse());
        record.setPreParseThreadCount(engine.getPreParseThreadCount());

        record.setHavePrepareFileFeature(engine.getHavePrepareFileFeature());
        record.setDecompress(engine.getDecompress());
        record.setNeedDecompressResult(engine.getNeedDecompressResult());
        record.setValidation(engine.getValidation());

        record.setIsActiveOnParse(engine.getIsActiveOnParse());
        record.setOnParseThreadCount(engine.getOnParseThreadCount());

        record.setIsActivePostParse(engine.getIsActivePostParse());
        record.setPostParseThreadCount(engine.getPostParseThreadCount());

        record.setIsActiveAutoCounter(engine.getIsActiveAutoCounter());

        record.setIsActiveDiscoverContentDate(engine.getIsActiveDiscoverContentDate());
        record.setDiscoverContentDateThreadCount(engine.getDiscoverContentDateThreadCount());
        record.setNeedContentDateResult(engine.getNeedContentDateResult());

        record.setIsActiveCleanDuplicateApp(engine.getIsActiveCleanDuplicateApp());

        record.setIsActiveLoader(engine.getIsActiveLoader());
        record.setLoaderThreadCount(engine.getLoaderThreadCount());
        record.setNeedLoaderResult(engine.getNeedLoaderResult());

        record.setIsActiveCleanDuplicateProc(engine.getIsActiveCleanDuplicateProc());

        record.setIsActiveCallProcedure(engine.getIsActiveCallProcedure());
        record.setIsActiveCallAggregate(engine.getIsActiveCallAggregate());
        record.setIsActiveCallExport(engine.getIsActiveCallExport());

        record.setResultFileExtension(engine.getResultFileExtension());

        return record;
    }

}
