package net.thevpc.nuts.boot;

import java.util.Scanner;

class PrivateNutsTerm {
    private static Scanner inScanner;

    static void errPrintf(String msg, Object... p) {
        System.err.printf(msg, p);
        System.err.flush();
    }

    static void outPrintf(String msg, Object... p) {
        System.out.printf(msg, p);
        System.out.flush();
    }

    static void errPrintStack(Throwable exception) {
        exception.printStackTrace(System.err);
    }

    public static String readLine() {
        if (inScanner == null) {
            inScanner = new Scanner(System.in);
        }
        return inScanner.nextLine();
    }
}
