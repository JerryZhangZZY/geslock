package com.example.geslock.tools;

public abstract class MyNameFormatter {

    private static final String LOCKED_FOLDER_MARK = "{]lf[}";

    public static String parseFolderName(String name) {
        if (name.startsWith(LOCKED_FOLDER_MARK)) {
            try {
                name = name.substring(30);
            } catch (Exception ignored) {}
        }
        return name;
    }

    public static boolean isLockedFolder(String name) {
        return name.startsWith(LOCKED_FOLDER_MARK);
    }

    public static String parseCheck(String name) {
        try {
            name = name.substring(6, 30);
        } catch (Exception ignored) {}
        return name;
    }

    /**
     * @param name file name
     * @return prefix = mark + check
     */
    public static String parsePrefix(String name) {
        return name.substring(0, 30);
    }
}
