package com.ttgint.library.loader;

import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.record.LoaderFileRecord;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

@Slf4j
public class PostgresLoader extends Loader {

    public PostgresLoader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        super(applicationContext, loaderFileRecord);
    }

    @Override
    public void loader() {
        Properties properties = new Properties();
        properties.setProperty("user", getLoaderFileRecord().getLoaderEnvironment().getUserName());
        properties.setProperty("password", getLoaderFileRecord().getLoaderEnvironment().getUserPass());
        try (Connection conn
                     = DriverManager.getConnection(getLoaderFileRecord().getLoaderEnvironment().getUrl(), properties);
             FileReader fileReader
                     = new FileReader(getLoaderFileRecord().getFile());
             BufferedReader bufferedReader
                     = new BufferedReader(fileReader)) {

            long loadedCount
                    = new CopyManager((BaseConnection) conn)
                    .copyIn("COPY "
                                    + getLoaderFileRecord().getSchemaName()
                                    + "."
                                    + getLoaderFileRecord().getTableName()
                                    + " "
                                    + "FROM STDIN ( "
                                    + "FORMAT csv, "
                                    + "delimiter "
                                    + "'"
                                    + getLoaderFileRecord().getFileDelimiter()
                                    + "' "
                                    + ")",
                            bufferedReader);
            setLoadedCount(loadedCount);
            setErrorCount(0L);
        } catch (Exception exception) {
            setLoadedCount(-1L);
            setErrorCount(getLoaderFileRecord().getContentDates().stream()
                    .mapToLong(ContentDateResultRecord::getRowCount).sum());
            log.error("! PostgresLoader exception for {} {}", getLoaderFileRecord().getFile().getName(), exception.getMessage());
        }
        properties.clear();
    }

}
