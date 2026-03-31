-- ═══════════════════════════════════════════════════════════════════════════
-- Setup: VF_TR_D_HW_MW_REPORT-PM  →  NE_Report parsing
-- Engine bean  : HW_MW_REPORT_PM_PARSE  (HwMwReportPmParseEngine)
-- Handler      : HwMwReportPmParseHandler
-- Branch       : VF_TR_D_HW_MW_REPORT  (branch_id = 123)
-- Flow         : flow_id = 172  (parse_engine already has id=172, component_id=130)
-- Parse table  : t00172n000001
-- Object key   : NE_Report
-- ═══════════════════════════════════════════════════════════════════════════

-- Step 1: Register the Spring component bean in t_parse_component
--   id=130 is referenced by t_parse_engine for flow_id=172
INSERT INTO etl.t_parse_component (id, component_code, is_active)
VALUES (130, 'HW_MW_REPORT_PM_PARSE', true)
ON CONFLICT (id) DO UPDATE
    SET component_code = EXCLUDED.component_code,
        is_active      = EXCLUDED.is_active;

-- Step 2: Ensure the flow exists in t_flow for VF_TR_D_HW_MW_REPORT-PM
--   (branch_id=123 = VF_TR_D_HW_MW_REPORT)
INSERT INTO etl.t_flow (id, branch_id, flow_code, is_active)
VALUES (172, 123, 'VF_TR_D_HW_MW_REPORT-PM', true)
ON CONFLICT (id) DO UPDATE
    SET branch_id = EXCLUDED.branch_id,
        flow_code = EXCLUDED.flow_code,
        is_active = EXCLUDED.is_active;

-- Step 3: Register the NE_Report table in t_all_table (for DB table generation)
INSERT INTO etl.t_all_table
    (id, flow_id, schema_name, table_name, table_name_alias, object_key,
     data_interval, time_period, need_refresh, is_generated, is_failed, is_active, created_time)
VALUES (76, 172, 'pm', 't00172n000001', 't172n1', 'NE_Report',
        1, 1, false, true, false, true, now())
ON CONFLICT (id) DO NOTHING;

-- Step 4: Register the NE_Report table in t_parse_table (for parse mapping)
--   date_column_index=1 → fragment_date at column_order_id=1
INSERT INTO etl.t_parse_table
    (id, flow_id, all_table_id, schema_name, table_name, object_key,
     date_column_index, date_column_name, result_file_delimiter, is_active, created_time)
VALUES (103, 172, 76, 'pm', 't00172n000001', 'NE_Report',
        1, 'fragment_date', '|', true, now())
ON CONFLICT (id) DO NOTHING;

-- Step 5: Pre-define CONSTANT columns in t_parse_column
--   These are required by the loader (LoaderFileRecord) and content-date discovery.
--   VARIABLE columns (actual NE_Report CSV headers) will be auto-discovered by
--   autoCounterDefine on the first parse run and must be added separately.
INSERT INTO etl.t_parse_column
    (flow_id, parse_table_id, schema_name, table_name, column_name,
     object_key, model_type, column_order_id, column_type, column_formula,
     is_default_value, is_column_gen, is_column_agg, is_active, created_time)
VALUES
    -- fragment_date: date_column_index=1 references this column_order_id
    (172, 103, 'pm', 't00172n000001', 'fragment_date',
     'etlApp.constant_fragmentDate', 'CONSTANT', 1, 'TIMESTAMPTZ', 'yyyy-MM-dd HH:mmZ',
     false, false, false, true, now()),

    -- file_date: collection/export time from filename
    (172, 103, 'pm', 't00172n000001', 'file_date',
     'etlApp.constant_fileDate', 'CONSTANT', 2, 'TIMESTAMPTZ', 'yyyy-MM-dd HH:mmZ',
     false, false, false, true, now()),

    -- file_id: numeric prefix before ^^ in the filename
    (172, 103, 'pm', 't00172n000001', 'file_id',
     'etlApp.info_fileId', 'INFO', 3, 'VARCHAR', NULL,
     false, false, false, true, now()),

    -- line_index: source file line number
    (172, 103, 'pm', 't00172n000001', 'line_index',
     'etlApp.info_lineIndex', 'INFO', 4, 'VARCHAR', NULL,
     false, false, false, true, now()),

    -- unique_row_code: deduplication key
    (172, 103, 'pm', 't00172n000001', 'unique_row_code',
     'etlApp.info_uniqueRowCode', 'INFO', 5, 'VARCHAR', NULL,
     false, false, false, true, now()),

    -- unique_row_hash_code: hash-based deduplication key
    (172, 103, 'pm', 't00172n000001', 'unique_row_hash_code',
     'etlApp.info_uniqueRowHashCode', 'INFO', 6, 'VARCHAR', NULL,
     false, false, false, true, now())
ON CONFLICT DO NOTHING;

-- ═══════════════════════════════════════════════════════════════════════════
-- After the first parse run:
--   1. autoCounterDefine populates t_all_counter with discovered CSV column keys.
--   2. Run the generate_tables_from_counter script (or insert_flowXXX_from_counter.sql)
--      to create the physical DB table (t00172n000001) and add VARIABLE columns
--      to t_all_column and t_parse_column.
-- ═══════════════════════════════════════════════════════════════════════════
