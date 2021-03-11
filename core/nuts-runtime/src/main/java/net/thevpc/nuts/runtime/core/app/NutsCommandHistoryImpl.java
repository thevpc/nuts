/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.thevpc.nuts.NutsCommandHistory;
import net.thevpc.nuts.NutsCommandHistoryEntry;
import net.thevpc.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsCommandHistoryImpl implements NutsCommandHistory {

    private NutsWorkspace ws;
    private List<NutsCommandHistoryEntry> entries = new ArrayList<>();
    private Path path;

    public NutsCommandHistoryImpl(NutsWorkspace ws, Path path) {
        this.ws = ws;
        this.path = path;
        if (path == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void load() {
        entries.clear();
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                load(in);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public void load(InputStream in) {
        entries.clear();
        if (in != null) {
            try (BufferedReader out = new BufferedReader(new InputStreamReader(in))) {
                String line = null;
                Instant instant = null;
                int index = 0;
                while ((line = out.readLine()) != null) {
                    if (line.length() > 0) {
                        if (line.startsWith("#")) {
                            if (line.startsWith("#at:")) {
                                instant = Instant.parse(line.substring("#at:".length()).trim());
                            }
                        } else {
                            entries.add(new NutsCommandHistoryEntryImpl(index, line, instant));
                        }
                    }
                }
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public void save(OutputStream outs) {
        try (PrintStream out = new PrintStream(outs)) {
            for (NutsCommandHistoryEntry entry : entries) {
                out.println("#at:" + entry.getTime().toString());
                out.println(entry.getLine().replace("\n", "\\n").replace("\r", "\\r"));
            }
        }
    }

    @Override
    public void save() {
        Path p = path.getParent();
        if (p != null) {
            if (!Files.exists(p)) {
                try {
                    Files.createDirectories(p);
                } catch (IOException ex) {
                    throw new UncheckedIOException(ex);
                }
            }
        }
        try (OutputStream out = Files.newOutputStream(path)) {
            save(out);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public void purge() {
        entries.clear();
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    @Override
    public NutsCommandHistoryEntry getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public ListIterator<NutsCommandHistoryEntry> iterator(int index) {
        return entries.listIterator(index);
    }

    @Override
    public void add(Instant time, String line) {
        entries.add(new NutsCommandHistoryEntryImpl(entries.size(), line, time));
    }

}
