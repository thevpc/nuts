/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.posix.extra;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.util.*;

import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class HostsCommand extends NShellBuiltinDefault {

    public HostsCommand() {
        super("hosts", NConstants.Support.DEFAULT_SUPPORT, Options.class);
    }

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options config = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get(session).key()) {
            case "add": {
                return nextOptionAdd(cmdLine, context, config);
            }
            case "remove": {
                return nextOptionRemove(cmdLine, context, config);
            }
        }
        return false;
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options config = context.getOptions();
        NSession session = context.getSession();
        switch (cmdLine.peek().get(session).key()) {
            case "--hosts-file": {
                NArg hf = cmdLine.nextEntry().get();
                if (hf.isActive()) {
                    config.hostsFile = hf.value();
                }
                return true;
            }
        }
        return false;
    }

    private boolean isKeyword(String a) {
        switch (a) {
            case "add":
            case "remove":
                return true;
        }
        return false;
    }

    private boolean nextOptionAdd(NCmdLine cmdLine, NShellExecutionContext context, Options config) {
        NSession session = context.getSession();
        if (cmdLine.peek().get(session).key().equals("add")) {
            cmdLine.next();
            while (cmdLine.hasNext()) {
                NArg a = cmdLine.peek().get(session);
                if (!a.isOption() && !isKeyword(a.getImage())) {
                    a = cmdLine.nextEntry().get();
                    String ip = a.key();
                    String name = a.value();
                    NAssert.requireNonBlank(ip, "ip in " + a);
                    NAssert.requireNonBlank(name, "name in " + a);
                    config.toAdd.add(new Host(ip, name));
                } else {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    private boolean nextOptionRemove(NCmdLine cmdLine, NShellExecutionContext context, Options config) {
        NSession session = context.getSession();
        if (cmdLine.peek().get(session).key().equals("remove")) {
            cmdLine.next();
            while (cmdLine.hasNext()) {
                NArg a = cmdLine.peek().get(session);
                if (!a.isOption() && !isKeyword(a.getImage())) {
                    a = cmdLine.next().get();
                    String ip = a.getImage();
                    NAssert.requireNonBlank(ip, "ip in " + a);
                    config.toRemove.add(ip);
                } else {
                    break;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options options = context.getOptions();
        NSession session = context.getSession();
        if (options.toAdd.isEmpty() && options.toRemove.isEmpty()) {
            cmdLine.throwMissingArgument();
        }
        HostLinesService s = new HostLinesService();
        HostLines h = s.readHostLines(options, session);
        boolean someChange = false;
        for (String n : options.toRemove) {
            if (s.removeEntry(h, n, session)) {
                someChange = true;
            }
        }
        for (Host n : options.toAdd) {
            if (s.addEntry(h, n, session)) {
                someChange = true;
            }
        }
        if (someChange) {
            s.writeHostLines(h, options, session);
        }
    }


    private static class HostLinesService {

        private void writeHostLines(HostLines value, Options options, NSession session) {
            String hostsFile = NStringUtils.firstNonBlank(options.hostsFile, "/etc/hosts");
            NPath nPath = NPath.of(hostsFile, session);
            try (PrintStream out = nPath.getPrintStream()) {
                for (HostLine line : value.lines) {
                    if (line instanceof HostLineEntry) {
                        HostLineEntry e = (HostLineEntry) line;
                        out.println(e.buildImage());
                    }else{
                        out.println(line.image);
                    }
                }
            }
        }

        private HostLines readHostLines(Options options, NSession session) {
            String hostsFile = NStringUtils.firstNonBlank(options.hostsFile, "/etc/hosts");
            NPath nPath = NPath.of(hostsFile, session);
            NRef<HostLineComment> lastComment = NRef.ofNull();
            HostLines hosts = new HostLines();
            nPath.getLines().forEach(s -> {
                HostLine t = parseHostLine(s, lastComment.get());
                if (t instanceof HostLineComment) {
                    lastComment.set((HostLineComment) t);
                }
                hosts.lines.add(t);
            });
            return hosts;
        }

        private HostLine parseHostLine(String line, HostLineComment last) {
            if (NBlankable.isBlank(line)) {
                return new HostLineBlank(line);
            }
            if (line.trim().startsWith("#")) {
                String a = line.trim();
                while (a.startsWith("#")) {
                    a = a.substring(1);
                }
                while (a.startsWith(" ") || a.startsWith("\t")) {
                    a = a.substring(1);
                }
                return new HostLineComment(line, a);
            }
            line = line.trim();
            List<String> columns = NStringUtils.split(line, " \t", true, true);
            if (columns.size() > 1) {
                HostLineEntry m = new HostLineEntry();
                m.groupName = last;
                m.image = line;
                m.ip = columns.get(0);
                for (int i = 1; i < columns.size(); i++) {
                    m.names.add(columns.get(i));
                }
                return m;
            }
            return new HostLineUnknown(line);
        }

        private boolean containsName(HostLineEntry hs, String ip, String name, NSession session) {
            if (Objects.equals(ip, hs.ip)) {
                if (hs.names.contains(name)) {
                    return true;
                }
            }
            return false;
        }

        private boolean addEntry(HostLineEntry hs, String name, NSession session) {
            if (hs.names.contains(name)) {
                return false;
            }
            hs.names.add(name);
            hs.changed=true;
            return true;
        }

        private boolean removeEntryByName(HostLineEntry hs, String name, NSession session) {
            if (!hs.names.contains(name)) {
                return false;
            }
            if(hs.names.remove(name)){
                hs.changed=true;
            }
            return true;
        }

        private boolean addEntry(HostLines hs, Host h, NSession session) {
            List<HostLineEntry> u = hs.lines.stream()
                    .filter(x -> x instanceof HostLineEntry)
                    .map(x -> (HostLineEntry) x)
                    .filter(x -> Objects.equals(x.ip, h.ip))
                    .collect(Collectors.toList());
            boolean someChange = false;
            HostLineEntry hs0;
            if (u.size() > 1) {
                hs0 = u.get(0);
                //merge both
                for (int i = 1; i < u.size(); i++) {
                    HostLineEntry o = u.get(i);
                    hs.lines.remove(o);
                    someChange = true;
                    for (String name : o.names) {
                        addEntry(hs0, name, session);
                    }
                }
            } else if (u.size() == 1) {
                hs0 = u.get(0);
            } else {
                hs0 = new HostLineEntry();
                hs0.changed=true;
                hs0.ip = h.ip;
                hs.lines.add(hs0);
                someChange = true;
            }
            if (addEntry(hs0, h.name, session)) {
                someChange = true;
            }
            return someChange;
        }

        private boolean removeEntry(HostLines hs, String name, NSession session) {
            List<HostLineEntry> u = hs.lines.stream()
                    .filter(x -> x instanceof HostLineEntry)
                    .map(x -> (HostLineEntry) x)
                    .collect(Collectors.toList());
            boolean someChange = false;
            for (HostLineEntry u0 : u) {
                if (removeEntryByName(u0, name, session)) {
                    someChange = true;
                }
                if (Objects.equals(u0.ip, name) || u0.names.isEmpty()) {
                    hs.lines.remove(u0);
                    someChange = true;
                }
            }
            return someChange;
        }
    }

    private static class HostLines {
        List<HostLine> lines = new ArrayList<>();
    }

    private static class HostLine {
        String image;

    }

    private static class HostLineUnknown extends HostLine {

        public HostLineUnknown(String image) {
            this.image = image;
        }
    }

    private static class HostLineBlank extends HostLine {

        public HostLineBlank(String image) {
            this.image = image;
        }
    }

    private static class HostLineComment extends HostLine {
        String comment;

        public HostLineComment(String image, String comment) {
            this.image = image;
            this.comment = comment;
        }
    }


    private static class HostLineEntry extends HostLine {
        private HostLineComment groupName;
        private String ip;
        private List<String> names = new ArrayList<>();
        boolean changed;
        String buildImage(){
            if(changed){
                return ip+" "+String.join(" ",names);
            }else{
                return super.image;
            }
        }
    }

    private static class Host {
        String ip;
        String name;

        public Host(String ip, String name) {
            this.ip = ip;
            this.name = name;
        }
    }

    private static class Options {
        List<Host> toAdd = new ArrayList<>();
        List<String> toRemove = new ArrayList<>();
        String hostsFile;
    }

}
