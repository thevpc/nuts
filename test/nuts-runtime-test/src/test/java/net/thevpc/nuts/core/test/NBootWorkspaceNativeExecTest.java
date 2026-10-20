package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.NBootException;
import net.thevpc.nuts.boot.NBootWorkspaceNativeExec;
import net.thevpc.nuts.boot.internal.util.NBootMsg;
import net.thevpc.nuts.boot.internal.util.NBootWorkspaceHelper;
import net.thevpc.nuts.boot.NBootOptionsInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

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

        NBootException error = Assertions.assertThrows(NBootException.class, workspace::runWorkspace0);

        Assertions.assertTrue(error.getMessage().contains("Unable to run command"));
        Assertions.assertEquals(1, workspace.options().errors().size());
    }
}
