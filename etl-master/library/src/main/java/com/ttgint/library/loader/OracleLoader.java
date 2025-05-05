package com.ttgint.library.loader;

import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.record.LoaderFileRecord;
import com.ttgint.library.record.SqlLdrColumnPatternRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.nio.file.Files;
import java.util.Comparator;

@Slf4j
public class OracleLoader extends Loader {

    public OracleLoader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        super(applicationContext, loaderFileRecord);
    }

    @Override
    public void loader() {
        File logFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalLogFolder(), ".log");
        File ctlFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalCtlFolder(), ".ctl");
        File badFile
                = prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalBadFolder(), ".bad");

        long rowCount
                = getLoaderFileRecord().getContentDates().stream().mapToLong(ContentDateResultRecord::getRowCount).sum();

        prepareCtlFile(ctlFile, badFile.getAbsolutePath());
        sqlLdr(ctlFile, logFile, 1000000, (rowCount == 0 ? 1000 : rowCount));
        checkLogFile(logFile);
    }

    @Override
    public void afterLoaderFailure() throws IOException {
        super.afterLoaderFailure();
        if (getLoadedCount() == 0) {
            Files.delete(
                    prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalBadFolder(), ".bad")
                            .toPath()
            );
        }
    }

    public void prepareCtlFile(File ctlFile, String badFilePath) {
        try (FileOutputStream output = new FileOutputStream(ctlFile)) {
            output.write(sqlLdrCtlFilePattern(badFilePath).getBytes());
        } catch (Exception exception) {
            log.error(exception.getMessage(), exception);
        }
    }

    public String sqlLdrCtlFilePattern(String badFileAbsolutePath) {
        String pattern
                = " load data" +
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
        return pattern;
    }

    public String sqlLdrColumnPattern() {
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

    public void sqlLdr(File ctlFile, File logFile, long nRows, long errors) {
        try {
            String sqlLdr
                    = getLoaderFileRecord().getLoaderEnvironment().getSqlLdrPath() +
                    " userid=" +
                    "'" +
                    getLoaderFileRecord().getLoaderEnvironment().getUserName() +
                    "/" +
                    getLoaderFileRecord().getLoaderEnvironment().getUserPass() +
                    "@" +
                    getLoaderFileRecord().getLoaderEnvironment().getUrl().split("\\@")[1].split("\\?")[0] +
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
                    nRows +
                    " errors=" +
                    errors;

            Runtime rt = Runtime.getRuntime();
            Process proc = rt.exec(sqlLdr);

            proc.waitFor();
            proc.destroy();
        } catch (Exception exception) {
            log.error("! OracleLoader {} for {}", exception.getMessage(), getLoaderFileRecord().getFile().getName());
        }
    }

    public void checkLogFile(File logFile) {
        long loadedCount = -1;
        long errorCount
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
        }
        setLoadedCount(loadedCount);
        setErrorCount(errorCount);
    }

}