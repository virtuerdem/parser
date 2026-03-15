-- Create physical tables for pm.t00164n000010 and pm.t00164n000011
-- t00164n000010 = report_detail_new (16 columns)
-- t00164n000011 = peak_report12 (14 columns)

CREATE TABLE IF NOT EXISTS pm.t00164n000010 (
    fragment_date      TIMESTAMPTZ,
    file_id            VARCHAR(100),
    line_index         VARCHAR,
    unique_row_code    VARCHAR(100),
    between_10_11      NUMERIC,
    between_11_12      NUMERIC,
    between_12_13      NUMERIC,
    between_13_14      NUMERIC,
    between_14_15      NUMERIC,
    busy_hour          NUMERIC,
    day                NUMERIC,
    greater_than_15    NUMERIC,
    less_than_10       NUMERIC,
    place_99_percent   NUMERIC,
    time_of_99         NUMERIC,
    total              NUMERIC
);

CREATE TABLE IF NOT EXISTS pm.t00164n000011 (
    fragment_date      TIMESTAMPTZ,
    file_id            VARCHAR(100),
    line_index         VARCHAR,
    unique_row_code    VARCHAR(100),
    a                  NUMERIC,
    adet_oran12        NUMERIC,
    b                  NUMERIC,
    c                  NUMERIC,
    c2                 NUMERIC,
    d                  NUMERIC,
    d2                 NUMERIC,
    d4                 NUMERIC,
    report_date        NUMERIC,
    sure_oran12        NUMERIC
);
