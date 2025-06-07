/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;


import net.thevpc.nuts.format.NVersionFormat;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessBuilder2;
import net.thevpc.nuts.util.NAssert;
import org.junit.jupiter.api.*;

import java.io.*;
import java.util.List;


/**
 * @author thevpc
 */
public class ExecTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("--verbose");
    }

    @Test
    public void execURL() {
        TestUtils.println(NVersionFormat.of());
        NSearchCmd q = NSearchCmd.of()
                .setId("net.thevpc.hl:hadra-build-tool#0.1.0")
//                .setRepositoryFilter("maven-central")
//                .setRepositoryFilter(NRepositoryFilters.of().byName("maven"))
//                .setFetchStrategy(NFetchStrategy.REMOTE)
                .setLatest(true);
        NOut.println(q.getResultQueryPlan());
        List<NId> nutsIds = q
                .getResultIds()
                .toList();
        NAssert.requireNonEmpty(nutsIds, "not found hadra-build-tool");
        TestUtils.println(nutsIds);
        List<NDependencies> allDeps = NSearchCmd.of().addId("net.thevpc.hl:hl#0.1.0")
//                .setDependencies(true)
                .getResultDependencies().toList();
        for (NDependencies ds : allDeps) {
            for (NDependency d : ds.transitiveWithSource()) {
                TestUtils.println(d);
            }
        }
        TestUtils.println("=============");
        for (NDependencies ds : allDeps) {
            for (NDependencyTreeNode d : ds.transitiveNodes()) {
                printlnNode(d, "");
            }
        }
        if(false) {
            String result = NExecCmd.of()
                    .addWorkspaceOptions(NWorkspaceOptionsBuilder.of()
                            .setBot(true)
                            .setWorkspace(NWorkspace.of().getWorkspaceLocation().resolve("temp-ws").toString())
                            .build()
                    )
                    //.addExecutorOption("--main-class=Version")
                    .addCommand(
                            "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar",
                            "--version"
                    ).grabAll().failFast().getGrabbedOutString();
            TestUtils.println("Result:");
            TestUtils.println(result);
        }
        //Assertions.assertFalse(result.contains("[0m"),"Message should not contain terminal format");
    }

    private void printlnNode(NDependencyTreeNode d, String s) {
        TestUtils.println(s + d.getDependency());
        for (NDependencyTreeNode child : d.getChildren()) {
            printlnNode(child, "  ");
        }
    }


    @Test
    public void testEmbeddedInfo() {
        TestUtils.println(NVersionFormat.of());
        String result = NExecCmd.of()
                .addCommand("info")
                .getGrabbedAllString();
        NOut.println(result);
        Assertions.assertFalse(result.contains("[0m"), "Message should not contain terminal format");
    }

    //disabled, unless we find a good executable example jar
    //@Test
    public void execURL2() {
        TestUtils.println(NVersionFormat.of());
        String result = NExecCmd.of()
                //there are three classes and no main-class, so need to specify the one
                .addExecutorOption("--main-class=Version")
//                .addExecutorOption("--main-class=junit.runner.Version")
                //get the command
                .addCommand(
//                        "https://search.maven.org/remotecontent?filepath=junit/junit/4.12/junit-4.12.jar"
                        "https://search.maven.org/remotecontent?filepath=net/java/sezpoz/demo/app/1.6/app-1.6.jar"
//                "https://search.maven.org/remotecontent?filepath=net/thevpc/hl/hl/0.1.0/hl-0.1.0.jar",
//                "--version"
                ).grabAll().failFast().getGrabbedOutString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"), "Message should not contain terminal format");
    }

    //@Test
    public void testNtf() {
        TestUtils.println(NVersionFormat.of());
        String result = NExecCmd.of()
                //.addExecutorOption()
                .addCommand(NConstants.Ids.NSH, "-c", "ls")
                .grabAll().failFast().getGrabbedOutString();
        TestUtils.println("Result:");
        TestUtils.println(result);
        Assertions.assertFalse(result.contains("[0m"), "Message should not contain terminal format");
    }


    //@Test
    public void testCallSpecialId() {
        TestUtils.println(NVersionFormat.of());
        String result = NExecCmd.of()
                .addExecutorOptions("--bot")
                //.setExecutionType(NExecutionType.EMBEDDED)
                .addCommand("com.cts.nuts.enterprise.postgres:pgcli")
                .addCommand("list", "-i")
                .getGrabbedAllString();
        NOut.println(result);
        Assertions.assertFalse(result.contains("[0m"), "Message should not contain terminal format");
    }

    private void runUsingRuntime(String... args) {
        NOut.println("================= runUsingRuntime");
        Runtime rt = Runtime.getRuntime();
        Process p = null;
        try {
            p = rt.exec(args);
            MemConsumer out = new MemConsumer(p.getInputStream()).run();
            MemConsumer err = new MemConsumer(p.getErrorStream()).run();
            p.getOutputStream().close();
            p.waitFor();
            System.out.println("==================OUT");
            System.out.println(out.sb2);
            System.out.println("==================ERR");
            System.out.println(err.sb2);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void runUsingNuts(String... args) {
        NOut.println("================= runUsingNuts");
        NExecCmd e = NExecCmd.of(args)
                .setIn(NExecInput.ofNull())
                .setErr(NExecOutput.ofGrabMem())
                .setOut(NExecOutput.ofGrabMem())
                .run();
        System.out.println(e.getResultCode());
        System.out.println("============= OUT");
        System.out.println(e.getGrabbedOutString());
        System.out.println("============= ERR");
        System.out.println(e.getGrabbedErrString());
    }

    private void runUsingProcessBuilder2(String... args) {
        NOut.println("================= runUsingProcessBuilder2");
        ProcessBuilder2 e = new ProcessBuilder2();
        e.addCommand(args);
        e.setIn(NExecInput.ofBytes(new byte[0]));
        e.setOut(NExecOutput.ofGrabMem());
        e.setErr(NExecOutput.ofGrabMem());
        try {
            e.start();
            e.waitFor();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        System.out.println(e.getResult());
        System.out.println("============= OUT");
        System.out.println(e.getOut().getResult().readString());
        System.out.println("============= ERR");
        System.out.println(e.getErr().getResult().readString());
    }

    @Test
    public void testExecOnWindows1() {
        TestUtils.println(NVersionFormat.of());
        if (NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS) {
            runUsingProcessBuilder2("cmd.exe", "dir", ".");
        }
    }


    @Test
    public void testExecOnWindows2() {
        TestUtils.println(NVersionFormat.of());
        if (NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS) {
            runUsingRuntime("cmd.exe", "dir", ".");
        }
    }

    @Test
    public void testExecOnWindows3() {
        TestUtils.println(NVersionFormat.of());
        if (NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS) {
            String[] args = {
                    "powershell.exe", "-Command",
                    "Get-WmiObject Win32_Process | ForEach-Object { $o = $_.GetOwner(); $user = if ($o) { $o.User } else { 'N/A' }; $mem = Get-WmiObject Win32_ComputerSystem; $state = if ($_.ExecutionState -eq 0) { 'Running' } elseif ($_.ExecutionState -eq 2) { 'Sleeping' } else { 'Suspended' }; $start = if ($_.CreationDate) { $_.CreationDate.Substring(0, 12) } else { 'N/A' }; New-Object PSObject -Property @{ USER=$user; PID=$_.ProcessId; CPU=([math]::Round(($_.KernelModeTime + $_.UserModeTime)/1e7, 2)); MEM=([math]::Round($_.WorkingSetSize / $mem.TotalPhysicalMemory * 100, 2)); VSZ=[int]($_.VirtualSize / 1KB); RSS=[int]($_.WorkingSetSize / 1KB); TTY='N/A'; STAT=$state; START=$start; TIME=([math]::Round(($_.KernelModeTime + $_.UserModeTime)/1e7, 2)); COMMAND=$_.CommandLine } }"
//                                        "$mem=(Get-WmiObject Win32_ComputerSystem).TotalPhysicalMemory; Get-WmiObject Win32_Process|ForEach-Object{ $o=$_.GetOwner();$user=if($o){$o.User}else{'N/A'};$state=if($_.ExecutionState -eq 0){'Running'}elseif($_.ExecutionState -eq 2){'Sleeping'}else{'Suspended'};$start=if($_.CreationDate){$_.CreationDate.Substring(0,12)}else{'N/A'};New-Object PSObject -Property @{USER=$user;PID=$_.ProcessId;CPU=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);MEM=[math]::Round($_.WorkingSetSize/$mem*100,2);VSZ=[int]($_.VirtualSize/1KB);RSS=[int]($_.WorkingSetSize/1KB);TTY='N/A';STAT=$state;START=$start;TIME=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);COMMAND=$_.CommandLine}}|ConvertTo-Csv -NoTypeInformation  | Out-String -Width 1000"
            };
//            runUsingRuntime(args);
//            runUsingProcessBuilder2(args);
            runUsingNuts(args);
        }
    }

    @Test
    public void testExecOnWindows4() {
        TestUtils.println(NVersionFormat.of());
        if (NWorkspace.of().getOsFamily() == NOsFamily.WINDOWS) {
            String[] args = {
                    "powershell.exe", "-Command",
                    "$mem=(Get-WmiObject Win32_ComputerSystem).TotalPhysicalMemory; Get-WmiObject Win32_Process|ForEach-Object{ $o=$_.GetOwner();$user=if($o){$o.User}else{'N/A'};$state=if($_.ExecutionState -eq 0){'Running'}elseif($_.ExecutionState -eq 2){'Sleeping'}else{'Suspended'};$start=if($_.CreationDate){$_.CreationDate.Substring(0,12)}else{'N/A'};New-Object PSObject -Property @{USER=$user;PID=$_.ProcessId;CPU=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);MEM=[math]::Round($_.WorkingSetSize/$mem*100,2);VSZ=[int]($_.VirtualSize/1KB);RSS=[int]($_.WorkingSetSize/1KB);TTY='N/A';STAT=$state;START=$start;TIME=[math]::Round(($_.KernelModeTime+$_.UserModeTime)/1e7,2);COMMAND=$_.CommandLine}}|ConvertTo-Csv -NoTypeInformation  | Out-String -Width 1000"
            };
            runUsingRuntime(args);
            runUsingProcessBuilder2(args);
            runUsingNuts(args);
        }
    }

    private static class MemConsumer {
        boolean finished = false;
        ByteArrayOutputStream sb2 = new ByteArrayOutputStream();
        InputStream is;

        public MemConsumer(InputStream is) {
            this.is = is;
        }

        MemConsumer run() {
            new Thread(() -> {
                try (Reader r = new InputStreamReader(is)) {
                    while (true) {
                        int read = r.read();
                        if (read < 0) {
                            break;
                        }
                        sb2.write(read);
                    }
                } catch (Exception ex) {
                    //
                } finally {
                    finished = true;
                }
            }).start();
            return this;
        }
    }

}
