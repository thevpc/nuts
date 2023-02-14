package net.thevpc.nuts.toolbox.ncode.log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LogItemIterator implements Iterator<LogItem> {
    private BufferedReader reader;
    private List<Line> buffer = new ArrayList<>();
    private long lineNumber;
    private int maxBufferSize=10;
    private Line next;

    private LogItemIterator(Reader reader) {
        this.reader = new BufferedReader(reader);
    }

    private Line nextLine() {
        String line = null;
        try {
            line = reader.readLine();
        } catch (IOException e) {
            //
        }
        if (line == null) {
            return null;
        }
        lineNumber++;
        Line rLine = new Line(lineNumber, line);
        buffer.add(rLine);
        while(buffer.size()>maxBufferSize){
            buffer.remove(0);
        }
        return rLine;
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public LogItem next() {
        return null;
    }

    private static class Line {
        long index;
        String text;

        public Line(long index, String text) {
            this.index = index;
            this.text = text;
        }
    }
}
