package com.ttgint.library.loader;

import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.record.LoaderFileRecord;
import com.ttgint.library.record.SqlLdrColumnPatternRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;

@Slf4j
public class OracleLoader extends Loader {

    private final HashMap<String, Long> badFileContentDateMap = new HashMap<>();

    public OracleLoader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        super(applicationContext, loaderFileRecord);
    }

    @Override
    public void loader() throws Exception {
        File logFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalLogFolder(), ".log");
        File ctlFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalCtlFolder(), ".ctl");
        File badFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalBadFolder(), ".bad");

        prepareCtlFile(ctlFile, badFile.getAbsolutePath());
        sqlLdr(ctlFile, logFile, getLoaderFileRecord().getLoaderEnvironment().getSqlLdrErrorLimit());
        checkLogFile(logFile);
        if (badFile.exists()) {
            checkBadFile(badFile);
        }
    }

    @Override
    public void insertResult(OffsetDateTime fragmentDate,
                             Long totalRowCount,
                             Long loadedRowCount,
                             Long failedRowCount) {
        long failedCount = badFileContentDateMap
                .getOrDefault(
                        fragmentDate.format(
                                DateTimeFormatter.ofPattern(
                                        dbDateFormatToJavaDateFormat(
                                                getLoaderFileRecord().getFragmentDateFormat()
                                        )
                                )
                        ),
                        0L);
        super.insertResult(fragmentDate, totalRowCount, totalRowCount - failedCount, failedCount);
    }

    @Override
    public void afterLoader() {
        super.afterLoader();
        badFileContentDateMap.clear();
    }

    @Override
    public void afterLoaderFailure() throws IOException {
        super.afterLoaderFailure();
        if (getLoadedCount() == 0) {
            Files.deleteIfExists(
                    prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalBadFolder(), ".bad")
                            .toPath()
            );
        }
    }

    private void prepareCtlFile(File ctlFile, String badFilePath) {
        try (FileOutputStream output = new FileOutputStream(ctlFile)) {
            output.write(sqlLdrCtlFilePattern(badFilePath).getBytes());
        } catch (Exception exception) {
            log.error("! OracleLoader prepareCtlFile fileName: {}", ctlFile.getName(), exception);
        }
    }

    private String sqlLdrCtlFilePattern(String badFileAbsolutePath) {
        return " load data" +
                "\n" +
                " infile '" + getLoaderFileRecord().getFile().getAbsolutePath() + "'" +
                "\n" +
                " badfile '" + badFileAbsolutePath + "'" +
                "\n" +
                " append into table " +
                getLoaderFileRecord().getSchemaName() +
                "." +
                getLoaderFileRecord().getTableName() +
                "\n" +
                " fields terminated by '" +
                getLoaderFileRecord().getFileDelimiter() +
                "'" +
                "\n" +
                " trailing nullcols" +
                "\n" +
                " (" +
                "\n" +
                sqlLdrColumnPattern() +
                "\n" +
                " )";
    }

    private String sqlLdrColumnPattern() {
        StringBuilder builder = new StringBuilder();
        getLoaderFileRecord().getColumnRecord().stream()
                .sorted(Comparator.comparingInt(SqlLdrColumnPatternRecord::getColumnOrderId))
                .forEach(column -> {
                    builder.append(" ").append(column.getColumnName());
                    switch (column.getColumnType().toUpperCase()) {
                        case "VARCHAR2":
                        case "CLOB":
                            builder.append(" char")
                                    .append("(")
                                    .append((column.getColumnLength() == null ? 255 : column.getColumnLength()))
                                    .append(")");
                            break;
                        case "DATE":
                            builder.append(" date")
                                    .append(" \"")
                                    .append(column.getColumnFormula().toLowerCase())
                                    .append("\"");
                            break;
                        case "TIMESTAMPTZ":
                            builder.append(" timestamp with time zone ")
                                    .append(" \"")
                                    .append(column.getColumnFormula().toLowerCase().replace("z", " tzh:tzm"))
                                    .append("\"");
                            break;
                    }
                    if (column.getIsColumnAgg() != null && column.getIsColumnAgg()
                            && column.getColumnAggFormula() != null) {
                        builder.append(" \"").append(column.getColumnAggFormula()).append("\"");
                    }
                    builder.append(",").append("\n");
                });
        return builder.delete(builder.length() - 2, builder.length()).toString();
    }

    private void sqlLdr(File ctlFile, File logFile, long errors) throws Exception {
        try {
            String sqlLdr
                    = getLoaderFileRecord().getLoaderEnvironment().getSqlLdrPath() +
                    " userid=" +
                    "'" +
                    getLoaderFileRecord().getLoaderEnvironment().getUserName() +
                    "/" +
                    getLoaderFileRecord().getLoaderEnvironment().getUserPass() +
                    "@" +
                    getLoaderFileRecord().getLoaderEnvironment().getUrl().split("@")[1].split("\\?")[0] +
                    "'" +
                    " control=" +
                    "'" +
                    ctlFile.getAbsolutePath() +
                    "'" +
                    " log=" +
                    "'" +
                    logFile.getAbsolutePath() +
                    "'" +
                    " direct=true" +
                    " rows=" +
                    1000000 +
                    " errors=" +
                    errors;

            Process proc = new ProcessBuilder(sqlLdr.split(" ")).start();
            proc.waitFor();
            proc.destroy();
        } catch (Exception exception) {
            Thread.currentThread().interrupt();
            log.error("! OracleLoader sqlLdr fileName: {}", getLoaderFileRecord().getFile().getName(), exception);
            throw exception;
        }
    }

    private void checkLogFile(File logFile) {
        long loadedCount = 0;
        long errorCount = 0;
        long rowCount
                = getLoaderFileRecord().getContentDates().stream().mapToLong(ContentDateResultRecord::getRowCount)
                .sum();
        try (FileInputStream fileInputStream = new FileInputStream(logFile);
             DataInputStream dataInputStream = new DataInputStream(fileInputStream);
             InputStreamReader inputStreamReader = new InputStreamReader(dataInputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String strLine;
            while ((strLine = bufferedReader.readLine()) != null) {
                if (strLine.contains("successfully loaded")) {
                    loadedCount = Long.parseLong(strLine.split("Row")[0].trim());
                }
                if (strLine.contains("başarıyla yüklendi")) {
                    loadedCount = Long.parseLong(strLine.split("Satır")[0].trim());
                }

                if (strLine.contains("not loaded due to data errors")) {
                    errorCount += Long.parseLong(strLine.split("Row")[0].trim());
                }
                if (strLine.contains("Satırlar yüklenmedi")) {
                    try {
                        errorCount += Long.parseLong(strLine.split("Satır")[0].split("hataları nedeniyle ")[1].trim());
                    } catch (Exception ex) {
                        errorCount += Long.parseLong(strLine.split("Satır")[0].split("için ")[1].trim());
                    }
                }
            }
        } catch (Exception e) {
            errorCount = (errorCount == 0 ? rowCount : errorCount);
        }
        setLoadedCount(loadedCount);
        setErrorCount((loadedCount < 0 ? rowCount : errorCount));
    }

    private void checkBadFile(File badFile) throws Exception {
        StringBuilder delimiter = new StringBuilder().append("\\").append(getLoaderFileRecord().getFileDelimiter());
        try (FileReader fileReader = new FileReader(badFile);
             BufferedReader bufferedReader = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String dateValue = line.split(delimiter.toString())[getLoaderFileRecord().getFragmentDateIndex() - 1];
                if (!badFileContentDateMap.containsKey(dateValue)) {
                    badFileContentDateMap.put(dateValue, 1L);
                } else {
                    badFileContentDateMap.put(dateValue, badFileContentDateMap.get(dateValue) + 1L);
                }
            }
        } catch (Exception exception) {
            log.error("! OracleLoader readBadFile fileName: {}", badFile.getName(), exception);
            throw exception;
        }
    }

    private String dbDateFormatToJavaDateFormat(String dateFormat) {
        return dateFormat.toUpperCase()
                .replace("YYYY", "yyyy")
                .replace("DD", "dd")
                .replace("HH12", "hh")
                .replace("HH24", "HH")
                .replace("MI", "mm")
                .replace("SS", "ss");
    }
}