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
package net.vpc.app.nuts.toolbox.nsh.cmds;

import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.FilePath;
import net.vpc.common.commandline.format.PropertiesFormatter;

import java.io.*;
import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class PropsCommand extends AbstractNutsCommand {

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
        public String comments;
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        net.vpc.common.commandline.CommandLine cmdLine = cmdLine(args, context);
        cmdLine.requireNonEmpty();
        Options o = new Options();
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("--version", "-v")) {
                break;
            } else if (cmdLine.readOnce("--help", "-h")) {
                break;
            } else if (cmdLine.readOnce("get")) {
                o.property = cmdLine.readValue();
                o.action = "get";
                while (!cmdLine.isEmpty()) {
                    if (cmdLine.readOnce("--xml")) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();

                    } else if (cmdLine.readOnce("--system")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.readOnce("--props")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();

                    } else if (cmdLine.readOnce("--file")) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();
                    } else {
                        cmdLine.requireEmpty();
                    }

                }
            } else if (cmdLine.readOnce("set")) {
                String k = cmdLine.readValue();
                String v = cmdLine.readValue();
                o.updates.put(k, v);
                o.action = "set";
                while (!cmdLine.isEmpty()) {
                    if (cmdLine.readOnce("--comments")) {
                        o.comments = cmdLine.readValue();
                    } else if (cmdLine.readOnce("--to-props-file")) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readNonOptionOrError("file").getString();

                    } else if (cmdLine.readOnce("--to-xml-file")) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readNonOptionOrError("file").getString();
                    } else if (cmdLine.readOnce("--to-file")) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.FILE;
                        o.targetFile = cmdLine.readNonOptionOrError("file").getString();

                    } else if (cmdLine.readOnce("--print-props")) {
                        o.targetFormat = Format.PROPS;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.readOnce("--print-xml")) {
                        o.targetFormat = Format.XML;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;

                    } else if (cmdLine.readOnce("--save")) {
                        o.targetFormat = Format.AUTO;
                        o.targetType = TargetType.CONSOLE;
                        o.targetFile = null;
                    } else if (cmdLine.readOnce("--sort")) {
                        o.sort = true;
                    } else if (cmdLine.readOnce("--xml")) {
                        o.sourceFormat = Format.XML;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();

                    } else if (cmdLine.readOnce("--system")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.SYSTEM;
                        o.sourceFile = null;

                    } else if (cmdLine.readOnce("--props")) {
                        o.sourceFormat = Format.PROPS;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();
                    } else if (cmdLine.readOnce("--file")) {
                        o.sourceFormat = Format.AUTO;
                        o.sourceType = SourceType.FILE;
                        o.sourceFile = cmdLine.readNonOptionOrError("file").getString();
                    } else {
                        cmdLine.requireEmpty();
                    }
                }
            } else if (cmdLine.readOnce("list")) {
                o.action = "list";
                while (!cmdLine.isEmpty()) {
                    cmdLine.requireEmpty();
                }
            } else {
                cmdLine.requireEmpty();
            }
        }
        if (o.sourceType != SourceType.FILE && o.sourceFile != null) {
            throw new IllegalArgumentException("Should not use file with --system flag");
        }
        if (o.sourceType == SourceType.FILE && o.sourceFile == null) {
            throw new IllegalArgumentException("Missing file");
        }
        if (o.action == null) {
            throw new IllegalArgumentException("Missing action");
        }
        switch (o.action) {
            case "get": {
                return action_get(context, o);
            }
            case "set": {
                switch (o.sourceType) {
                    case FILE: {
                        Properties p = readProperties(o);
                        if (o.targetType == TargetType.FILE) {
                            try (FileWriter os = new FileWriter(
                                    o.targetFile == null ? o.targetFile : o.sourceFile
                            )) {
                                p.store(os, o.comments);
                            }
                        } else {
                            try (FileWriter os = new FileWriter(o.sourceFile)) {
                                p.store(os, o.comments);
                            }
                        }
                    }
                }
                return action_get(context, o);
            }
            case "list": {
                return action_list(context, o);
            }
            default: {
                throw new IllegalArgumentException("Unsupported action " + o.action);
            }
        }
    }

    private int action_list(NutsCommandContext context, Options o) throws IOException {
        Properties p = getProperties(o);
        PrintStream out = context.getFormattedOut();
        PropertiesFormatter f=new PropertiesFormatter()
                .setSort(o.sort)
                .setTable(true)
                ;
        f.format(p,out);
        return 0;
    }

    private int action_get(NutsCommandContext context, Options o) throws IOException {
        Properties p = getProperties(o);
        PrintStream out = context.getFormattedOut();
        String v = p.getProperty(o.property);
        if (v != null) {
            out.println(v);
            return 0;
        }
        out.println("");
        return 1;
    }

    private Properties getProperties(Options o) throws IOException {
        Properties p = new Properties();
        switch (o.sourceType) {
            case FILE: {
                p = readProperties(o);
                break;
            }
            case SYSTEM: {
                p = System.getProperties();
                break;
            }
        }
        return p;
    }



    private Format detectFileFormat(String file) {
        if (
                file.toLowerCase().endsWith(".props")
                        || file.toLowerCase().endsWith(".properties")
                ) {
            return Format.PROPS;
        } else if (file.toLowerCase().endsWith(".xml")) {
            return Format.XML;
        }
        throw new IllegalArgumentException("Unknown file format " + file);
    }

    private Properties readProperties(Options o) throws IOException {
        Properties p = new Properties();
        String sourceFile = o.sourceFile;
        FilePath filePath = new FilePath(sourceFile);
        try (InputStream is = filePath.getInputStream()) {

            Format sourceFormat = o.sourceFormat;
            if (sourceFormat == Format.AUTO) {
                sourceFormat = detectFileFormat(filePath.getPath());
            }
            switch (sourceFormat) {
                case PROPS: {
                    p.load(is);
                    break;
                }
                case XML: {
                    p.loadFromXML(is);
                    break;
                }
            }
        }
        return p;
    }

    private void storeProperties(Properties p, Options o, NutsCommandContext context) throws IOException {
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
                    PropertiesFormatter f=new PropertiesFormatter()
                            .setSort(o.sort)
                            .setTable(true)
                            ;
                    f.format(p,context.getFormattedOut());
                    break;
                }
                case PROPS: {
                    if(o.sort){
                        p=new SortedProperties(p);
                    }
                    p.store(context.getOut(), o.comments);
                    break;
                }
                case XML: {
                    if(o.sort){
                        p=new SortedProperties(p);
                    }
                    p.storeToXML(context.getOut(), o.comments);
                    break;
                }
            }
        } else {
            FilePath filePath=new FilePath(targetFile);
            try (OutputStream os = filePath.getOutputStream()) {
                Format format = o.targetFormat;
                if (format == Format.AUTO) {
                    format = detectFileFormat(filePath.getPath());
                }
                switch (format) {
                    case PROPS: {
                        if(o.sort){
                            p=new SortedProperties(p);
                        }
                        p.store(os, o.comments);
                        break;
                    }
                    case XML: {
                        if(o.sort){
                            p=new SortedProperties(p);
                        }
                        p.storeToXML(os, o.comments);
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
}
