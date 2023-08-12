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
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NTextFormat;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NCollections;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.FileSystems;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NComponentScope(NScopeType.WORKSPACE)
public class DfCommand extends NShellBuiltinDefault {

    public DfCommand() {
        super("df", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean onCmdNextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        NSession session = context.getSession();
        Options options = context.getOptions();
        switch (cmdLine.peek().get(session).key()) {
            case "-a":
            case "--all": {
                cmdLine.withNextFlag((v, a, s) -> options.all = v);
                return true;
            }
            case "-h":
            case "--human-readable": {
                cmdLine.withNextFlag((v, a, s) -> options.humanReadable = v);
                return true;
            }
        }
        return false;
    }

    public static class CInfo {
        long totalSpace = -1;
        long usableSpace = -1;
        long available = -1;
        long unallocatedSpace = -1;
        long used = -1;
        String name;
        boolean readOnly;
        int usedPercent;
        String desc;
        String type;
        FileStore fs;

        public CInfo(FileStore fs) {
            this.fs = fs;
            this.name = fs.name();
            this.type = fs.type();
            this.desc = fs.toString();
            this.readOnly = fs.isReadOnly();
            try {
                this.usableSpace = fs.getUsableSpace();
            } catch (IOException e) {
                //
            }
            try {
                totalSpace = fs.getTotalSpace();
            } catch (IOException e) {
                //
            }
            try {
                unallocatedSpace = fs.getUnallocatedSpace();
            } catch (IOException e) {
                //
            }
            if (totalSpace >= 0 && unallocatedSpace >= 0) {
                available = totalSpace - unallocatedSpace;
            }
            if (totalSpace >= 0 && unallocatedSpace >= 0) {
                used = totalSpace - unallocatedSpace;
            }
            if (used >= 0 && totalSpace > 0) {
                usedPercent = (int) (used * 100L / totalSpace);
            }
        }
    }

    public static class UInfo {
        String name;
        String desc;
        String type;
        boolean readOnly;
        String totalSpace;
        String usableSpace;
        String unallocatedSpace;
        String available;
        String used;
        String usedPercent;

        public UInfo(CInfo x, NTextFormat<Number> formatter, NSession session, Options options) {
            name = x.name;
            type = x.type;
            desc = x.desc;
            readOnly = x.readOnly;
            if (options.humanReadable) {
                totalSpace = x.totalSpace < 0 ? "" : formatter.toString(x.totalSpace, session);
                usableSpace = x.usableSpace < 0 ? "" : formatter.toString(x.usableSpace, session);
                unallocatedSpace = x.unallocatedSpace < 0 ? "" : formatter.toString(x.unallocatedSpace, session);
                available = x.available < 0 ? "" : formatter.toString(x.available, session);
                used = x.used < 0 ? "" : formatter.toString(x.used, session);
            } else {
                totalSpace = x.totalSpace < 0 ? "" : String.valueOf(x.totalSpace);
                usableSpace = x.usableSpace < 0 ? "" : String.valueOf(x.usableSpace);
                unallocatedSpace = x.unallocatedSpace < 0 ? "" : String.valueOf(x.unallocatedSpace);
                used = x.used < 0 ? "" : String.valueOf(x.used);
            }
            usedPercent = x.usedPercent < 0 ? "" : (String.valueOf(x.usedPercent) + "%");
        }
    }

    @Override
    protected void onCmdExec(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        options.xfiles = ShellHelper.xfilesOf(options.files, context.getDirectory(), session);
        List<UInfo> result = new ArrayList<>();
        NTextFormat<Number> formatter = NTexts.of(session).createNumberTextFormat("bytes", "").get();
        List<FileStore> stores = NCollections.list(FileSystems.getDefault().getFileStores());
        if (options.xfiles.isEmpty()) {
            Stream<CInfo> s = stores.stream()
                    .map(x -> new CInfo(x));
            if (!options.all) {
                s = s.filter(NCollections.distinctByKey(x -> x.name));
                s = s.filter(x -> x.totalSpace > 0);
            }
            result.addAll(
                    s
                            .map(x -> new UInfo(x, formatter, session, options))
                            .sorted(Comparator.<UInfo, String>comparing(x -> x.name).thenComparing(x -> x.desc))
                            .collect(Collectors.toList())
            );
        }
        context.out().println(result);
    }

    public static class Options {

        boolean all;
        boolean humanReadable;
        List<String> files = new ArrayList<>();
        List<NPath> xfiles = new ArrayList<>();

        boolean p;
    }

    @Override
    protected boolean onCmdNextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return onCmdNextOption(arg, cmdLine, context);
    }
}
