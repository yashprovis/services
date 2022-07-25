package com.service.app.frp.config.parts;

import com.service.app.logging.log.Logger;
import com.service.app.logging.log.LoggerFactory;

public class ParseUtils {

    private static final Logger logger = LoggerFactory.getLogger();

    public static Integer parseNumber(String value, String logTag, String numberType) {
        if (value == null)
            return null;
        else {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException exc) {
                logger.e(logTag, "Failed to parse " + numberType + ": " + value);
                return null;
            }
        }
    }

    public static Boolean parseBoolean(String value, String logTag, String booleanType) {
        if (value == null)
            return null;
        else {
            try {
                return Boolean.parseBoolean(value);
            } catch (NumberFormatException exc) {
                logger.e(logTag, "Failed to parse " + booleanType + ": " + value);
                return null;
            }
        }
    }

}
