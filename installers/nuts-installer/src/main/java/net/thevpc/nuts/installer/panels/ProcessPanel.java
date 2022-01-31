package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.InstallData;
import net.thevpc.nuts.installer.util.*;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ProcessPanel extends AbstractInstallPanel {
    AnsiTermPane ansiTermPane;
    boolean processed;
    JLabel logLabel = new JLabel();
    private boolean nl = true;

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
        if (id.otherOptions != null && !id.otherOptions.isEmpty()) {
            command.addAll(id.otherOptions);
        }
        runNutsCommand(command.toArray(new String[0]));

        if(!id.recommendedIds.isEmpty()) {
            java.util.List<String> install = new ArrayList<>(id.recommendedIds);
            runNutsCommand(install.toArray(new String[0]));
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
        return "java";
    }

    private Path getNutsJar() {
        return Paths.get("/home/vpc/.m2/repository/net/thevpc/nuts/nuts/0.8.3/nuts-0.8.3.jar");
    }

    private void runNutsCommand(String... command) {
        InstallData id = InstallData.of(getInstallerContext());
        java.util.List<String> newCmd = new ArrayList<>();
        newCmd.add(getJavaCommand());
        newCmd.add("-jar");
        newCmd.add(Utils.downloadFile(id.installVersion.location,".jar", null).toString());
        newCmd.add("-y");
        newCmd.add("-P=%n");
        newCmd.add("--color");
        if(!id.installVersion.stable){
            newCmd.add("-r=dev");
        }
        newCmd.addAll(Arrays.asList(command));
        runCommand(newCmd.toArray(new String[0]));
    }

    private void runCommand(String[] command) {
        printStdOut("start process...\n");
        printStdOut(String.join(" ",command)+"\n");
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
            printStdErr("\nprocess terminated with exit code " + e);
        } catch (Exception e) {
            printStdErr("\n" + e);
        }
    }

}
