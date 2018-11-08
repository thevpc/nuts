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

import net.vpc.app.nuts.extensions.util.CoreStringUtils;
import net.vpc.app.nuts.toolbox.nsh.AbstractNutsCommand;
import net.vpc.app.nuts.toolbox.nsh.NutsCommandContext;
import net.vpc.app.nuts.toolbox.nsh.util.FileUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by vpc on 1/7/17.
 */
public class PropertiesCommand extends AbstractNutsCommand {

    public PropertiesCommand() {
        super("properties", DEFAULT_SUPPORT);
    }

    public static class Options {
        String keyPassword = null;
        String keyFilePath = null;
        String property = null;
        String action = null;
        String file = null;
        boolean sort = false;
        boolean xml = false;
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
            } else if (cmdLine.readOnce("--password")) {
                o.keyPassword = cmdLine.readValue();
            } else if (cmdLine.readOnce("--cert")) {
                o.keyFilePath = cmdLine.readValue();
            } else if (cmdLine.readOnce("--sort")) {
                o.sort=true;
            } else if (cmdLine.readOnce("--xml")) {
                o.xml=true;
            } else if (cmdLine.readOnce("read")) {
                o.property = cmdLine.readValue();
                o.action = "read";
            } else if (cmdLine.readOnce("list")) {
                o.action = "list";
            } else {
                if (o.file == null) {
                    o.file = cmdLine.readNonOptionOrError("file").getString();
                } else {
                    cmdLine.requireEmpty();
                }
            }
        }
        if (o.file == null) {
            throw new IllegalArgumentException("Missing file");
        }
        if (o.action == null) {
            throw new IllegalArgumentException("Missing action");
        }
        switch (o.action) {
            case "read": {
                Properties p = readProperties(o);
                PrintStream out = context.getFormattedOut();
                String v = p.getProperty(o.property);
                if (v != null) {
                    out.println(v);
                    return 0;
                }
                out.println("");
                return 1;
            }
            case "list": {
                Properties p = readProperties(o);
                PrintStream out = context.getFormattedOut();
                List<String> keys=new ArrayList(p.keySet());
                if(o.sort){
                    keys.sort(null);
                }
                int maxSize=0;
                for (String o1 : keys) {
                    maxSize=Math.max(maxSize,formatValue(o1).length());
                }
                for (String o1 : keys) {
                    String v1 = p.getProperty(o1);
                    out.printf("[[%s]] = %s\n", CoreStringUtils.alignLeft(formatValue(o1),maxSize), formatValue(v1));
                }
                return 0;
            }
            default: {
                throw new IllegalArgumentException("Unsupported action " + o.action);
            }
        }
    }

    private String formatValue(String value) {
        if(value==null){
            return "";
        }
        StringBuilder sb=new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c){
                case '\n':{
                    sb.append("\\n");
                    break;
                }
                default: {
                    sb.append(c);
                    break;
                }
            }
        }
        return sb.toString();
    }

    private Properties readProperties(Options o) throws IOException {
        Properties p = new Properties();
        try (InputStream is = FileUtils.getInputStream(o.file, o.keyFilePath, o.keyPassword)) {
            if(o.xml){
                p.loadFromXML(is);
            }else{
                p.load(is);
            }
        }
        return p;
    }
}
