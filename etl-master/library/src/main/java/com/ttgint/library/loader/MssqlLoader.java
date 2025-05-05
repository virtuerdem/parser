package com.ttgint.library.loader;

import com.ttgint.library.record.LoaderFileRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.IOException;

@Slf4j
public class MssqlLoader extends Loader { // todo

    public MssqlLoader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        super(applicationContext, loaderFileRecord);
    }

    @Override
    public void beforeLoader() {
    }

    @Override
    public void loader() {
    }

    @Override
    public void afterLoader() {
    }

    @Override
    public void afterLoaderSuccess() throws IOException {
    }

    @Override
    public void afterLoaderFailure() throws IOException {
    }

}
