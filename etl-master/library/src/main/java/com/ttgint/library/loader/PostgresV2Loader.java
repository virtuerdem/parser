package com.ttgint.library.loader;

import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.record.LoaderFileRecord;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.PGConnection;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.springframework.context.ApplicationContext;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;

@Slf4j
public class PostgresV2Loader extends Loader {

    private final DataSource dataSource;

    public PostgresV2Loader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        super(applicationContext, loaderFileRecord);
        this.dataSource = applicationContext.getBean(DataSource.class);
    }

    @Override
    public void loader() {
        try (Connection conn
                     = dataSource.getConnection();
             FileReader fileReader
                     = new FileReader(getLoaderFileRecord().getFile());
             BufferedReader bufferedReader
                     = new BufferedReader(fileReader)) {

            long loadedCount
                    = new CopyManager((BaseConnection) conn.unwrap(PGConnection.class))
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
            log.error("! PostgresV2Loader exception for {} {}", getLoaderFileRecord().getFile().getName(), exception.getMessage());
        }
    }

}
