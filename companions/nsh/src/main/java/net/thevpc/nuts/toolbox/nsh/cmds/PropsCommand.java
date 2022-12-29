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
package net.thevpc.nuts.toolbox.nsh.cmds;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArgumentName;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.SimpleJShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.jshell.JShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class PropsCommand extends SimpleJShellBuiltin {

    public PropsCommand() {
        super("props", DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean configureFirst(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        if (commandLine.next("get").isPresent()) {
            o.property = commandLine.next().flatMap(NValue::asString).get(session);
            o.action = "get";
            while (commandLine.hasNext()) {
                if (commandLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (commandLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);
                } else {
                    commandLine.setCommandName(getName()).throwUnexpectedArgument();
                }

            }
            return true;
        } else if (commandLine.next("set").isPresent()) {
            String k = commandLine.next().flatMap(NValue::asString).get(session);
            String v = commandLine.next().flatMap(NValue::asString).get(session);
            o.updates.put(k, v);
            o.action = "set";
            while (commandLine.hasNext()) {
                if (commandLine.next("--comments").isPresent()) {
                    o.comments = commandLine.next().get(session).getStringValue().get(session);
                } else if (commandLine.next("--to-props-file").isPresent()) {
                    o.targetFormat = Format.PROPS;
                    o.targetType = TargetType.FILE;
                    o.targetFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--to-xml-file").isPresent()) {
                    o.targetFormat = Format.XML;
                    o.targetType = TargetType.FILE;
                    o.targetFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);
                } else if (commandLine.next("--to-file").isPresent()) {
                    o.targetFormat = Format.AUTO;
                    o.targetType = TargetType.FILE;
                    o.targetFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--print-props").isPresent()) {
                    o.targetFormat = Format.PROPS;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;

                } else if (commandLine.next("--print-xml").isPresent()) {
                    o.targetFormat = Format.XML;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;

                } else if (commandLine.next("--save").isPresent()) {
                    o.targetFormat = Format.AUTO;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;
                } else if (commandLine.next("--sort").isPresent()) {
                    o.sort = true;
                    session.addOutputFormatOptions("--sort");
                } else if (commandLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (commandLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);
                } else if (commandLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);
                } else {
                    commandLine.setCommandName(getName()).throwUnexpectedArgument();
                }
            }
            return true;
        } else if (commandLine.next("list").isPresent()) {
            o.action = "list";
            while (commandLine.hasNext()) {
                if (commandLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (commandLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);

                } else if (commandLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = commandLine.nextNonOption(NArgumentName.of("file",session)).flatMap(NValue::asString).get(session);
                } else if (commandLine.next("--sort").isPresent()) {
                    o.sort = true;
                    session.addOutputFormatOptions("--sort");
                } else {
                    commandLine.setCommandName(getName()).throwUnexpectedArgument();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void execBuiltin(NCommandLine commandLine, JShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        commandLine.setCommandName(getName());
        if (o.sourceType != SourceType.FILE && o.sourceFile != null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Should not use file with --system flag"), 2);
        }
        if (o.sourceType == SourceType.FILE && o.sourceFile == null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Missing file"), 3);
        }
        if (o.action == null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Missing action"), 4);
        }
        switch (o.action) {
            case "get": {
                action_get(context, o);
                return;
            }
            case "set": {
                Map<String, String> p = getProperties(o, context);
                try {
                    for (Map.Entry<String, String> e : o.updates.entrySet()) {
                        p.put(e.getKey(), e.getValue());
                    }
                    storeProperties(p, o, context);
                } catch (Exception ex) {
                    throw new NExecutionException(session, NMsg.ofCstyle("%s", ex), ex, 100);
                }
                return;
            }
            case "list": {
                action_list(context, o);
                return;
            }
            default: {
                throw new NExecutionException(session, NMsg.ofCstyle("props: Unsupported action %s", o.action), 2);
            }
        }
    }


    @Override
    public String getHelpHeader() {
        return "show properties vars";
    }

    private void action_list(JShellExecutionContext context, Options o) {
        NObjectFormat.of(context.getSession()).setValue(getProperties(o, context)).print();
    }

    private int action_get(JShellExecutionContext context, Options o) {
        Map<String, String> p = getProperties(o, context);
        String v = p.get(o.property);
        NObjectFormat.of(context.getSession()).setValue(v == null ? "" : v).print();
        return 0;
    }

    private Map<String, String> getProperties(Options o, JShellExecutionContext context) {
        Map<String, String> p = o.sort ? new TreeMap<String, String>() : new HashMap<String, String>();
        switch (o.sourceType) {
            case FILE: {
                p.putAll(readProperties(o, context));
                break;
            }
            case SYSTEM: {
                p = new TreeMap(System.getProperties());
                break;
            }
        }
        return p;
    }

    private Format detectFileFormat(String file, JShellExecutionContext context) {
        if (file.toLowerCase().endsWith(".props")
                || file.toLowerCase().endsWith(".properties")) {
            return Format.PROPS;
        } else if (file.toLowerCase().endsWith(".xml")) {
            return Format.XML;
        }
        throw new NExecutionException(context.getSession(), NMsg.ofCstyle("unknown file format %s", file), 2);
    }

    private Map<String, String> readProperties(Options o, JShellExecutionContext context) {
        Map<String, String> p = new LinkedHashMap<>();
        String sourceFile = o.sourceFile;
        NPath filePath = ShellHelper.xfileOf(sourceFile, context.getCwd(), context.getSession());
        try (InputStream is = filePath.getInputStream()) {

            Format sourceFormat = o.sourceFormat;
            if (sourceFormat == Format.AUTO) {
                sourceFormat = detectFileFormat(filePath.getName(), context);
            }
            switch (sourceFormat) {
                case PROPS: {
                    Properties pp = new Properties();
                    pp.load(is);
                    p.putAll((Map) pp);
                    break;
                }
                case XML: {
                    Properties pp = new Properties();
                    pp.loadFromXML(is);
                    p.putAll((Map) pp);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new NExecutionException(context.getSession(), NMsg.ofCstyle("%s", ex), ex, 100);
        }
        return p;
    }

    private void storeProperties(Map<String, String> p, Options o, JShellExecutionContext context) throws IOException {
        String targetFile = o.targetFile;
        boolean console = false;
        switch (o.targetType) {
            case AUTO: {
                if (targetFile == null) {
                    targetFile = o.sourceFile;
                }
                break;
            }
            case CONSOLE: {
                console = true;
                break;
            }
        }
        NSession session = context.getSession();
        if (console) {
            Format format = o.targetFormat;
            switch (format) {
                case AUTO: {
                    NObjectFormat f = NObjectFormat.of(session).setValue(p);
                    f.configure(true, session.boot().getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
                    f.configure(true, session.getOutputFormatOptions().toArray(new String[0]));
                    f.println(session.out());
                    break;
                }
                case PROPS: {
                    if (o.sort && !(p instanceof SortedMap)) {
                        p = new TreeMap<String, String>(p);
                    }
                    new OrderedProperties(p).store(context.out().asPrintStream(), o.comments);
                    break;
                }
                case XML: {
                    if (o.sort && !(p instanceof SortedMap)) {
                        p = new TreeMap<String, String>(p);
                    }
                    new OrderedProperties(p).storeToXML(context.out().asPrintStream(), o.comments);
                    break;
                }
            }
        } else {
            NPath filePath = ShellHelper.xfileOf(targetFile, context.getCwd(), session);
            try (OutputStream os = filePath.getOutputStream()) {
                Format format = o.targetFormat;
                if (format == Format.AUTO) {
                    format = detectFileFormat(filePath.getName(), null);
                }
                switch (format) {
                    case PROPS: {
                        if (o.sort && !(p instanceof SortedMap)) {
                            p = new TreeMap<String, String>(p);
                        }
                        new OrderedProperties(p).store(os, o.comments);
                        break;
                    }
                    case XML: {
                        if (o.sort && !(p instanceof SortedMap)) {
                            p = new TreeMap<String, String>(p);
                        }
                        new OrderedProperties(p).storeToXML(os, o.comments);
                        break;
                    }
                }
            }
        }
    }

    public enum SourceType {
        FILE,
        SYSTEM
    }

    public enum TargetType {
        AUTO,
        FILE,
        CONSOLE
    }

    public enum Format {
        PROPS,
        XML,
        AUTO,
    }

    public static class Options {

        String property = null;
        String action = null;
        Format sourceFormat = Format.AUTO;
        String sourceFile = null;
        String targetFile = null;
        Format targetFormat = Format.AUTO;
        boolean sort = false;
        Map<String, String> updates = new HashMap<>();
        SourceType sourceType = SourceType.FILE;
        TargetType targetType = TargetType.FILE;
        String comments;
    }

    private static class SortedProperties extends Properties {

        public SortedProperties(Properties other) {
            putAll(other);
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration(new TreeSet<>((Set) super.keySet()));
        }
    }

    private static class OrderedProperties extends Properties {
        private Map<String, String> other;

        public OrderedProperties(Map<String, String> other) {
            putAll(other);
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration((Set) other.keySet());
        }
    }
}
