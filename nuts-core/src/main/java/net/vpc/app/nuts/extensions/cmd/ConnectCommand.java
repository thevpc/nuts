/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.NutsIllegalArgumentsException;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.NutsCommandContext;
import net.vpc.app.nuts.NutsConstants;
import net.vpc.app.nuts.extensions.cmd.cmdline.CmdLine;
import net.vpc.app.nuts.extensions.cmd.cmdline.DefaultNonOption;
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.NutsNonBlockingInputStreamAdapter;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Created by vpc on 1/7/17.
 */
public class ConnectCommand extends AbstractNutsCommand {

    public ConnectCommand() {
        super("connect", 1);
    }

    public int exec(String[] args, NutsCommandContext context) throws Exception {
        NutsCommandAutoComplete autoComplete = context.getAutoComplete();
        CmdLine cmdLine = new CmdLine(autoComplete, args);
        String password = null;
        String server = null;
        while (!cmdLine.isEmpty()) {
            if (cmdLine.readOnce("--password")) {
                password = cmdLine.readNonOptionOrError(new DefaultNonOption("Password")).getStringOrError();
            } else {
                server = cmdLine.readNonOptionOrError(new DefaultNonOption("ServerAddress")).getStringOrError();
                cmdLine.requireEmpty();
            }
        }
        if (!cmdLine.isExecMode()) {
            return -1;
        }
        String login = null;
        int port = -1;
        if (server == null) {
            throw new NutsIllegalArgumentsException("Missing address");
        }
        if (server.contains("@")) {
            login = server.substring(0, server.indexOf("@"));
            server = server.substring(server.indexOf("@") + 1);
        }
        if (server.contains(":")) {
            port = Integer.parseInt(server.substring(server.indexOf(":") + 1));
            server = server.substring(0, server.indexOf(":"));
        }
        if (!CoreStringUtils.isEmpty(login) && CoreStringUtils.isEmpty(password)) {
            password = context.getTerminal().readPassword("Password:");
        }
        Socket socket = null;
        try {
            int validPort = port <= 0 ? NutsConstants.DEFAULT_ADMIN_SERVER_PORT : port;
            socket = new Socket(InetAddress.getByName(server), validPort);
            CoreIOUtils.pipe("pipe-out-socket-" + server + ":" + validPort, new NutsNonBlockingInputStreamAdapter("pipe-out-socket-" + server + ":" + validPort, socket.getInputStream()), context.getTerminal().getOut());
            PrintStream out = new PrintStream(socket.getOutputStream());
            if (!CoreStringUtils.isEmpty(login)) {
                out.printf("connect ==%s %s== \n",login,password);
            }
            while (true) {
                String line = context.getTerminal().readLine("");
                if (line == null) {
                    break;
                }
                if (line.trim().length() > 0) {
                    if (line.trim().equals("quit") || line.trim().equals("exit")) {
                        break;
                    }
                    out.printf("%s\n",line);
                }
            }
        } finally {
            if (socket != null) {
                socket.close();
            }
        }

        return 0;

    }
}
