package net.thevpc.nuts.boot;

import java.util.Scanner;

class PrivateNutsTerm {
    private static Scanner inScanner;

    static void errln(String msg, Object... p) {
        System.err.printf(msg, p);
        System.err.printf("%n");
        System.err.flush();
    }

    static void err(String msg, Object... p) {
        System.err.printf(msg, p);
        System.err.flush();
    }

    static void outln(String msg, Object... p) {
        System.out.printf(msg, p);
        System.out.printf("%n");
        System.out.flush();
    }

    static void errln(Throwable exception) {
        exception.printStackTrace(System.err);
    }

    public static String readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }
}
