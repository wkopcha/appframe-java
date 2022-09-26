package com.wyattk.appframe.util;

import java.time.Instant;
import java.util.HashMap;

public class Logger {

    private static boolean enabled = false;
    private static boolean enableColor = false;
    private static boolean enableVerbose = false;
    private static HashMap<String, String> colors;

    private static final String LOG = " LOG", WARN = "WARN", ERROR = " ERR", VERBOSE = "VERB";

    public static void enable() {
        enabled = true;
    }

    public static void withColor() {
        enableColor = true;
        colors = new HashMap<>();
        colors.put(LOG, "");
        colors.put(VERBOSE, "\u001B[36m");
        colors.put(WARN, "\u001B[33m");
        colors.put(ERROR, "\u001B[31m");
    }

    public static void verbose() {
        enableVerbose = true;
    }

    private static void log(String type, String message, boolean err) {
        if(!enabled)
            return;

        if(enableColor)
            System.out.print(colors.get(type));

        Instant now = Instant.now();
        String dateString = now.toString().split("\\.")[0].replaceAll("[0-9]T[0-9]", " ");

        if(!err)
            System.out.print("(" + dateString + ")[" + type + "]: " + message);
        else
            System.err.print("(" + dateString + ")[" + type + "]: " + message);

        if(enableColor)
            System.out.print("\u001B[0m");

        System.out.println();
    }

    public static void log(String message) {
        log(LOG, message, false);
    }

    public static void verb(String message) {
        if(enableVerbose)
            log(VERBOSE, message, false);
    }

    public static void warn(String message) {
        log(WARN, message, false);
    }

    public static void err(String message) {
        log(ERROR, message, true);
    }
}
