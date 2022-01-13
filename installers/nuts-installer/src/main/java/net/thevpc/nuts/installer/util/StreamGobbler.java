package net.thevpc.nuts.installer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamGobbler extends Thread {
    InputStream is;
    Consumer<String> cc;

    public StreamGobbler(InputStream is, Consumer<String> cc) {
        this.is = is;
        this.cc = cc;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            int r;
            char[] bb = new char[4096];
            while ((r = br.read(bb)) > 0) {
                cc.accept(new String(bb, 0, r));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
