package com.service.app.network.utils;



import androidx.annotation.NonNull;

import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;

abstract public class RepositoryCallback implements Callback {

    protected static final Logger logger = LoggerFactory.getLogger();

    protected final String LOG_TAG;

    protected String requestName;

    public RepositoryCallback(
            String logTag,
            String requestName) {
        LOG_TAG = logTag;
        this.requestName = requestName;
    }

    @Override
    public void onFailure(@NonNull Call call, IOException e) {
        logger.e(LOG_TAG, requestName + ", onFailure: " + e.getMessage());
    }

}
