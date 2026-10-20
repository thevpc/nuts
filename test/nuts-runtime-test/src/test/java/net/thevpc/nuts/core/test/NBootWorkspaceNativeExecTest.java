package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.NBootCompleteRequest;
import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootWorkspaceNativeExec;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.boot.internal.util.NBootWorkspaceHelper;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

public class NBootWorkspaceNativeExecTest {

    @Test
    public void addErrorStoresFirstErrorWhenErrorListWasNull() {
        NBootOptionsInfo options = new NBootOptionsInfo();

        NBootWorkspaceHelper.addError(NBootMsg.ofPlain("missing command"), options);

        Assertions.assertEquals(1, options.errors().size());
        Assertions.assertEquals("missing command", options.errors().get(0));
    }

    @Test
    public void nativeExecWithoutCommandReportsMissingCommand() {
        NBootWorkspaceNativeExec workspace = new NBootWorkspaceNativeExec(NBootArguments.ofOptionArgs());

        NBootException error = Assertions.assertThrows(NBootException.class, workspace::runWorkspace);

        Assertions.assertTrue(error.getMessage().contains("Unable to run command"));
        Assertions.assertEquals(1, workspace.options().errors().size());
    }

    @Test
    public void nativeExecHelpRunsWithoutRuntimeContext() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NBootArguments arguments = NBootArguments.ofOptionArgs("--help")
                .out(new PrintStream(output));
        NBootWorkspaceNativeExec workspace = new NBootWorkspaceNativeExec(arguments);

        Assertions.assertDoesNotThrow(workspace::runWorkspace);
        Assertions.assertTrue(output.toString().contains("nuts is an open source package manager"));
    }

    @Test
    public void nativeExecCompletionRunsWithoutRuntimeContext() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        NBootArguments arguments = new NBootArguments()
                .complete(new NBootCompleteRequest(0, 0))
                .out(new PrintStream(output));
        NBootWorkspaceNativeExec workspace = new NBootWorkspaceNativeExec(arguments);

        Assertions.assertDoesNotThrow(workspace::runWorkspace);
        Assertions.assertFalse(output.toString().isEmpty());
    }
}
