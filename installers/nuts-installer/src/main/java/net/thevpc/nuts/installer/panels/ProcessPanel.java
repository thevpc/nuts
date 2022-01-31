package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.App;
import net.thevpc.nuts.installer.InstallData;
import net.thevpc.nuts.installer.util.*;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ProcessPanel extends AbstractInstallPanel {
    AnsiTermPane ansiTermPane;
    boolean processed;
    JLabel logLabel = new JLabel();
    private boolean nl = true;
    private Path nutsJar;

    public ProcessPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please wait unless the installation is complete"), BorderLayout.PAGE_START);
        ansiTermPane = new AnsiTermPane();
//        ansiTermPane.setEditable(false);
        add(new JScrollPane(ansiTermPane), BorderLayout.CENTER);
        add(logLabel, BorderLayout.PAGE_END);

        logLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
    }

    @Override
    public void onPrevious() {
        getInstallerContext().setInstallFailed(false);
        processed = false;
    }

    @Override
    public void onShow() {
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        if (!processed) {
            getInstallerContext().setInstallFailed(false);
            getInstallerContext().getPreviousButton().setEnabled(false);
            getInstallerContext().getNextButton().setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        processImpl();
                        processed = true;
                        getInstallerContext().setInstallFailed(false);
                        getInstallerContext().getCancelButton().setEnabled(false);
                        getInstallerContext().getExitButton().setEnabled(false);
                    } catch (Exception ex) {
                        getInstallerContext().setInstallFailed(true);
                        ex.printStackTrace();
                    } finally {
                        getInstallerContext().getPreviousButton().setEnabled(true);
                        getInstallerContext().getNextButton().setEnabled(getInstallerContext().hasNext(getPageIndex()));
                    }
                }
            }).start();
        }
    }

    private void processImpl() throws Exception {
        ansiTermPane.clearScreen();
        logLabel.setText("\n ");
        java.util.List<String> command = new ArrayList<>();
        InstallData id = InstallData.of(getInstallerContext());
        if (id.optionZ) {
            command.add("-Z");
        } else {
            command.add("-zN");
        }
        if (id.optionS) {
            command.add("-S");
        }
        if (id.optionk) {
            command.add("-k");
        }
        if (id.optionVerbose) {
            command.add("--verbose");
        }
        if (id.optionSwitch) {
            command.add("--switch");
        }
        if (id.workspace != null && id.workspace.trim().length() > 0) {
            command.add("-w");
            command.add(id.workspace.trim());
        }
        if (!id.installVersion.stable) {
            command.add("-r=preview");
        }
        Set<String> extraRepos = id.recommendedIds.stream().map(App::getRepo)
                .filter(x -> x != null && x.length() > 0)
                .collect(Collectors.toSet());
        //add other repos
        for (String extraRepo : extraRepos) {
            command.add("-r=" + extraRepo);
        }
        command.add("--theme=horizon");

        if (id.otherOptions != null && !id.otherOptions.isEmpty()) {
            command.addAll(id.otherOptions);
        }

        printStdOut("Start installation...\n");
        printStdOut("Download " + id.installVersion.location + "\n");
        nutsJar = Utils.downloadFile(id.installVersion.location, "nuts-" + (id.installVersion.stable ? "stable-" : "preview-"), ".jar", null);
        boolean someError=false;
        try {
            if (runNutsCommand(command.toArray(new String[0])) != 0) {
                someError=true;
                return;
            }

            if (!id.recommendedIds.isEmpty()) {
                for (App recommendedId : id.recommendedIds) {
                    printStdOut("Install " + recommendedId.getId() + "...\n");
                    if (runNutsCommand("install", recommendedId.getId()) != 0) {
                        someError=true;
                        return;
                    }
                }
            }
        }finally {
            if(someError) {
                printStdOut("Installation cancelled.");
            }else{
                printStdOut("Installation complete.");
            }
        }
    }


    private void printStdErr(String str) {
        StringBuilder sb = new StringBuilder(logLabel.getText());
        for (char c : str.toCharArray()) {
            if (c == '\n' || c == '\r') {
                nl = true;
            } else {
                if (nl) {
                    sb.delete(0, sb.length());
                    nl = false;
                }
                sb.append(c);
            }
        }
        logLabel.setText(sb.toString());
    }

    private void printStdOut(String str) {
        SwingUtilities.invokeLater(() -> ansiTermPane.appendANSI(str));
    }

    private String getJavaCommand() {
        InstallData id = InstallData.of(getInstallerContext());
        if (Utils.isBlank(id.java)) {
            return "java";
        }
        return id.java;
    }

    private int runNutsCommand(String... command) {
        java.util.List<String> newCmd = new ArrayList<>();
        newCmd.add(getJavaCommand());
        newCmd.add("-jar");
        newCmd.add(nutsJar.toString());
        newCmd.add("-y");
        newCmd.add("-P=%n");
        newCmd.add("--color");
        newCmd.addAll(Arrays.asList(command));
        return runCommand(newCmd.toArray(new String[0]));
    }

    private int runCommand(String[] command) {
        InstallData id = InstallData.of(getInstallerContext());
        printStdOut(String.join(" ", command) + "\n");
        ProcessBuilder sb = new ProcessBuilder();
        sb.command(Arrays.asList(command));
        Process p = null;
        try {
            p = sb.start();
            StreamGobbler errorGobbler = new StreamGobbler(p.getErrorStream(), this::printStdErr);
            StreamGobbler outputGobbler = new StreamGobbler(p.getInputStream(), this::printStdOut);
            errorGobbler.start();
            outputGobbler.start();
            int e = p.waitFor();
            printStdErr("\nProcess terminated with exit code " + e);
            return e;
        } catch (Exception e) {
            printStdErr("\n" + e);
            return -1;
        }
    }

}
