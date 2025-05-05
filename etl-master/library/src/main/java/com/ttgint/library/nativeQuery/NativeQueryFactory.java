package com.ttgint.library.nativeQuery;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@Getter
@Slf4j
public class NativeQueryFactory {

    private final NativeQuery nativeQuery;

    public NativeQueryFactory(ApplicationContext applicationContext) {
        String dbType = applicationContext.getBean(Environment.class).getProperty("app.all.dbType");
        if ("ORACLE".equalsIgnoreCase(dbType)) {
            nativeQuery = new OracleQuery(applicationContext);
        } else if ("POSTGRESQL".equalsIgnoreCase(dbType)) {
            nativeQuery = new PostgresqlQuery(applicationContext);
        } else { //MSSQL
            nativeQuery = new MssqlQuery(applicationContext);
        }
    }
}
