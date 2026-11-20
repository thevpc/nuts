package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Regression tests for process PID discovery and PATH redirects (REL-07).
 */
public class ProcessExecutionReliabilityTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void processIdIsAvailableOnModernJdks() throws Exception {
        ProcessBuilder2 e = newProcess();
        e.start();
        e.waitFor();
        Assertions.assertTrue(e.getProcessId() > 0,
                "Process.pid() should provide a real PID instead of -1");
    }

    @Test
    public void pathRedirectsCaptureOutputAndError() throws Exception {
        Path out = Files.createTempFile("nuts-proc-out", ".txt");
        Path err = Files.createTempFile("nuts-proc-err", ".txt");
        ProcessBuilder2 e = newProcess();
        e.setOut(NExecOutput.ofPath(NPath.of(out)));
        e.setErr(NExecOutput.ofPath(NPath.of(err)));
        e.start();
        e.waitFor();
        Assertions.assertTrue(Files.size(err) > 0,
                "stderr must be written to the requested PATH redirect");
    }

    private static ProcessBuilder2 newProcess() {
        ProcessBuilder2 e = new ProcessBuilder2();
        e.addCommand(java.nio.file.Paths.get(System.getProperty("java.home"), "bin", "java").toString(), "-version");
        return e;
    }
}
