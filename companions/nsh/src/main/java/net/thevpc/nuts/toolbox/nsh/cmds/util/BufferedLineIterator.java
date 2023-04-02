package net.thevpc.nuts.toolbox.nsh.cmds.util;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

public class BufferedLineIterator extends NNumberedIterator<String> {

    public BufferedLineIterator(InputStream r, Long from0, Long to0) {
        super(new BufferedReaderIterator(new InputStreamReader(r)), from0, to0);
    }

    public BufferedLineIterator(Reader r, Long from0, Long to0) {
        super(new BufferedReaderIterator(r), from0, to0);
    }

    private static class BufferedReaderIterator implements Iterator<String> {
        private final Reader r;
        private String next;
        private BufferedReader reader;

        public BufferedReaderIterator(Reader r) {
            this.r = r;
            next = null;
            reader = (r instanceof BufferedReader) ? (BufferedReader) r : new BufferedReader(r);
        }

        @Override
        public boolean hasNext() {
            try {
                next = reader.readLine();
            } catch (IOException e) {
                next = null;
            }
            return next != null;
        }

        @Override
        public String next() {
            return next;
        }
    }
}
