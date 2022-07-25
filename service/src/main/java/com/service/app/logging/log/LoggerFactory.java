package com.service.app.logging.log;

public class LoggerFactory {

    private static Logger logger = new DefaultLogger();

    private LoggerFactory() {
    }

    public static void setCustomLogger(Logger logger) {
        LoggerFactory.logger = logger;
    }

    public static Logger getLogger() {
        return logger;
    }

}
