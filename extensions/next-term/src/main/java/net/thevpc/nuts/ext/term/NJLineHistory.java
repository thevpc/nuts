package net.thevpc.nuts.ext.term;

import org.jline.reader.History;
import org.jline.reader.LineReader;
import org.jline.utils.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.*;
import net.thevpc.nuts.cmdline.NCommandHistory;
import net.thevpc.nuts.cmdline.NCommandHistoryEntry;
import net.thevpc.nuts.NSession;

class NJLineHistory implements History {

    private NSession session;
    public static final int DEFAULT_HISTORY_SIZE = 500;
    public static final int DEFAULT_HISTORY_FILE_SIZE = 10000;

    private NJLineTerminal terminal;
    private NJLineCommandHistory defaultHistory;
    private int index = 0;
    private LineReader reader;

    public NJLineHistory(LineReader reader, NSession session, NJLineTerminal terminal) {
        this.session = session;
        this.terminal = terminal;
        defaultHistory = new NJLineCommandHistory(session);
        attach(reader);
    }

    @Override
    public void add(Instant time, String line) {
        NCommandHistory h = getNutsCommandHistory();
        if (h.size() > 0) {
            NCommandHistoryEntry last = h.getEntry(h.size() - 1);
            if(last!=null && last.getLine().equals(line)){
                //remove duplicates by default!
                return;
            }
        }
        h.add(time, line);
    }

    @Override
    public void attach(LineReader reader) {
        if (this.reader != reader) {
            this.reader = reader;
            defaultHistory.setReader(reader);
            try {
                load();
            } catch (IOException e) {
                Log.warn("failed to load history", e);
            }
        }
    }

    private NCommandHistory getNutsCommandHistory() {
        if (terminal.getCommandHistory() != null) {
            return terminal.getCommandHistory();
        }
        return defaultHistory;
    }

    @Override
    public void load() throws IOException {
        getNutsCommandHistory().load();
    }

    @Override
    public void purge() throws IOException {
//        if (shellHistory != null) {
//            shellHistory.clear();
//        } else {
        getNutsCommandHistory().purge();
//        }
    }

    @Override
    public void save() throws IOException {
        getNutsCommandHistory().save();
    }

    public int size() {
        return getNutsCommandHistory().size();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public int index() {
        return index;
    }

    public int first() {
        return 0;
    }

    public int last() {
        return size() - 1;
    }

    public String getLast() {
        return get(last());
    }

    @Override
    public String get(int index) {
        NCommandHistory h = getNutsCommandHistory();
        if (index < h.size()) {
            return h.getEntry(index).getLine();
        } else {
            if (h.size() > 0) {
                index = h.size() - 1;
                return h.getEntry(index).getLine();
            }
            return "";
        }
    }

    public ListIterator<Entry> iterator(int index) {
        ListIterator<NCommandHistoryEntry> li = getNutsCommandHistory().iterator(index);
        return new ListIterator<Entry>() {
            @Override
            public boolean hasNext() {
                return li.hasNext();
            }

            private Entry mapTo(NCommandHistoryEntry h) {
                if (h == null) {
                    return null;
                }
                if (h instanceof Entry) {
                    return (Entry) h;
                } else {
                    return new NJLineCommandHistoryEntry(h.getIndex(), h.getTime(), h.getLine());
                }
            }

            @Override
            public Entry next() {
                return mapTo(li.next());
            }

            @Override
            public boolean hasPrevious() {
                return li.hasPrevious();
            }

            @Override
            public Entry previous() {
                return mapTo(li.previous());
            }

            @Override
            public int nextIndex() {
                return li.nextIndex();
            }

            @Override
            public int previousIndex() {
                return li.previousIndex();
            }

            @Override
            public void remove() {
                li.remove();
            }

            @Override
            public void set(Entry e) {
                li.set(new NJLineCommandHistoryEntry(e.index(), e.time(), e.line()));
            }

            @Override
            public void add(Entry e) {
                li.add(new NJLineCommandHistoryEntry(e.index(), e.time(), e.line()));
            }
        };
    }

    //
    // Navigation
    //
    /**
     * This moves the history to the last entry. This entry is one position
     * before the moveToEnd() position.
     *
     * @return Returns false if there were no history iterator or the history
     * index was already at the last entry.
     */
    @Override
    public boolean moveToLast() {
        int lastEntry = size() - 1;
        if (lastEntry >= 0 && lastEntry != index) {
            index = size() - 1;
            return true;
        }

        return false;
    }

    /**
     * Move to the specified index in the history
     */
    public boolean moveTo(int index) {
        if (index >= 0 && index < size()) {
            this.index = index;
            return true;
        }
        return false;
    }

    /**
     * Moves the history index to the first entry.
     *
     * @return Return false if there are no iterator in the history or if the
     * history is already at the beginning.
     */
    public boolean moveToFirst() {
        if (size() > 0 && index != 0) {
            index = 0;
            return true;
        }
        return false;
    }

    /**
     * Move to the end of the history buffer. This will be a blank entry, after
     * all of the other iterator.
     */
    public void moveToEnd() {
        index = size();
    }

    /**
     * Return the content of the current buffer.
     */
    public String current() {
        if (index >= size()) {
            index = size();
            return "";
        }
        return get(index);
    }

    /**
     * Move the pointer to the previous element in the buffer.
     *
     * @return true if we successfully went to the previous element
     */
    public boolean previous() {
        if (index <= 0) {
            return false;
        }
        if (index > size()) {
            index = size();
        }
        index--;
        return true;
    }

    /**
     * Move the pointer to the next element in the buffer.
     *
     * @return true if we successfully went to the next element
     */
    public boolean next() {
        if (index >= size()) {
            return false;
        }
        index++;
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Entry e : this) {
            sb.append(e.toString()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void write(Path path, boolean bln) throws IOException {
        try(OutputStream out=Files.newOutputStream(path)){
            defaultHistory.save(out);
        }
    }

    @Override
    public void append(Path path, boolean bln) throws IOException {
        try(OutputStream out=Files.newOutputStream(path,StandardOpenOption.APPEND)){
            defaultHistory.save(out);
        }
    }

    @Override
    public void read(Path path, boolean bln) throws IOException {
        try(InputStream in=Files.newInputStream(path,StandardOpenOption.APPEND)){
            defaultHistory.load(in);
        }
    }

    @Override
    public void resetIndex() {
        index=0;
    }
    
    

}
