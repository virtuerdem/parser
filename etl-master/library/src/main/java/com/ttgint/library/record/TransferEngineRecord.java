package com.ttgint.library.record;

import com.ttgint.library.model.Branch;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.TransferEngine;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TransferEngineRecord {

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

    private Boolean preTransfer;
    private Integer preTransferThreadCount;
    private Boolean onTransfer;
    private Integer onTransferThreadCount;

    private Boolean preThread;
    private Boolean checkLastModifiedTime;
    private Boolean readFiles;
    private Boolean filterFiles;
    private Boolean setFileInfo;
    private Boolean cacheResults;
    private Boolean setLastModifiedTime;
    private Boolean clearRemoteFiles;
    private Boolean download;
    private Boolean postThread;

    private Boolean decompress;
    private Integer decompressThreadCount;
    private Boolean needDecompressResult;
    private Boolean validation;
    private Integer validationThreadCount;
    private Boolean postTransfer;
    private Integer postTransferThreadCount;

    private String rawPath;

    public static TransferEngineRecord getRecord(Branch branch, Flow flow, String flowProcessCode, TransferEngine engine) {
        TransferEngineRecord record = new TransferEngineRecord();
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
        record.setPreTransfer(engine.getPreTransfer());
        record.setPreTransferThreadCount(engine.getPreTransferThreadCount());
        record.setOnTransfer(engine.getOnTransfer());
        record.setOnTransferThreadCount(engine.getOnTransferThreadCount());

        record.setPreThread(engine.getPreThread());
        record.setCheckLastModifiedTime(engine.getCheckLastModifiedTime());
        record.setReadFiles(engine.getReadFiles());
        record.setFilterFiles(engine.getFilterFiles());
        record.setSetFileInfo(engine.getSetFileInfo());
        record.setCacheResults(engine.getCacheResults());
        record.setSetLastModifiedTime(engine.getSetLastModifiedTime());
        record.setClearRemoteFiles(engine.getClearRemoteFiles());
        record.setDownload(engine.getDownload());
        record.setPostThread(engine.getPostThread());

        record.setDecompress(engine.getDecompress());
        record.setDecompressThreadCount(engine.getDecompressThreadCount());
        record.setNeedDecompressResult(engine.getNeedDecompressResult());
        record.setValidation(engine.getValidation());
        record.setValidationThreadCount(engine.getValidationThreadCount());
        record.setPostTransfer(engine.getPostTransfer());
        record.setPostTransferThreadCount(engine.getPostTransferThreadCount());

        return record;
    }

}
