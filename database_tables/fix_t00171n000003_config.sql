-- Fix 1 (CRITICAL): t_parse_column.parse_table_id was set to all_table_id (75)
--   but should be t_parse_table.id (102).
--   ParseMapper groups columns by parse_table_id and looks them up by parse_table.id,
--   so mismatch leaves parseColumns empty → NoSuchElementException in loader.
UPDATE etl.t_parse_column
SET parse_table_id = 102
WHERE flow_id = 171
  AND parse_table_id = 75;

-- Fix 2: date_column_index must point to fragment_date column (column_order_id = 19),
--   not to line_index (column_order_id = 1).
--   Wrong value causes ContentDateReader NullPointerException (dateFormat = null).
UPDATE etl.t_parse_table
SET date_column_index = 19
WHERE id = 102;

-- Fix 3: column_formula required by ContentDateReader to parse the date value
--   from the .unl file for content date discovery.
UPDATE etl.t_parse_column
SET column_formula = 'yyyy-MM-dd HH:mmZ'
WHERE flow_id = 171
  AND parse_table_id = 102
  AND object_key = 'etlApp.constant_fragmentDate';
