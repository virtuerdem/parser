package com.ttgint.library.loader;

import com.ttgint.library.record.LoaderEnvironmentRecord;
import com.ttgint.library.record.LoaderFileRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Getter
@Slf4j
public class LoaderFactory {

    private final Loader loader;

    public LoaderFactory(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        loaderFileRecord.setLoaderEnvironment(
                new LoaderEnvironmentRecord().getRecord(applicationContext, loaderFileRecord.getLoaderTarget()));
        if (loaderFileRecord.getLoaderEnvironment() == null) {
            this.loader = null;
        } else {
            switch (loaderFileRecord.getLoaderEnvironment().getDbType()) {
                case "ORACLE":
                    this.loader = new OracleLoader(applicationContext, loaderFileRecord); // Oracle-Sqlldr
                    break;
                case "MSSQL":
                    this.loader = new MssqlLoader(applicationContext, loaderFileRecord); // Mssql
                    break;
                case "POSTGRESQL":
                    if ("V1".equals(loaderFileRecord.getLoaderEnvironment().getLoaderVersion())
                            || !(loaderFileRecord.getLoaderTarget() == null || loaderFileRecord.getLoaderTarget().isEmpty())) {
                        this.loader = new PostgresLoader(applicationContext, loaderFileRecord); // PostgresqlCopy-DriverManager
                    } else {
                        this.loader = new PostgresV2Loader(applicationContext, loaderFileRecord); // PostgresqlCopy-DataSource
                    }
                    break;
                default:
                    log.error("! Unknown loaderType: {}-{}",
                            loaderFileRecord.getLoaderEnvironment().getDbType(),
                            loaderFileRecord.getLoaderEnvironment().getLoaderVersion());
                    this.loader = null;
                    break;
            }
        }
    }
}

