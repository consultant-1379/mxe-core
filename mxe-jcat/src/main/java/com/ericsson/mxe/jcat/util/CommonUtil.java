package com.ericsson.mxe.jcat.util;

import java.io.File;

public class CommonUtil {

    private CommonUtil() {}

    public static String getNameFromPath(String path) {
        return new File(path).getName();
    }

}
