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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.cmds.common;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NArgName;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.format.NObjectFormat;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.eval.NShellExecutionContext;
import net.thevpc.nuts.toolbox.nsh.util.ShellHelper;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class PropsCommand extends NShellBuiltinDefault {

    public PropsCommand() {
        super("props", NConstants.Support.DEFAULT_SUPPORT,Options.class);
    }

    @Override
    protected boolean nextOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        if (cmdLine.next("get").isPresent()) {
            o.property = cmdLine.next().flatMap(NLiteral::asString).get(session);
            o.action = "get";
            while (cmdLine.hasNext()) {
                if (cmdLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (cmdLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);
                } else {
                    cmdLine.setCommandName(getName()).throwUnexpectedArgument();
                }

            }
            return true;
        } else if (cmdLine.next("set").isPresent()) {
            String k = cmdLine.next().flatMap(NLiteral::asString).get(session);
            String v = cmdLine.next().flatMap(NLiteral::asString).get(session);
            o.updates.put(k, v);
            o.action = "set";
            while (cmdLine.hasNext()) {
                if (cmdLine.next("--comments").isPresent()) {
                    o.comments = cmdLine.next().get(session).getStringValue().get(session);
                } else if (cmdLine.next("--to-props-file").isPresent()) {
                    o.targetFormat = Format.PROPS;
                    o.targetType = TargetType.FILE;
                    o.targetFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--to-xml-file").isPresent()) {
                    o.targetFormat = Format.XML;
                    o.targetType = TargetType.FILE;
                    o.targetFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);
                } else if (cmdLine.next("--to-file").isPresent()) {
                    o.targetFormat = Format.AUTO;
                    o.targetType = TargetType.FILE;
                    o.targetFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--print-props").isPresent()) {
                    o.targetFormat = Format.PROPS;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;

                } else if (cmdLine.next("--print-xml").isPresent()) {
                    o.targetFormat = Format.XML;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;

                } else if (cmdLine.next("--save").isPresent()) {
                    o.targetFormat = Format.AUTO;
                    o.targetType = TargetType.CONSOLE;
                    o.targetFile = null;
                } else if (cmdLine.next("--sort").isPresent()) {
                    o.sort = true;
                    session.addOutputFormatOptions("--sort");
                } else if (cmdLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (cmdLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);
                } else if (cmdLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);
                } else {
                    cmdLine.setCommandName(getName()).throwUnexpectedArgument();
                }
            }
            return true;
        } else if (cmdLine.next("list").isPresent()) {
            o.action = "list";
            while (cmdLine.hasNext()) {
                if (cmdLine.next("--xml").isPresent()) {
                    o.sourceFormat = Format.XML;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--system").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.SYSTEM;
                    o.sourceFile = null;

                } else if (cmdLine.next("--props").isPresent()) {
                    o.sourceFormat = Format.PROPS;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);

                } else if (cmdLine.next("--file").isPresent()) {
                    o.sourceFormat = Format.AUTO;
                    o.sourceType = SourceType.FILE;
                    o.sourceFile = cmdLine.nextNonOption(NArgName.of("file",session)).flatMap(NLiteral::asString).get(session);
                } else if (cmdLine.next("--sort").isPresent()) {
                    o.sort = true;
                    session.addOutputFormatOptions("--sort");
                } else {
                    cmdLine.setCommandName(getName()).throwUnexpectedArgument();
                }
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void main(NCmdLine cmdLine, NShellExecutionContext context) {
        Options o = context.getOptions();
        NSession session = context.getSession();
        cmdLine.setCommandName(getName());
        if (o.sourceType != SourceType.FILE && o.sourceFile != null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Should not use file with --system flag"), NExecutionException.ERROR_2);
        }
        if (o.sourceType == SourceType.FILE && o.sourceFile == null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Missing file"), NExecutionException.ERROR_3);
        }
        if (o.action == null) {
            throw new NExecutionException(session, NMsg.ofPlain("props: Missing action"), NExecutionException.ERROR_4);
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
                    throw new NExecutionException(session, NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_2);
                }
                return;
            }
            case "list": {
                action_list(context, o);
                return;
            }
            default: {
                throw new NExecutionException(session, NMsg.ofC("props: Unsupported action %s", o.action), NExecutionException.ERROR_2);
            }
        }
    }


    @Override
    public String getHelpHeader() {
        return "show properties vars";
    }

    private void action_list(NShellExecutionContext context, Options o) {
        NObjectFormat.of(context.getSession()).setValue(getProperties(o, context)).print();
    }

    private int action_get(NShellExecutionContext context, Options o) {
        Map<String, String> p = getProperties(o, context);
        String v = p.get(o.property);
        NObjectFormat.of(context.getSession()).setValue(v == null ? "" : v).print();
        return 0;
    }

    private Map<String, String> getProperties(Options o, NShellExecutionContext context) {
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

    private Format detectFileFormat(String file, NShellExecutionContext context) {
        if (file.toLowerCase().endsWith(".props")
                || file.toLowerCase().endsWith(".properties")) {
            return Format.PROPS;
        } else if (file.toLowerCase().endsWith(".xml")) {
            return Format.XML;
        }
        throw new NExecutionException(context.getSession(), NMsg.ofC("unknown file format %s", file), NExecutionException.ERROR_2);
    }

    private Map<String, String> readProperties(Options o, NShellExecutionContext context) {
        Map<String, String> p = new LinkedHashMap<>();
        String sourceFile = o.sourceFile;
        NPath filePath = ShellHelper.xfileOf(sourceFile, context.getDirectory(), context.getSession());
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
            throw new NExecutionException(context.getSession(), NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_2);
        }
        return p;
    }

    private void storeProperties(Map<String, String> p, Options o, NShellExecutionContext context) throws IOException {
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
                    f.configure(true, NBootManager.of(session).getBootOptions().getOutputFormatOptions().orElseGet(Collections::emptyList).toArray(new String[0]));
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
            NPath filePath = ShellHelper.xfileOf(targetFile, context.getDirectory(), session);
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

    @Override
    protected boolean nextNonOption(NArg arg, NCmdLine cmdLine, NShellExecutionContext context) {
        return nextOption(arg, cmdLine, context);
    }
}
