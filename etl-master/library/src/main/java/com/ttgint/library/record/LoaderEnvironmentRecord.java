package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

@Slf4j
@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class LoaderEnvironmentRecord {

    private String dbType;
    private String loaderVersion;

    private String url;
    private String userName;
    private String userPass;
    private String sqlLdrPath;
    private String localRootPath;
    private String localRawFolder;
    private String localLogFolder;
    private String localCtlFolder;
    private String localBadFolder;
    private String localFailedFolder;

    public LoaderEnvironmentRecord getRecord(ApplicationContext applicationContext, String loaderTarget) {
        Environment environment = (Environment) applicationContext.getBean(Environment.class);
        LoaderEnvironmentRecord record = new LoaderEnvironmentRecord();
        if (loaderTarget == null || loaderTarget.isEmpty()) {
            record.setUrl(environment.getProperty("spring.datasource.url"));
            record.setUserName(environment.getProperty("spring.datasource.username"));
            record.setUserPass(environment.getProperty("spring.datasource.password"));
        } else {
            record.setUrl(environment.getProperty(loaderTarget + ".url"));
            record.setUserName(environment.getProperty(loaderTarget + ".username"));
            record.setUserPass(environment.getProperty(loaderTarget + ".password"));
            if (record.getUrl() == null || record.getUserName() == null || record.getUserPass() == null) {
                log.error("! Missing environment for {}", loaderTarget);
                return null;
            }
        }
        record.setDbType(environment.getProperty("app.all.dbType"));
        record.setLoaderVersion(environment.getProperty("app.all.loaderVersion"));

        record.setSqlLdrPath(environment.getProperty("app.all.sqlLdrPath"));
        record.setLocalRootPath(environment.getProperty("app.engine.rootPath"));
        record.setLocalRawFolder(environment.getProperty("app.engine.rawFolder"));
        record.setLocalLogFolder(environment.getProperty("app.engine.logFolder"));
        record.setLocalCtlFolder(environment.getProperty("app.engine.ctlFolder"));
        record.setLocalBadFolder(environment.getProperty("app.engine.badFolder"));
        record.setLocalFailedFolder(environment.getProperty("app.engine.failedFolder"));
        return record;
    }

}
