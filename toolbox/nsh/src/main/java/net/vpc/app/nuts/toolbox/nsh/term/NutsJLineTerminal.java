/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.term;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.NutsConsoleContext;
import net.vpc.common.commandline.ArgumentCandidate;
import net.vpc.common.io.FileUtils;
import net.vpc.common.javashell.AutoCompleteCandidate;
import net.vpc.common.javashell.InterruptShellException;
import net.vpc.common.javashell.ShellHistory;
import net.vpc.common.javashell.cmds.Command;
import net.vpc.common.strings.StringUtils;
import org.jline.reader.*;
import org.jline.reader.impl.LineReaderImpl;
import org.jline.reader.impl.history.DefaultHistory;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.Log;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.jline.reader.LineReader.HISTORY_IGNORE;
import static org.jline.reader.impl.ReaderUtils.*;

/**
 * Created by vpc on 2/20/17.
 */
public class NutsJLineTerminal implements NutsSystemTerminalBase {

    private Terminal terminal;
    private LineReader reader;
    private PrintStream out;
    private PrintStream err;
    private InputStream in;
    private NutsWorkspace workspace;
    private NutsTerminalMode outMode;
    private NutsTerminalMode errMode;

    public NutsJLineTerminal() {
    }

    @Override
    public void setOutMode(NutsTerminalMode mode) {
        this.outMode=mode;
    }

    @Override
    public NutsTerminalMode getOutMode() {
        return outMode;
    }

    @Override
    public void setErrorMode(NutsTerminalMode mode) {
        this.errMode=mode;
    }

    @Override
    public NutsTerminalMode getErrorMode() {
        return errMode;
    }

    public void install(NutsWorkspace workspace) {
        this.workspace = workspace;
        TerminalBuilder builder = TerminalBuilder.builder();
        builder.streams(System.in, System.out);
        builder.system(true);

        try {
            terminal = builder.build();
        } catch (Throwable ex) {
            //unable to create system terminal
        }
        if (terminal == null) {
            builder.system(false);
            try {
                terminal = builder.build();
            } catch (IOException ex) {
                Logger.getLogger(NutsJLineTerminal.class.getName()).log(Level.SEVERE, null, ex);
                throw new NutsIOException(ex);
            }
        }

        reader = LineReaderBuilder.builder()
                .completer(new MyCompleter(workspace))
                .terminal(terminal)
                //                .completer(completer)
                //                .parser(parser)
                .build();
        reader.setVariable(LineReader.HISTORY_FILE, FileUtils.getAbsoluteFile(new File(workspace.getConfigManager().getWorkspaceLocation()), "history"));
        ((LineReaderImpl) reader).setHistory(new MyHistory(reader, workspace));
        this.out = workspace.getIOManager().createPrintStream(
                new TransparentPrintStream(
                        reader.getTerminal().output(),
                        System.out
                )
                , NutsTerminalMode.FORMATTED);
        this.err = workspace.getIOManager().createPrintStream(
                new TransparentPrintStream(
                        reader.getTerminal().output(),
                        System.err
                )
                , NutsTerminalMode.FORMATTED);//.setColor(NutsPrintStream.RED);
        this.in = new TransparentInputStream(reader.getTerminal().input(), System.in);

    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT + 1;
    }

    @Override
    public String readLine(String promptFormat, Object... params) {
        String readLine = null;
        try {
            readLine = reader.readLine(promptFormat);
        } catch (UserInterruptException e) {
            throw new InterruptShellException();
        }
        try {
            reader.getHistory().save();
        } catch (IOException e) {
            throw new NutsIOException(e);
        }
        return readLine;
    }

    @Override
    public String readPassword(String prompt) {
        return reader.readLine(prompt, '*');
    }

    @Override
    public InputStream getIn() {
        return in;
    }

    @Override
    public PrintStream getOut() {
        return out;
    }

    @Override
    public PrintStream getErr() {
        return err;
    }

    private static class TransparentInputStream extends FilterInputStream implements InputStreamTransparentAdapter {
        private InputStream root;

        public TransparentInputStream(InputStream in, InputStream root) {
            super(in);
            this.root = root;
        }

        @Override
        public InputStream baseInputStream() {
            return root;
        }
    }

    private static class TransparentPrintStream extends PrintStream implements OutputStreamTransparentAdapter {
        private OutputStream root;

        public TransparentPrintStream(OutputStream out, OutputStream root) {
            super(out, true);
            this.root = root;
        }

        @Override
        public OutputStream baseOutputStream() {
            return root;
        }

    }

    private static class MyHistory implements History {
        private NutsWorkspace workspace;
        private ShellHistory shellHistory;
        public static final int DEFAULT_HISTORY_SIZE = 500;
        public static final int DEFAULT_HISTORY_FILE_SIZE = 10000;

        private final LinkedList<Entry> items = new LinkedList<>();

        private LineReader reader;

        private int lastLoaded = 0;
        private int nbEntriesInFile = 0;
        private int offset = 0;
        private int index = 0;

        public MyHistory(LineReader reader, NutsWorkspace workspace) {
            attach(reader);
            this.workspace = workspace;
            workspace.addUserPropertyListener(new MapListener<String, Object>() {

                @Override
                public void entryAdded(String name, Object value) {
                    if (ShellHistory.class.getName().equals(name)) {
                        setShellHistory((ShellHistory) value);
                    }
                }

                @Override
                public void entryRemoved(String name, Object value) {
                    if (ShellHistory.class.getName().equals(name)) {
                        setShellHistory(null);
                    }
                }

                @Override
                public void entryUpdated(String key, Object newValue, Object oldValue) {
                    if (ShellHistory.class.getName().equals(newValue)) {
                        setShellHistory((ShellHistory) newValue);
                    }
                }
            });
            setShellHistory((ShellHistory) workspace.getUserProperties().get(ShellHistory.class.getName()));
        }

        private void setShellHistory(ShellHistory shellHistory) {
            this.shellHistory = shellHistory;
            offset = 0;
            index = 0;
            items.clear();
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
        public void attach(LineReader reader) {
            if (this.reader != reader) {
                this.reader = reader;
                try {
                    load();
                } catch (IOException e) {
                    Log.warn("Failed to load history", e);
                }
            }
        }

        @Override
        public void load() throws IOException {
            if (shellHistory != null) {
                shellHistory.load();
                maybeResize();
            } else {
                Path path = getPath();
                if (path != null) {
                    try {
                        if (Files.exists(path)) {
                            Log.trace("Loading history from: ", path);
                            try (BufferedReader reader = Files.newBufferedReader(path)) {
                                internalClear();
                                reader.lines().forEach(l -> {
                                    int idx = l.indexOf(':');
                                    if (idx < 0) {
                                        throw new NutsExecutionException("Bad history file syntax! " +
                                                "The history file `" + path + "` may be an older history: " +
                                                "please remove it or use a different history file.", 2);
                                    }
                                    Instant time = Instant.ofEpochMilli(Long.parseLong(l.substring(0, idx)));
                                    String line = unescape(l.substring(idx + 1));
                                    internalAdd(time, line);
                                });
                                lastLoaded = items.size();
                                nbEntriesInFile = lastLoaded;
                                maybeResize();
                            }
                        }
                    } catch (IOException e) {
                        Log.debug("Failed to load history; clearing", e);
                        internalClear();
                        throw e;
                    }
                }
            }
        }

        @Override
        public void purge() throws IOException {
            if (shellHistory != null) {
                shellHistory.clear();
            } else {
                internalClear();
                Path path = getPath();
                if (path != null) {
                    Log.trace("Purging history from: ", path);
                    Files.deleteIfExists(path);
                }
            }
        }

        @Override
        public void save() throws IOException {
            if (shellHistory != null) {
                shellHistory.save();
            } else {
                Path path = getPath();
                if (path != null) {
                    Log.trace("Saving history to: ", path);
                    Files.createDirectories(path.toAbsolutePath().getParent());
                    // Append new items to the history file
                    try (BufferedWriter writer = Files.newBufferedWriter(path.toAbsolutePath(),
                            StandardOpenOption.WRITE, StandardOpenOption.APPEND, StandardOpenOption.CREATE)) {
                        for (Entry entry : items.subList(lastLoaded, items.size())) {
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
            }
        }

        protected void trimHistory(Path path, int max) throws IOException {
            Log.trace("Trimming history path: ", path);
            // Load all history entries
            LinkedList<Entry> allItems = new LinkedList<>();
            try (BufferedReader reader = Files.newBufferedReader(path)) {
                reader.lines().forEach(l -> {
                    int idx = l.indexOf(':');
                    Instant time = Instant.ofEpochMilli(Long.parseLong(l.substring(0, idx)));
                    String line = unescape(l.substring(idx + 1));
                    allItems.add(new EntryImpl(allItems.size(), time, line));
                });
            }
            // Remove duplicates
            doTrimHistory(allItems, max);
            // Write history
            Path temp = Files.createTempFile(path.toAbsolutePath().getParent(), path.getFileName().toString(), ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(temp, StandardOpenOption.WRITE)) {
                for (Entry entry : allItems) {
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

        private void internalClear() {
            offset = 0;
            index = 0;
            lastLoaded = 0;
            nbEntriesInFile = 0;
            items.clear();
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

        public int size() {
            if (shellHistory != null) {
                return shellHistory.size();
            } else {
                return items.size();
            }
        }

        public boolean isEmpty() {
            if (shellHistory != null) {
                return shellHistory.isEmpty();
            } else {
                return items.isEmpty();
            }
        }

        public int index() {
            return offset + index;
        }

        public int first() {
            return offset;
        }

        public int last() {
            return offset + size() - 1;
        }

        private String format(Entry entry) {
            return Long.toString(entry.time().toEpochMilli()) + ":" + escape(entry.line()) + "\n";
        }

        public String getLast() {
            if (shellHistory != null) {
                return shellHistory.getLast();
            } else {
                return items.getLast().line();
            }
        }

        public String get(final int index) {
            if (shellHistory != null) {
                return shellHistory.get(index - offset);
            } else {
                return items.get(index - offset).line();
            }
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
                if (!isEmpty() && line.equals(getLast())) {
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
                } catch (IOException e) {
                    Log.warn("Failed to save history", e);
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
            if (shellHistory != null) {
                shellHistory.add(line);
                maybeResize();
            } else {
                Entry entry = new EntryImpl(offset + items.size(), time, line);
                items.add(entry);
                maybeResize();
            }
        }

        private void maybeResize() {
            if (shellHistory != null) {
                while (size() > getInt(reader, LineReader.HISTORY_SIZE, DEFAULT_HISTORY_SIZE)) {
                    shellHistory.remove(0);
                    //lastLoaded--;
                    //offset++;
                }
                index = size();
            } else {
                while (size() > getInt(reader, LineReader.HISTORY_SIZE, DEFAULT_HISTORY_SIZE)) {
                    items.removeFirst();
                    lastLoaded--;
                    offset++;
                }
                index = size();
            }
        }

        public ListIterator<Entry> iterator(int index) {
            if (shellHistory != null) {
                List<Entry> r = new ArrayList<>();
                List<String> elements = shellHistory.getElements();
                for (int i = 0; i < elements.size(); i++) {
                    r.add(new EntryImpl(
                            i,
                            Instant.now(),
                            elements.get(i)
                    ));
                }
                return r.listIterator(index);
            } else {
                return items.listIterator(index - offset);
            }
        }

        static class EntryImpl implements Entry {

            private final int index;
            private final Instant time;
            private final String line;

            public EntryImpl(int index, Instant time, String line) {
                this.index = index;
                this.time = time;
                this.line = line;
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
            public String toString() {
                return String.format("%d: %s", index, line);
            }
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
            index -= offset;
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

    }

    private static class MyCompleter implements Completer {
        private final NutsWorkspace workspace;

        public MyCompleter(NutsWorkspace workspace) {
            this.workspace = workspace;
        }

        @Override
        public void complete(LineReader reader, final ParsedLine line, List<Candidate> candidates) {
            NutsConsoleContext nutsConsoleContext = (NutsConsoleContext) workspace.getUserProperties().get(NutsConsoleContext.class.getName());
            if (nutsConsoleContext != null) {
                if (line.wordIndex() == 0) {
                    for (Command command : nutsConsoleContext.getShell().getCommands()) {
                        candidates.add(new Candidate(command.getName()));
                    }
                } else {
                    String commandName = line.words().get(0);
                    int wordIndex = line.wordIndex() - 1;
                    List<String> autoCompleteWords = new ArrayList<>(line.words().subList(1, line.words().size()));
                    int x = commandName.length();
                    String autoCompleteLine = line.line().substring(x);
                    List<AutoCompleteCandidate> autoCompleteCandidates =
                            nutsConsoleContext.resolveAutoCompleteCandidates(commandName, autoCompleteWords, wordIndex, autoCompleteLine);
                    for (Object cmdCandidate0 : autoCompleteCandidates) {
                        ArgumentCandidate cmdCandidate = (ArgumentCandidate) cmdCandidate0;
                        if (cmdCandidate != null) {
                            String value = cmdCandidate.getValue();
                            if (!StringUtils.isEmpty(value)) {
                                String display = cmdCandidate.getDisplay();
                                if (StringUtils.isEmpty(display)) {
                                    display = value;
                                }
                                candidates.add(new Candidate(
                                        value,
                                        display,
                                        null, null, null, null, true
                                ));
                            }
                        }
                    }
                }
            }
        }
    }
}
