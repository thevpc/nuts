package net.thevpc.nuts.installer.panels;

import net.thevpc.nuts.installer.model.App;
import net.thevpc.nuts.installer.model.InstallData;
import net.thevpc.nuts.installer.util.*;
import net.thevpc.nuts.boot.swing.AnsiTermPane;
import net.thevpc.nuts.boot.swing.UIHelper;
import net.thevpc.nuts.boot.swing.WizardPageBase;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class ProcessPanel extends WizardPageBase {
    AnsiTermPane ansiTermPane;
    boolean processed;
    JLabel logLabel = new JLabel();
    private boolean nl = true;
    private Path nutsJar;

    public ProcessPanel() {
        super(new BorderLayout());
        add(UIHelper.titleLabel("Please wait until the installation is complete"), BorderLayout.PAGE_START);
        ansiTermPane = new AnsiTermPane(false);
//        ansiTermPane.setEditable(false);
        add(new JScrollPane(ansiTermPane), BorderLayout.CENTER);
        add(logLabel, BorderLayout.PAGE_END);

        logLabel.setFont(new Font("Courier New", Font.PLAIN, 12));
    }


    @Override
    public void onPrevious() {
        InstallData.of(getInstallerContext()).setInstallFailed(false);
        processed = false;
    }

    @Override
    public void onShow() {
        ansiTermPane.setDarkMode(InstallData.of(getInstallerContext()).darkMode);
        getInstallerContext().getExitButton().setEnabled(false);
        getInstallerContext().getCancelButton().setEnabled(true);
        if (!processed) {
            InstallData.of(getInstallerContext()).setInstallFailed(false);
            getInstallerContext().getPreviousButton().setEnabled(false);
            getInstallerContext().getNextButton().setEnabled(false);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        processImpl();
                        processed = true;
                        if (InstallData.of(getInstallerContext()).isInstallFailed()) {

                        } else {
                            InstallData.of(getInstallerContext()).setInstallFailed(false);
                            getInstallerContext().getCancelButton().setEnabled(false);
                            getInstallerContext().getExitButton().setEnabled(false);
                        }
                    } catch (Exception ex) {
                        InstallData.of(getInstallerContext()).setInstallFailed(true);
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
        if (id.optionVerboseFile) {
            command.add("--log-file-verbose");
        }
        if (id.optionSwitch) {
            command.add("--switch");
        }
        if (id.workspace != null && id.workspace.trim().length() > 0) {
            command.add("-w");
            command.add(id.workspace.trim());
        }
        if (!id.getInstallVersion().stable) {
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

        printlnStdOutAndErr("Start installation...");
        try {
            printlnStdOutAndErr("Download " + id.getInstallVersion().location);
            nutsJar = Utils.downloadFile(id.getInstallVersion().location, "nuts-" + (id.getInstallVersion().stable ? "stable-" : "preview-"), ".jar", null);
            boolean someError = false;
            try {
                if (runNutsCommand(command.toArray(new String[0])) != 0) {
                    someError = true;
                    return;
                }
                printlnStdOut("");
                printlnStdOut("");
                printlnStdOut("---------------------------");
                int max = id.recommendedIds.size();
                int count = 0;
                if (max > 0) {
                    ArrayList<App> succeeded = new ArrayList<>();
                    ArrayList<App> failed = new ArrayList<>();
                    for (App recommendedId : id.recommendedIds) {
                        if (runCompanionInstallation(recommendedId, id)) {
                            succeeded.add(recommendedId);
                            count++;
                        } else {
                            failed.add(recommendedId);
                        }
                    }
                    if (count < id.recommendedIds.size()) {
                        someError = true;
                    }
                    if (!failed.isEmpty()) {
                        printlnStdOutAndErr("Unable to install " + failed.size() + "/" + max + " application(s) : " + failed.stream().map(x -> x.getId()).collect(Collectors.toList()));
                    }
                    if (!succeeded.isEmpty()) {
                        printlnStdOutAndErr("Succeeded to install " + succeeded.size() + "/" + max + " application(s) : " + succeeded.stream().map(x -> x.getId()).collect(Collectors.toList()));
                    }

                }
            } finally {
                if (someError) {
                    printlnStdOutAndErr("Installation cancelled with error.");
                } else {
                    printlnStdOutAndErr("Installation complete.");
                }
            }
        } catch (Exception ex) {
            printlnStdOutAndErr("Installation failed : " + ex);
            printlnStdOutAndErr("Installation failed : " + ex);
            InstallData.of(getInstallerContext()).setInstallFailed(true);
        }
    }


    private boolean runCompanionInstallation(App recommendedId, InstallData id) {
        printlnStdOutAndErr("");
        printlnStdOutAndErr("");
        printlnStdOutAndErr("---------------------------");
        printlnStdOutAndErr("Install " + recommendedId.getId() + "...\n");
        java.util.List<String> appCommand = new ArrayList<>();
        boolean someError = false;
        if (id.workspace != null && id.workspace.trim().length() > 0) {
            appCommand.add("-w");
            appCommand.add(id.workspace.trim());
        }
        if (id.optionVerbose) {
            appCommand.add("--verbose");
        }
        if (id.optionVerboseFile) {
            appCommand.add("--log-file-verbose");
        }
        appCommand.add("--theme=horizon");
        appCommand.add("install");
        appCommand.add(recommendedId.getId());
        try {
            int u = runNutsCommand(appCommand.toArray(new String[0]));
            if (u != 0) {
                someError = true;
            }
        } finally {
            if (someError) {
                printlnStdOutAndErr("Installation of " + recommendedId.getId() + " cancelled with error.");
            } else {
                printlnStdOutAndErr("Installation of " + recommendedId.getId() + " complete.");
            }
        }
        return !someError;
    }

    private void printlnStdOutAndErr(String str) {
        printStdOut(str+"\n");
        printStdErr(str+"\n");
    }

    private void printlnStdErr(String str) {
        printStdErr(str+"\n");
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

    private void printlnStdOut(String str) {
        SwingUtilities.invokeLater(() -> ansiTermPane.appendANSI(str));
        SwingUtilities.invokeLater(() -> ansiTermPane.appendANSI("\n"));
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
