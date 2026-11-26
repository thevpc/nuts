package net.thevpc.nuts.core.test.concurrent;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.DefaultNInstalledRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstallLogAction;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstallLogRecord;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * A workspace is shared by every nuts process of the same user, including the
 * children nuts itself forks. This test starts several independent JVMs that
 * all open the same, fresh workspace at the same time and then write to the
 * shared install log.
 */
public class ConcurrentWorkspaceTest {

    private static final int PROCESSES = 4;
    private static final int RECORDS = 40;

    @TempDir
    Path tempDir;

    @Test
    public void concurrentProcessesShareOneWorkspace() throws Exception {
        Path workspace = tempDir.resolve("shared-ws").toAbsolutePath();
        List<Process> processes = new ArrayList<>();
        for (int i = 0; i < PROCESSES; i++) {
            processes.add(startProbe("write", workspace, i, PROCESSES, RECORDS));
        }
        String workspaceUuid = null;
        for (int i = 0; i < processes.size(); i++) {
            String out = waitFor(processes.get(i));
            Assertions.assertTrue(out.contains("WORKER_OK"), "worker " + i + " failed:\n" + out);
            String uuid = parseValue(out, "WORKER_UUID=");
            Assertions.assertNotNull(uuid, "worker " + i + " did not report a workspace uuid:\n" + out);
            if (workspaceUuid == null) {
                workspaceUuid = uuid;
            } else {
                Assertions.assertEquals(workspaceUuid, uuid,
                        "workers opened different workspaces:\n" + out);
            }
        }
        String verify = runProbe("verify", workspace, 0, PROCESSES, 0);
        Assertions.assertEquals(workspaceUuid, parseValue(verify, "VERIFY_UUID="),
                "verification process opened a different workspace:\n" + verify);
        int count = parseLogCount(verify);
        Assertions.assertEquals(PROCESSES * RECORDS, count,
                "install log records were lost across processes:\n" + verify);
    }

    private Process startProbe(String mode, Path workspace, int worker, int processes, int records) throws Exception {
        List<String> cmd = new ArrayList<>();
        cmd.add(System.getProperty("java.home") + File.separator + "bin" + File.separator + "java");
        cmd.add("-cp");
        cmd.add(buildChildClassPath());
        cmd.add(Probe.class.getName());
        cmd.add(mode);
        cmd.add(workspace.toString());
        cmd.add(String.valueOf(worker));
        cmd.add(String.valueOf(processes));
        cmd.add(String.valueOf(records));
        ProcessBuilder pb = new ProcessBuilder(cmd);
        // The test runner may itself be running inside a nuts workspace. Do not
        // leak that workspace (or its store locations) into the children.
        pb.environment().keySet().removeIf(k -> k.startsWith("NUTS_"));
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private String runProbe(String mode, Path workspace, int worker, int processes, int records) throws Exception {
        return waitFor(startProbe(mode, workspace, worker, processes, records));
    }

    private static String waitFor(Process process) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
        }
        Assertions.assertTrue(process.waitFor(240, TimeUnit.SECONDS), "probe process timed out");
        Assertions.assertEquals(0, process.exitValue(), "probe process failed:\n" + sb);
        return sb.toString();
    }

    private static int parseLogCount(String output) {
        for (String line : output.split("\\r?\\n")) {
            if (line.startsWith("PROBE_LOG_COUNT=")) {
                return Integer.parseInt(line.substring("PROBE_LOG_COUNT=".length()).trim());
            }
        }
        Assertions.fail("missing PROBE_LOG_COUNT in:\n" + output);
        return -1;
    }

    private static String parseValue(String output, String prefix) {
        for (String line : output.split("\\r?\\n")) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private static String buildChildClassPath() throws URISyntaxException {
        Set<String> cp = new LinkedHashSet<>();
        cp.add(codeSource(Nuts.class));
        cp.add(codeSource(NWorkspaceExt.class));
        cp.add(codeSource(ConcurrentWorkspaceTest.class));
        String current = System.getProperty("java.class.path");
        if (current != null) {
            for (String s : current.split(File.pathSeparator)) {
                if (!s.trim().isEmpty()) {
                    cp.add(s);
                }
            }
        }
        return String.join(File.pathSeparator, cp);
    }

    private static String codeSource(Class<?> type) throws URISyntaxException {
        return new File(type.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    }

    /**
     * Runs in a fresh JVM.
     */
    public static class Probe {
        public static void main(String[] args) {
            try {
                String mode = args[0];
                String workspacePath = args[1];
                int worker = Integer.parseInt(args[2]);
                int processes = Integer.parseInt(args[3]);
                int records = Integer.parseInt(args[4]);
                NWorkspace workspace = Nuts.openWorkspace(
                        "--workspace=" + workspacePath,
                        "--standalone",
                        "--desktop-launcher=unsupported",
                        "--menu-launcher=unsupported",
                        "--user-launcher=unsupported",
                        "--!switch",
                        "--!init-platforms",
                        "--!init-scripts",
                        "--!init-launchers",
                        "--!install-companions",
                        "--!progress",
                        "--shared-instance",
                        "--yes",
                        "--skip-welcome"
                );
                try {
                    DefaultNInstalledRepository repo =
                            (DefaultNInstalledRepository) NWorkspaceExt.of(workspace).getInstalledRepository();
                    if ("write".equals(mode)) {
                        // Barrier: every writer boots first, then all of them
                        // hit the shared log at the same time.
                        Path readyDir = Files.createDirectories(
                                Paths.get(workspacePath).resolve("probe-ready"));
                        Files.write(readyDir.resolve("ready-" + worker + ".tmp"),
                                new byte[]{1});
                        long deadline = System.currentTimeMillis() + 120_000L;
                        while (System.currentTimeMillis() < deadline) {
                            int ready = 0;
                            for (int i = 0; i < processes; i++) {
                                if (Files.exists(readyDir.resolve("ready-" + i + ".tmp"))) {
                                    ready++;
                                }
                            }
                            if (ready == processes) {
                                break;
                            }
                            Thread.sleep(25);
                        }
                        NId id = NId.get("net.thevpc.test:concurrent-process#1.0.0").get();
                        for (int i = 0; i < records; i++) {
                            repo.addLog(NInstallLogAction.INSTALL, id, null,
                                    "probe-" + worker + "-" + i, true);
                        }
                        System.out.println("WORKER_UUID=" + workspace.uuid());
                        System.out.println("WORKER_OK " + worker);
                    } else {
                        int count = 0;
                        for (NInstallLogRecord record : repo.findLog()) {
                            if (record.getMessage() != null
                                    && record.getMessage().startsWith("probe-")) {
                                count++;
                            }
                        }
                        System.out.println("VERIFY_UUID=" + workspace.uuid());
                        System.out.println("PROBE_LOG_COUNT=" + count);
                    }
                } finally {
                    workspace.close();
                }
            } catch (Throwable ex) {
                ex.printStackTrace(System.out);
                System.exit(2);
            }
        }
    }
}
