#!/usr/bin/env python3
"""
t_all_counter tablosundaki kayıtları baz alarak:
  - t_all_table
  - t_all_column
  - t_parse_table
  - t_parse_column
tablolarına eklenecek INSERT SQL'lerini üretir.

Kullanım:
  python3 generate_tables_from_counter.py --flow-id 164
  python3 generate_tables_from_counter.py --flow-id 164 --schema pm
"""

import argparse
import csv
import os
import re
import xml.etree.ElementTree as ET
from collections import defaultdict
from datetime import datetime

# ── Sabitler ────────────────────────────────────────────────────────────────
DB_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "database_tables")
NOW = datetime.now().strftime("%Y-%m-%d %H:%M:%S.000 +0300")

# Özel INFO counter_key'lerinin sıralama önceliği (küçük sayı = önce)
INFO_ORDER = {
    "etlApp.constant_fragmentDate": 1,
    "etlApp.info_fileId":           2,
    "etlApp.info_lineIndex":        3,
    "etlApp.info_uniqueRowCode":    4,
    "etlApp.info_uniqueRowHashCode": 9999,   # her zaman en sona
}

# ── Yardımcı fonksiyonlar ────────────────────────────────────────────────────

def read_csv(filename):
    """database_tables/ altındaki CSV'yi okur, satırları dict listesi olarak döndürür."""
    path = os.path.join(DB_DIR, filename)
    if not os.path.exists(path):
        print(f"  [UYARI] Dosya bulunamadı: {path}")
        return []
    with open(path, newline="", encoding="utf-8") as f:
        return list(csv.DictReader(f))

def find_csv(prefix):
    """database_tables/ altında prefix ile başlayan ilk CSV dosyasını bulur."""
    for fname in sorted(os.listdir(DB_DIR)):
        if fname.startswith(prefix) and fname.endswith(".csv"):
            return fname
    return None

def find_xml(prefix):
    """database_tables/ altında prefix ile başlayan ilk XML dosyasını bulur."""
    for fname in sorted(os.listdir(DB_DIR)):
        if fname.startswith(prefix) and fname.endswith(".xml"):
            return fname
    return None

def max_id_xml(filename, id_tag="id"):
    """XML dosyasındaki max id değerini döndürür."""
    path = os.path.join(DB_DIR, filename)
    if not os.path.exists(path):
        return 0
    try:
        tree = ET.parse(path)
        root = tree.getroot()
        ids = []
        for elem in root:
            id_elem = elem.find(id_tag)
            if id_elem is not None and id_elem.text and id_elem.text.strip().isdigit():
                ids.append(int(id_elem.text.strip()))
        return max(ids) if ids else 0
    except ET.ParseError:
        return 0

def max_id(rows, field="id"):
    ids = []
    for r in rows:
        val = r.get(field, "").strip().strip('"')
        if val.isdigit():
            ids.append(int(val))
    return max(ids) if ids else 0

def camel_to_snake(name):
    """camelCase → snake_case dönüşümü."""
    s = re.sub(r"([A-Z])", r"_\1", name)
    return s.lower().strip("_")

def derive_column_name(counter_key):
    """counter_key'den DB sütun adı türet."""
    if counter_key.startswith("etlApp.constant_"):
        raw = counter_key.replace("etlApp.constant_", "")
        return camel_to_snake(raw)
    if counter_key.startswith("etlApp.info_"):
        raw = counter_key.replace("etlApp.info_", "")
        return camel_to_snake(raw)
    # VARIABLE: büyük harfleri küçük yap
    return counter_key.lower()

def get_column_type(counter_key, model_type):
    if counter_key == "etlApp.constant_fragmentDate":
        return "TIMESTAMPTZ"
    if model_type == "VARIABLE":
        return "NUMERIC"
    return "VARCHAR"

def get_column_formula(counter_key):
    if counter_key == "etlApp.constant_fragmentDate":
        return "yyyy-mm-dd hh24:miZ"
    return ""

def get_column_length(counter_key):
    if counter_key in ("etlApp.info_fileId", "etlApp.info_uniqueRowCode"):
        return "100"
    return ""

def get_column_length_int(counter_key):
    """t_parse_column için INTEGER column_length (NULL veya sayı)."""
    if counter_key in ("etlApp.info_fileId", "etlApp.info_uniqueRowCode"):
        return "100"
    return "NULL"

def sort_key(counter_key, model_type):
    """
    Sıralama:
      1 → etlApp.constant_fragmentDate
      2 → etlApp.info_fileId
      3 → etlApp.info_lineIndex
      4 → etlApp.info_uniqueRowCode
      5-998 → VARIABLE (alfabetik)
      9999 → etlApp.info_uniqueRowHashCode
    """
    if counter_key in INFO_ORDER:
        return (INFO_ORDER[counter_key], counter_key)
    if model_type == "VARIABLE":
        return (5, counter_key)
    # Bilinmeyen INFO/CONSTANT → ortaya koy
    return (500, counter_key)

def q(val):
    """None/boş değerleri NULL, diğerlerini 'tek tırnaklı' SQL string'e çevirir."""
    if val is None or str(val).strip() == "":
        return "NULL"
    escaped = str(val).replace("'", "''")
    return f"'{escaped}'"

# ── Ana fonksiyon ────────────────────────────────────────────────────────────

def generate(flow_id: int, schema: str, only_group: str = None):
    print(f"\n[INFO] flow_id={flow_id}, schema={schema}")

    # CSV dosyalarını bul
    f_counter = find_csv("t_all_counter")
    f_table   = find_csv("t_all_table")
    f_column  = find_csv("t_all_column")
    f_ptable  = find_csv("t_parse_table")
    f_pcolumn = find_xml("t_parse_column")

    if not all([f_counter, f_table, f_column, f_ptable]):
        print("[HATA] Gerekli CSV dosyaları bulunamadı.")
        return

    # Okuma
    all_counter_rows = [r for r in read_csv(f_counter)
                        if r.get("flow_id", "").strip().strip('"') == str(flow_id)]
    all_table_rows   = read_csv(f_table)
    all_column_rows  = read_csv(f_column)
    parse_table_rows = read_csv(f_ptable)

    print(f"  t_all_counter kayıt sayısı (flow {flow_id}): {len(all_counter_rows)}")

    if not all_counter_rows:
        print("[HATA] Bu flow_id için t_all_counter kaydı yok.")
        return

    # Mevcut tablolar (bu flow için)
    existing_tables = {
        r["object_key"].strip().strip('"'): r
        for r in all_table_rows
        if r.get("flow_id", "").strip().strip('"') == str(flow_id)
    }
    print(f"  Mevcut t_all_table kayıtları: {list(existing_tables.keys())}")

    # Bu flow için kaç tablo var → yeni tablo index'i hesapla
    flow_table_count = len(existing_tables)

    # Max ID'ler
    next_table_id    = max_id(all_table_rows)   + 1
    next_column_id   = max_id(all_column_rows)  + 1
    next_ptable_id   = max_id(parse_table_rows) + 1
    next_pcolumn_id  = (max_id_xml(f_pcolumn) if f_pcolumn else 0) + 1

    # Gruplama: counter_group_key → counter listesi
    groups = defaultdict(list)
    for row in all_counter_rows:
        gk = row.get("counter_group_key", "").strip().strip('"')
        if gk:
            groups[gk].append(row)

    # İşlenecek gruplar
    target_groups = [only_group] if only_group else sorted(groups.keys())

    sql_blocks = []
    new_table_count = 0

    for group_key in target_groups:
        if group_key not in groups:
            print(f"  [UYARI] Grup bulunamadı: {group_key}")
            continue

        if group_key in existing_tables:
            print(f"  [SKIP] Zaten mevcut: {group_key}")
            continue

        # Yeni tablo
        flow_table_count += 1
        new_table_count  += 1
        table_name  = f"t{flow_id:05d}n{flow_table_count:06d}"
        table_alias = f"t{flow_id}n{flow_table_count}"
        table_id    = next_table_id
        next_table_id += 1
        ptable_id   = next_ptable_id
        next_ptable_id += 1

        print(f"  [NEW] {group_key} → {table_name} (id={table_id})")

        lines = [f"-- ═══════════════════════════════════════════════════",
                 f"-- {group_key}",
                 f"-- ═══════════════════════════════════════════════════\n"]

        # ── t_all_table ────────────────────────────────────────────
        lines.append("-- t_all_table")
        lines.append(
            f"INSERT INTO t_all_table ("
            f"id, flow_id, db_catalog, schema_name, table_name, tablespace_name, "
            f"table_name_alias, table_name_lookup, table_description, object_key, "
            f"object_key2, object_type, element_type, node_type, item_type, table_type, "
            f"sub_table_type, network_type, sub_network_type, data_type, data_interval, "
            f"time_period, time_delay, need_refresh, is_generated, is_failed, is_active, "
            f"created_time, created_by, updated_time, updated_by, extra_info, flow_code"
            f") VALUES ("
            f"{table_id}, {flow_id}, NULL, {q(schema)}, {q(table_name)}, NULL, "
            f"{q(table_alias)}, NULL, NULL, {q(group_key)}, "
            f"NULL, NULL, NULL, NULL, NULL, NULL, "
            f"NULL, NULL, NULL, NULL, 1, "
            f"1, NULL, false, true, false, true, "
            f"{q(NOW)}, NULL, {q(NOW)}, NULL, NULL, NULL"
            f");\n"
        )

        # ── t_parse_table ──────────────────────────────────────────
        lines.append("-- t_parse_table")
        lines.append(
            f"INSERT INTO t_parse_table ("
            f"id, flow_id, all_table_id, schema_name, table_name, object_key, "
            f"object_type, element_type, node_type, item_type, table_type, sub_table_type, "
            f"network_type, sub_network_type, group_type, data_type, data_source, "
            f"table_group, data_group, date_column_index, date_column_name, "
            f"result_file_delimiter, loader_target, is_active, "
            f"created_time, created_by, updated_time, updated_by, extra_info, flow_code, "
            f"measurement_key, object_id, object_id_lookup, object_key_lookup, "
            f"sub_element_type, sub_item_type"
            f") VALUES ("
            f"{ptable_id}, {flow_id}, {table_id}, {q(schema)}, {q(table_name)}, {q(group_key)}, "
            f"NULL, NULL, NULL, NULL, NULL, NULL, "
            f"NULL, NULL, NULL, NULL, NULL, "
            f"NULL, NULL, 1, 'fragment_date', "
            f"'|', NULL, true, "
            f"{q(NOW)}, NULL, {q(NOW)}, NULL, NULL, NULL, "
            f"NULL, NULL, NULL, NULL, "
            f"NULL, NULL"
            f");\n"
        )

        # ── t_all_column ───────────────────────────────────────────
        counters = groups[group_key]
        counters_sorted = sorted(counters,
                                 key=lambda r: sort_key(
                                     r.get("counter_key", "").strip().strip('"'),
                                     r.get("model_type", "").strip().strip('"')
                                 ))

        col_values = []
        pcol_values = []
        for order_idx, counter in enumerate(counters_sorted, start=1):
            counter_key  = counter.get("counter_key", "").strip().strip('"')
            model_type   = counter.get("model_type", "").strip().strip('"')
            is_active_c  = counter.get("is_active", "true").strip().strip('"')

            column_name    = derive_column_name(counter_key)
            column_type    = get_column_type(counter_key, model_type)
            column_formula = get_column_formula(counter_key)
            column_length  = get_column_length(counter_key)

            col_id = next_column_id
            next_column_id += 1

            col_values.append(
                f"  ({col_id}, {flow_id}, {table_id}, {q(schema)}, {q(table_name)}, "
                f"{q(column_name)}, {q(counter_key)}, NULL, NULL, NULL, "
                f"{order_idx}, {q(column_type)}, {q(column_formula)}, {q(column_length)}, {q(model_type)}, "
                f"false, NULL, false, NULL, "
                f"false, true, false, {is_active_c}, "
                f"{q(NOW)}, NULL, {q(NOW)}, NULL, NULL, NULL)"
            )

            pcol_id = next_pcolumn_id
            next_pcolumn_id += 1
            col_length_int = get_column_length_int(counter_key)
            col_formula_q  = q(column_formula) if column_formula else "NULL"

            pcol_values.append(
                f"  ({pcol_id}, {flow_id}, {ptable_id}, {q(schema)}, {q(table_name)}, "
                f"{q(column_name)}, {q(counter_key)}, NULL, "
                f"{q(model_type)}, {order_idx}, {q(column_type)}, {col_length_int}, {col_formula_q}, "
                f"false, NULL, false, NULL, "
                f"{is_active_c}, "
                f"{q(NOW)}, NULL, {q(NOW)}, NULL, NULL)"
            )

        lines.append("-- t_all_column")
        lines.append(
            f"INSERT INTO t_all_column ("
            f"id, flow_id, all_table_id, schema_name, table_name, "
            f"column_name, object_key, object_key2, column_name_lookup, column_description, "
            f"column_order_id, column_type, column_formula, column_length, model_type, "
            f"is_column_gen, column_gen_formula, is_column_agg, column_agg_formula, "
            f"need_refresh, is_generated, is_failed, is_active, "
            f"created_time, created_by, updated_time, updated_by, extra_info, flow_code"
            f") VALUES\n" + ",\n".join(col_values) + ";\n"
        )

        lines.append("-- t_parse_column")
        lines.append(
            f"INSERT INTO t_parse_column ("
            f"id, flow_id, parse_table_id, schema_name, table_name, "
            f"column_name, object_key, object_key2, "
            f"model_type, column_order_id, column_type, column_length, column_formula, "
            f"is_default_value, column_default_value, is_column_gen, column_gen_formula, "
            f"is_active, "
            f"created_time, created_by, updated_time, updated_by, extra_info"
            f") VALUES\n" + ",\n".join(pcol_values) + ";\n"
        )

        sql_blocks.append("\n".join(lines))

    # ── Çıktı ──────────────────────────────────────────────────────────────
    if not sql_blocks:
        print("\n[INFO] Eklenecek yeni kayıt yok.")
        return

    output_path = os.path.join(DB_DIR, f"insert_flow{flow_id}_from_counter.sql")
    full_sql = (
        f"-- Otomatik üretildi: generate_tables_from_counter.py\n"
        f"-- Tarih: {NOW}\n"
        f"-- flow_id: {flow_id}\n"
        f"-- Yeni tablo sayısı: {new_table_count}\n\n"
        + "\n\n".join(sql_blocks)
    )

    with open(output_path, "w", encoding="utf-8") as f:
        f.write(full_sql)

    print(f"\n[OK] SQL dosyası oluşturuldu: {output_path}")
    print(f"     Toplam {new_table_count} yeni grup (table + parse_table + all_column + parse_column) eklendi.")


# ── CLI ──────────────────────────────────────────────────────────────────────

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description="t_all_counter → t_all_table / t_all_column / t_parse_table INSERT SQL üretici"
    )
    parser.add_argument("--flow-id",  type=int, required=True, help="İşlenecek flow_id (örn: 164)")
    parser.add_argument("--schema",   type=str, default="pm",   help="DB schema adı (varsayılan: pm)")
    parser.add_argument("--group",    type=str, default=None,
                        help="Sadece belirtilen counter_group_key'i işle (örn: report12)")
    args = parser.parse_args()

    generate(flow_id=args.flow_id, schema=args.schema, only_group=args.group)
