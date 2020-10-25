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
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <br>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <br>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.toolbox.nsh.AbstractNshBuiltin;
import net.vpc.app.nuts.toolbox.nsh.util.ShellHelper;
import net.vpc.common.xfile.XFile;

import java.io.*;
import java.util.*;
import net.vpc.app.nuts.toolbox.nsh.NutsShellContext;
import net.vpc.app.nuts.toolbox.nsh.NshExecutionContext;

/**
 * Created by vpc on 1/7/17.
 */
public class PropsCommand extends AbstractNshBuiltin {

    public PropsCommand() {
        super("props", DEFAULT_SUPPORT);
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

    public void exec(String[] args, NshExecutionContext context) {
        NutsCommandLine cmdLine = cmdLine(args, context);
        Options o = new Options();
        NutsArgument a;
        NutsCommandLineFormat commandLineFormat = context.getWorkspace().commandLine();
        do {
            if (context.configureFirst(cmdLine)) {
                //
            } else if (cmdLine.next("get") != null) {
                o.property = cmdLine.next().getString();
                o.action = "get";
                while (cmdLine.hasNext()) {
                    if (cmdLine.next("--xml") != null) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--system") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.next("--props") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--file") != null) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();
                    } else {
                        cmdLine.setCommandName(getName()).unexpectedArgument();
                    }

                }
            } else if (cmdLine.next("set") != null) {
                String k = cmdLine.next().getString();
                String v = cmdLine.next().getString();
                o.updates.put(k, v);
                o.action = "set";
                while (cmdLine.hasNext()) {
                    if (cmdLine.next("--comments") != null) {
                        o.comments = cmdLine.next().getStringValue();
                    } else if (cmdLine.next("--to-props-file") != null) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--to-xml-file") != null) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();
                    } else if (cmdLine.next("--to-file") != null) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--print-props") != null) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.next("--print-xml") != null) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.next("--save") != null) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;
                    } else if (cmdLine.next("--sort") != null) {
                        o.sort = true;
                        context.getSession().addOutputFormatOptions("--sort");
                    } else if (cmdLine.next("--xml") != null) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--system") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.next("--props") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();
                    } else if (cmdLine.next("--file") != null) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();
                    } else {
                        cmdLine.setCommandName(getName()).unexpectedArgument();
                    }
                }
            } else if (cmdLine.next("list") != null) {
                o.action = "list";
                while (cmdLine.hasNext()) {
                    if (cmdLine.next("--xml") != null) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--system") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.next("--props") != null) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();

                    } else if (cmdLine.next("--file") != null) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.required().nextNonOption(commandLineFormat.createName("file")).getString();
                    } else if (cmdLine.next("--sort") != null) {
                        o.sort = true;
                        context.getSession().addOutputFormatOptions("--sort");
                    } else {
                        cmdLine.setCommandName(getName()).unexpectedArgument();
                    }
                }
            } else {
                cmdLine.setCommandName(getName()).unexpectedArgument();
            }
        } while (cmdLine.hasNext());
        if (o.sourceType != SourceType.FILE && o.sourceFile != null) {
            throw new NutsExecutionException(context.getWorkspace(), "props: Should not use file with --system flag", 2);
        }
        if (o.sourceType == SourceType.FILE && o.sourceFile == null) {
            throw new NutsExecutionException(context.getWorkspace(), "props: Missing file", 3);
        }
        if (o.action == null) {
            throw new NutsExecutionException(context.getWorkspace(), "props: Missing action", 4);
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
                        p.put(e.getKey(),e.getValue());
                    }
                    storeProperties(p,o,context);
                } catch (Exception ex) {
                    throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
                }
                return;
            }
            case "list": {
                action_list(context, o);
                return;
            }
            default: {
                throw new NutsExecutionException(context.getWorkspace(), "props: Unsupported action " + o.action, 2);
            }
        }
    }

    private void action_list(NshExecutionContext context, Options o) {
        context.getWorkspace().formats().object().setSession(context.getSession()).value(getProperties(o, context)).print();
    }

    private void action_get(NshExecutionContext context, Options o) {
        Map<String,String> p = getProperties(o, context);
        String v = p.get(o.property);
        context.getWorkspace().formats().object().setSession(context.getSession()).value(v == null ? "" : v).print();
    }

    private Map<String,String> getProperties(Options o, NshExecutionContext context) {
        Map<String,String> p = o.sort?new TreeMap<String,String>():new HashMap<String,String>();
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

    private Format detectFileFormat(String file, NshExecutionContext context) {
        if (file.toLowerCase().endsWith(".props")
                || file.toLowerCase().endsWith(".properties")) {
            return Format.PROPS;
        } else if (file.toLowerCase().endsWith(".xml")) {
            return Format.XML;
        }
        throw new NutsExecutionException(context.getWorkspace(), "Unknown file format " + file, 2);
    }

    private Map<String,String> readProperties(Options o, NshExecutionContext context) {
        Map<String,String> p = new LinkedHashMap<>();
        String sourceFile = o.sourceFile;
        XFile filePath = ShellHelper.xfileOf(sourceFile, context.getGlobalContext().getCwd());
        try (InputStream is = filePath.getInputStream()) {

            Format sourceFormat = o.sourceFormat;
            if (sourceFormat == Format.AUTO) {
                sourceFormat = detectFileFormat(filePath.getPath(), context);
            }
            switch (sourceFormat) {
                case PROPS: {
                    Properties pp=new Properties();
                    pp.load(is);
                    p.putAll((Map)pp);
                    break;
                }
                case XML: {
                    Properties pp=new Properties();
                    pp.loadFromXML(is);
                    p.putAll((Map)pp);
                    break;
                }
            }
        } catch (Exception ex) {
            throw new NutsExecutionException(context.getWorkspace(), ex.getMessage(), ex, 100);
        }
        return p;
    }

    private void storeProperties(Map<String,String> p, Options o, NshExecutionContext context) throws IOException {
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
        if (console) {
            Format format = o.targetFormat;
            switch (format) {
                case AUTO: {
                    NutsObjectFormat f = context.getWorkspace().formats().object().setSession(context.getSession()).value(p);
                    f.configure(true, context.getWorkspace().config().options().getOutputFormatOptions());
                    f.configure(true, context.getSession().getOutputFormatOptions());
                    f.println(context.getSession().out());
                    break;
                }
                case PROPS: {
                    if (o.sort && !(p instanceof SortedMap)) {
                        p = new TreeMap<String,String>(p);
                    }
                    new OrderedProperties(p).store(context.out(), o.comments);
                    break;
                }
                case XML: {
                    if (o.sort && !(p instanceof SortedMap)) {
                        p = new TreeMap<String,String>(p);
                    }
                    new OrderedProperties(p).storeToXML(context.out(), o.comments);
                    break;
                }
            }
        } else {
            XFile filePath = ShellHelper.xfileOf(targetFile, context.getGlobalContext().getCwd());
            try (OutputStream os = filePath.getOutputStream()) {
                Format format = o.targetFormat;
                if (format == Format.AUTO) {
                    format = detectFileFormat(filePath.getPath(), null);
                }
                switch (format) {
                    case PROPS: {
                        if (o.sort && !(p instanceof SortedMap)) {
                            p = new TreeMap<String,String>(p);
                        }
                        new OrderedProperties(p).store(os, o.comments);
                        break;
                    }
                    case XML: {
                        if (o.sort && !(p instanceof SortedMap)) {
                            p = new TreeMap<String,String>(p);
                        }
                        new OrderedProperties(p).storeToXML(os, o.comments);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public String getHelpHeader() {
        return "show properties vars";
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
        private Map<String,String> other;
        public OrderedProperties(Map<String,String> other) {
            putAll(other);
        }

        @Override
        public synchronized Enumeration<Object> keys() {
            return Collections.enumeration((Set)other.keySet());
        }
    }
}
