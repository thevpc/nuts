/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.commands;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.DefaultNutsContent;
import net.vpc.app.nuts.core.DefaultNutsDefinition;
import net.vpc.app.nuts.core.DefaultNutsExecCommand;
import net.vpc.app.nuts.core.util.CharacterizedFile;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsPathComponentExecutable extends AbstractNutsExecutableCommand {
    
    String cmdName;
    String[] args;
    String[] executorOptions;
    boolean embedded;
    NutsWorkspace ws;
    NutsSession session;
    DefaultNutsExecCommand execCommand;

    public DefaultNutsPathComponentExecutable(String cmdName, String[] args, String[] executorOptions, boolean embedded, NutsWorkspace ws, NutsSession session, DefaultNutsExecCommand execCommand) {
        super(cmdName, 
                ws.parser().parseCommand(args).getCommandLine(),
                NutsExecutableType.COMPONENT);
        this.cmdName = cmdName;
        this.args = args;
        this.executorOptions = executorOptions;
        this.embedded = embedded;
        this.ws = ws;
        this.session = session;
    }

    @Override
    public NutsId getId() {
        NutsFetchCommand p = ws.fetch();
        p.setTransitive(true);
        try (final CharacterizedFile c = CoreIOUtils.characterize(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
            return c.descriptor == null ? null : c.descriptor.getId();
        }
    }

    @Override
    public void execute() {
        NutsFetchCommand p = ws.fetch();
        p.setTransitive(true);
        try (final CharacterizedFile c = CoreIOUtils.characterize(ws, CoreIOUtils.createInputSource(cmdName), p, session)) {
            if (c.descriptor == null) {
                //this is a native file?
                c.descriptor = DefaultNutsExecCommand.TEMP_DESC;
            }
            NutsDefinition nutToRun = new DefaultNutsDefinition(ws, null, c.descriptor.getId(), c.descriptor, new DefaultNutsContent(c.getContentPath(), false, c.temps.size() > 0), null);
            execCommand.ws_exec(nutToRun, cmdName, args, executorOptions, execCommand.getEnv(), execCommand.getDirectory(), execCommand.isFailFast(), session, embedded);
        }
    }

    @Override
    public String toString() {
        return "NUTS " + cmdName + " " + ws.parser().parseCommand(args).getCommandLine();
    }
    
}
