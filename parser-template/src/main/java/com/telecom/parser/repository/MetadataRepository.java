package com.telecom.parser.repository;

import com.telecom.parser.model.TableMetadata;
import java.util.List;
import java.util.Map;

/**
 * Repository for table metadata operations
 */
public interface MetadataRepository {

    /**
     * Generate metadata tables from database schema
     * Activity Diagram: fetchTables()
     */
    void fetchTables(String schemaName);

    /**
     * Load table metadata from repository
     * Activity Diagram: getTables()
     *
     * @param vendor Vendor name (HUAWEI, ERICSSON, etc.)
     * @param technology Technology (4G, 5G, 3G)
     * @param dataType Data type (PM, CM, CONF)
     * @return Map of table name to TableMetadata
     */
    Map<String, TableMetadata> getTables(String vendor, String technology, String dataType);

    /**
     * Get network nodes by branch ID
     * Activity Diagram: Repository.getNetworkNodesByBranchId()
     *
     * @param branchId Branch ID
     * @return Map of node name to node ID
     */
    Map<String, Long> getNetworkNodesByBranchId(Long branchId);

    /**
     * Save auto counter definitions
     * Activity Diagram: Save auto counter definitions
     *
     * @param counterDefinitions Map of counter name to definition
     */
    void saveAutoCounterDefinitions(Map<String, String> counterDefinitions);
}
