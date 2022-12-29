/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.ext.term;

import java.time.Instant;
import net.thevpc.nuts.cmdline.NCommandHistoryEntry;
import org.jline.reader.History;

/**
 *
 * @author thevpc
 */
class NJLineCommandHistoryEntry implements History.Entry, NCommandHistoryEntry {
    
    final int index;
    final Instant time;
    final String line;

    public NJLineCommandHistoryEntry(int index, Instant time, String line) {
        this.index = index;
        this.time = time;
        this.line = line;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public int index() {
        return index;
    }

    public Instant time() {
        return time;
    }

    public String line() {
        return line;
    }

    @Override
    public Instant getTime() {
        return time();
    }

    @Override
    public String getLine() {
        return line();
    }

    @Override
    public String toString() {
        return String.format("%d: %s", index, line);
    }
    
}
