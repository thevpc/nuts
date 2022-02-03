package net.thevpc.nuts.installer.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Consumer;

public class StreamGobbler extends Thread {
    private InputStream inputStream;
    private Consumer<String> consumer;

    public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
        this.inputStream = inputStream;
        this.consumer = consumer;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            int r;
            char[] bb = new char[4096];
            while ((r = br.read(bb)) > 0) {
                consumer.accept(new String(bb, 0, r));
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
