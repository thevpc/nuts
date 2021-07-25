/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.ext.term;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Consumer;

import net.thevpc.nuts.*;

import static net.thevpc.nuts.ext.term.NutsJLineHistory.DEFAULT_HISTORY_FILE_SIZE;
import static net.thevpc.nuts.ext.term.NutsJLineHistory.DEFAULT_HISTORY_SIZE;
import org.jline.reader.History;
import org.jline.reader.History.Entry;
import org.jline.reader.LineReader;
import static org.jline.reader.LineReader.HISTORY_IGNORE;
import static org.jline.reader.impl.ReaderUtils.getBoolean;
import static org.jline.reader.impl.ReaderUtils.getInt;
import static org.jline.reader.impl.ReaderUtils.getString;
import static org.jline.reader.impl.ReaderUtils.isSet;
import org.jline.utils.Log;

/**
 *
 * @author vpc
 */
public class NutsJLineCommandHistory implements NutsCommandHistory {

    private LineReader reader;
    private final LinkedList<History.Entry> items = new LinkedList<>();

    private int lastLoaded = 0;
    private int nbEntriesInFile = 0;
    private int offset = 0;
    private NutsSession session;

    public NutsJLineCommandHistory(NutsSession session) {
        this.session = session;
    }

    private void internalClear() {
        offset = 0;
        lastLoaded = 0;
        nbEntriesInFile = 0;
        items.clear();
    }

    public LineReader getReader() {
        return reader;
    }

    public void setReader(LineReader reader) {
        this.reader = reader;
    }

    private Path getPath() {
        Object obj = reader != null ? reader.getVariables().get(LineReader.HISTORY_FILE) : null;
        if (obj instanceof Path) {
            return (Path) obj;
        } else if (obj instanceof File) {
            return ((File) obj).toPath();
        } else if (obj != null) {
            return Paths.get(obj.toString());
        } else {
            return null;
        }
    }

    @Override
    public void load(InputStream in) {
        if (in != null) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
                internalClear();
                reader.lines().forEach(new Consumer<String>() {
                    @Override
                    public void accept(String l) {
                        int idx = l.indexOf(':');
                        if (idx < 0) {
                            throw new NutsExecutionException(session,  NutsMessage.cstyle("Bad history file syntax! "
                                    + "The history file may be an older history: "
                                    + "please remove it or use a different history file."), 2);
                        }
                        Instant time = Instant.ofEpochMilli(Long.parseLong(l.substring(0, idx)));
                        String line = unescape(l.substring(idx + 1));
                        internalAdd(time, line);
                    }
                });
                lastLoaded = items.size();
                nbEntriesInFile = lastLoaded;
                maybeResize();
            } catch (Exception ex) {
                throw new NutsIOException(session,ex);
            }
        }
    }

    @Override
    public void load() {
        Path path = getPath();
        if (path != null) {
            try {
                if (Files.exists(path)) {
                    Log.trace("loading history from: ", path);
                    try (InputStream in = Files.newInputStream(path)) {
                        load(in);
                    }
                }
            } catch (IOException e) {
                Log.debug("failed to load history; clearing", e);
                internalClear();
                throw new UncheckedIOException(e);
            }
        }
    }

    @Override
    public void save(OutputStream out) {
//        if (shellHistory != null) {
//            shellHistory.save();
//        } else {
        try {
            Path path = getPath();
            if (path != null) {
                // Append new items to the history file
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out))) {
                    for (History.Entry entry : items.subList(lastLoaded, items.size())) {
                        writer.append(format(entry));
                    }
                }
                nbEntriesInFile += items.size() - lastLoaded;
                // If we are over 25% max size, trim history file
                int max = getInt(reader, LineReader.HISTORY_FILE_SIZE, DEFAULT_HISTORY_FILE_SIZE);
                if (nbEntriesInFile > max + max / 4) {
                    trimHistory(path, max);
                }
            }
            lastLoaded = items.size();
//        }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void save() {
        try {
            Path path = getPath();
            if (path != null) {
                Log.trace("Saving history to: ", path);
                Files.createDirectories(path.toAbsolutePath().getParent());
                // Append new items to the history file
                try (OutputStream out = Files.newOutputStream(path.toAbsolutePath(),
                        StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                    save(out);
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public int size() {
        return offset + items.size();
    }

    @Override
    public void purge() {
//        if (shellHistory != null) {
//            shellHistory.clear();
//        } else {
        internalClear();
        Path path = getPath();
        if (path != null) {
            try {
                Log.trace("Purging history from: ", path);
                Files.deleteIfExists(path);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
//        }
    }

    protected void trimHistory(Path path, int max) throws IOException {
        Log.trace("Trimming history path: ", path);
        // Load all history entries
        LinkedList<History.Entry> allItems = new LinkedList<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            reader.lines().forEach(l -> {
                int idx = l.indexOf(':');
                Instant time = Instant.ofEpochMilli(Long.parseLong(l.substring(0, idx)));
                String line = unescape(l.substring(idx + 1));
                allItems.add(new NutsJLineCommandHistoryEntry(allItems.size(), time, line));
            });
        }
        // Remove duplicates
        doTrimHistory(allItems, max);
        // Write history
        Path temp = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName().toString(), ".tmp");
        try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardOpenOption.WRITE)) {
            for (History.Entry entry : allItems) {
                writer.append(format(entry));
            }
        }
        Files.move(temp, path, StandardCopyOption.REPLACE_EXISTING);
        // Keep items in memory
        internalClear();
        offset = allItems.get(0).index();
        items.addAll(allItems);
        lastLoaded = items.size();
        nbEntriesInFile = items.size();
        maybeResize();
    }

    @Override
    public void add(Instant time, String line) {
        Objects.requireNonNull(time);
        Objects.requireNonNull(line);

        if (getBoolean(reader, LineReader.DISABLE_HISTORY, false)) {
            return;
        }
        if (isSet(reader, LineReader.Option.HISTORY_IGNORE_SPACE) && line.startsWith(" ")) {
            return;
        }
        if (isSet(reader, LineReader.Option.HISTORY_REDUCE_BLANKS)) {
            line = line.trim();
        }
        if (isSet(reader, LineReader.Option.HISTORY_IGNORE_DUPS)) {
            if (!items.isEmpty() && line.equals(items.get(items.size() - 1).line())) {
                return;
            }
        }
        if (matchPatterns(getString(reader, HISTORY_IGNORE, ""), line)) {
            return;
        }
        internalAdd(time, line);
        if (isSet(reader, LineReader.Option.HISTORY_INCREMENTAL)) {
            try {
                save();
            } catch (Exception e) {
                Log.warn("failed to save history", e);
            }
        }
    }

    protected boolean matchPatterns(String patterns, String line) {
        if (patterns == null || patterns.isEmpty()) {
            return false;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < patterns.length(); i++) {
            char ch = patterns.charAt(i);
            if (ch == '\\') {
                ch = patterns.charAt(++i);
                sb.append(ch);
            } else if (ch == ':') {
                sb.append('|');
            } else if (ch == '*') {
                sb.append('.').append('*');
            }
        }
        return line.matches(sb.toString());
    }

    protected void internalAdd(Instant time, String line) {
        int index = offset + items.size();
        History.Entry entry = new NutsJLineCommandHistoryEntry(index, time, line);
        items.add(entry);
        maybeResize();
    }

    private void maybeResize() {
        while (items.size() > getInt(reader, LineReader.HISTORY_SIZE, DEFAULT_HISTORY_SIZE)) {
            items.removeFirst();
            lastLoaded--;
            offset++;
        }
//        index = size();
    }

    private String format(History.Entry entry) {
        return Long.toString(entry.time().toEpochMilli()) + ":" + escape(entry.line()) + "\n";
    }

    public NutsCommandHistoryEntry getEntry(final int index) {
        return (NutsCommandHistoryEntry) items.get(index);
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\n':
                    sb.append('\\');
                    sb.append('n');
                    break;
                case '\\':
                    sb.append('\\');
                    sb.append('\\');
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    static String unescape(String s) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            switch (ch) {
                case '\\':
                    ch = s.charAt(++i);
                    if (ch == 'n') {
                        sb.append('\n');
                    } else {
                        sb.append(ch);
                    }
                    break;
                default:
                    sb.append(ch);
                    break;
            }
        }
        return sb.toString();
    }

    public ListIterator<NutsCommandHistoryEntry> iterator(int index) {
        return (ListIterator) items.listIterator(index);
    }

    static void doTrimHistory(List<Entry> allItems, int max) {
        int idx = 0;
        while (idx < allItems.size()) {
            int ridx = allItems.size() - idx - 1;
            String line = allItems.get(ridx).line().trim();
            ListIterator<Entry> iterator = allItems.listIterator(ridx);
            while (iterator.hasPrevious()) {
                String l = iterator.previous().line();
                if (line.equals(l.trim())) {
                    iterator.remove();
                }
            }
            idx++;
        }
        while (allItems.size() > max) {
            allItems.remove(0);
        }
    }
}
