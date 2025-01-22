/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.CharacterizedExecFile;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmd;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;

import java.io.Closeable;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;

/**
 * @author thevpc
 */
public class DefaultNArtifactPathExecutable extends AbstractNExecutableInformationExt implements Closeable {

    String cmdName;
    String[] args;
    List<String> executorOptions;
    List<String> workspaceOptions;
    NExecutionType executionType;
    NRunAs runAs;
    DefaultNExecCmd execCommand;
    DefaultNDefinition nutToRun;
    CharacterizedExecFile c;
    String tempFolder;
    DefaultNExecCmd.NExecutorComponentAndContext executorComponentAndContext;

    public DefaultNArtifactPathExecutable(NWorkspace workspace,String cmdName, String[] args, List<String> executorOptions, List<String> workspaceOptions,
                                          NExecutionType executionType, NRunAs runAs, DefaultNExecCmd execCommand,
                                          DefaultNDefinition nutToRun,
                                          CharacterizedExecFile c,
                                          String tempFolder,
                                          DefaultNExecCmd.NExecutorComponentAndContext executorComponentAndContext
    ) {
        super(workspace,cmdName,
                NCmdLine.of(args).toString(),
                NExecutableType.ARTIFACT, execCommand);
        this.c = c;
        this.tempFolder = tempFolder;
        this.runAs = runAs;
        this.nutToRun = nutToRun;
        this.cmdName = cmdName;
        this.args = args;
        this.executionType = executionType;
        this.execCommand = execCommand;
        this.executorOptions = executorOptions;
        this.workspaceOptions = workspaceOptions;
        this.executorComponentAndContext = executorComponentAndContext;
    }

    @Override
    public NId getId() {
        return this.nutToRun.getId();
    }

    @Override
    public int execute() {
        return executeHelper();
    }

    public int executeHelper() {
        try {
            return executorComponentAndContext.getComponent().exec(executorComponentAndContext.getExecutionContext());
        } finally {
            dispose();
        }
    }

    @Override
    public void close() {
        dispose();
    }

    private void dispose() {
        if (tempFolder != null) {
            try {
                NIOUtils.delete(Paths.get(tempFolder));
            } catch (UncheckedIOException | NIOException e) {
                LOG().with().level(Level.FINEST).verb(NLogVerb.FAIL)
                        .log(NMsg.ofC("unable to delete temp folder created for execution : %s", tempFolder));
            }
        }
        c.close();
    }

    @Override
    public String toString() {
        return "nuts " + cmdName + " " + NCmdLine.of(args).toString();
    }

}
