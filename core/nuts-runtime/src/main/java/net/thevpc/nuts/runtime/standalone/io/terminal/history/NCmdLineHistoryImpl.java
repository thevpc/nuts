/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.io.terminal.history;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.cmdline.NCmdLineHistoryEntry;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NAssert;

import java.io.*;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author thevpc
 */
public class NCmdLineHistoryImpl implements NCmdLineHistory {

    private NPath path;
    private final NSession session;
    private final List<NCmdLineHistoryEntry> entries = new ArrayList<>();

    public NCmdLineHistoryImpl(NSession session) {
        this.session = session;
    }

    @Override
    public void load() {
        entries.clear();
        NAssert.requireNonNull(path, "path");
        if (path.exists()) {
            try (InputStream in = path.getInputStream()) {
                load(in);
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
    }

    @Override
    public void save() {
        NAssert.requireNonNull(path, "path");
        path.mkParentDirs();
        try (OutputStream out = path.getOutputStream()) {
            save(out);
        } catch (IOException ex) {
            throw new NIOException(ex);
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
                            entries.add(new NCmdLineHistoryEntryImpl(index, line, instant));
                        }
                    }
                }
            } catch (IOException ex) {
                throw new NIOException(ex);
            }
        }
    }

    @Override
    public void save(OutputStream outs) {
        try (PrintStream out = new PrintStream(outs)) {
            for (NCmdLineHistoryEntry entry : entries) {
                out.println("#at:" + entry.getTime().toString());
                out.println(entry.getLine().replace("\n", "\\n").replace("\r", "\\r"));
            }
        }
    }

    @Override
    public NCmdLineHistory setPath(Path path) {
        this.path = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NCmdLineHistory setPath(File path) {
        this.path = path == null ? null : NPath.of(path);
        return this;
    }

    @Override
    public NCmdLineHistory setPath(NPath path) {
        this.path = path;
        return this;
    }

    @Override
    public NPath getPath() {
        return path;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public void purge() {
        entries.clear();
        if (path.exists()) {
            path.delete();
        }
    }

    @Override
    public NCmdLineHistoryEntry getEntry(int index) {
        return entries.get(index);
    }

    @Override
    public ListIterator<NCmdLineHistoryEntry> iterator(int index) {
        return entries.listIterator(index);
    }

    @Override
    public void add(Instant time, String line) {
        entries.add(new NCmdLineHistoryEntryImpl(entries.size(), line, time));
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }
}
