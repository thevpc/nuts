package net.vpc.app.nuts.toolbox.ndi;

import java.io.BufferedWriter;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.io.IOUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class LinuxNdi implements SystemNdi {

    private NutsApplicationContext appContext;

    public LinuxNdi(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void createNutsScript(NdiScriptOptions options) throws IOException {
        if ("nuts".equals(options.getId())) {
            createBootScript(options.isForceBoot() || options.isForce(), false);
        } else {
            createBootScript(false, true);
            NutsId nutsId = appContext.getWorkspace().parser().parseId(options.getId());
            NutsDefinition fetched = null;
            if (nutsId.getVersion().isEmpty()) {
                fetched = appContext.getWorkspace().fetch().id(options.getId()).getResultDefinition();
                nutsId = fetched.getId().getSimpleNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
            if (options.isFetch()) {
                if (fetched == null) {
                    fetched = appContext.getWorkspace().fetch().id(options.getId()).getResultDefinition();
                }
                //appContext.out().printf("==%s== resolved as ==%s==\n", id,fetched.getId());
            }
            String n = nutsId.getName();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            if (!options.isForce() && exists) {
                if (!options.isSilent()) {
                    appContext.out().printf("Script already exists ==%s==\n", ff);
                }
            } else {
                String idContent = "RUN : " + nutsId;
                StringBuilder command = new StringBuilder("nuts ");
                if (options.getExecType() != null) {
                    command.append("--").append(options.getExecType().toString());
                }
                command.append(" \"").append(nutsId).append("\"");
                command.append(" \"$@\"");
                createScript(n, options.isSilent(), nutsId.toString(), idContent, command.toString());
            }
        }
    }

    public void createBootScript(boolean force, boolean silent) throws IOException {
        NutsId b = appContext.getWorkspace().config().getRunningContext().getApiId();
        NutsDefinition f = appContext.getWorkspace().fetch().id(b).setAcceptOptional(false).getResultDefinition();
        Path ff = getScriptFile("nuts");
        if (!force && Files.exists(ff)) {
            if (!silent) {
                appContext.out().printf("Script already exists ==%s==\n", ff);
            }
        } else {
            String idContent = "BOOT : " + f.getId().toString();
            createScript("nuts", silent, f.getId().getLongName(), idContent, "java -jar \"" + f.getContent().getPath() + "\" \"$@\"");
        }
    }

    @Override
    public void configurePath(boolean force, boolean silent) throws IOException {
        File bashrc = new File(System.getProperty("user.home"), ".bashrc");
        boolean found = false;
        boolean ignore = false;
        List<String> lines = new ArrayList<>();
        Path programsFolder = appContext.getProgramsFolder();
        String goodLine = "PATH='" + programsFolder + "':$PATH";
        if (bashrc.isFile()) {
            String fileContent = IOUtils.loadString(bashrc);
            String[] fileRows = fileContent.split("\n");
            for (int i = 0; i < fileRows.length; i++) {
                String row = fileRows[i];
                if (row.trim().equals("# net.vpc.app.nuts.toolbox.ndi configuration")) {
                    lines.add(row);
                    found = true;
                    i++;
                    if (i < fileRows.length) {
                        if (fileRows[i].trim().equals(goodLine)) {
                            ignore = true;
                        } else {
                            lines.add(goodLine);
                        }
                    } else {
                        lines.add(goodLine);
                    }
                    i++;
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (ignore && force) {
            ignore = false;
        }
        if (!ignore) {
            if (!found) {
                lines.add("# net.vpc.app.nuts.toolbox.ndi configuration");
                lines.add(goodLine);
            }
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
                sb.append("\n");
            }
            if (!silent) {
                appContext.out().printf("Updating ==%s== file to point to workspace ==%s==\n", "~/.bashrc", appContext.getWorkspace().config().getWorkspaceLocation());
                appContext.out().printf("@@ATTENTION@@ You may need to re-run terminal or issue \\\"==%s==\\\" in your current terminal for new environment to take effect.\n", ". ~/.bashrc");
                while (true) {
                    if (appContext.getWorkspace().config().getOptions().isYes()) {
                        break;
                    }
                    if (appContext.getWorkspace().config().getOptions().isNo()) {
                        return;
                    }
                    String r = appContext.getTerminal().ask(
                            NutsQuestion.forString("Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.")
                    );
                    if ("ok".equalsIgnoreCase(r)) {
                        break;
                    }
                    if ("why".equalsIgnoreCase(r)) {
                        appContext.out().printf("\\\"==%s==\\\" is a special file in your home that is invoked upon each interactive terminal launch.\n", ".bashrc");
                        appContext.out().print("It helps configuring environment variables. ==Nuts== make usage of such facility to update your **PATH** env variable\n");
                        appContext.out().print("to point to current ==Nuts== workspace, so that when you call a ==Nuts== command it will be resolved correctly...\n");
                        appContext.out().printf("However updating \\\"==%s==\\\" does not affect the running process/terminal. So you have basicly two choices :\n", ".bashrc");
                        appContext.out().print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)\n");
                        appContext.out().printf(" - Or to run by your self the \\\"==%s==\\\" script (dont forget the leading dot)\n", ". ~/.bashrc");
                    } else if ("cancel".equalsIgnoreCase(r)) {
                        return;
                    } else {
                        appContext.out().print(" @@Sorry...@@ but you need to type 'ok', 'why' or 'cancel' !\n");
                    }
                }
            }
            IOUtils.saveString(sb.toString(), bashrc);
        }
    }

    public Path getScriptFile(String name) {
        Path bin = appContext.getProgramsFolder();
        return bin.resolve(name);
    }

    public Path createScript(String name, boolean silent, String desc, String idContent, String content) throws IOException {
        Path script = getScriptFile(name);
        if (script.getParent() != null) {
            if (!Files.exists(script.getParent())) {
                if (!silent) {
                    appContext.out().printf("Creating folder ==%s==\n", script.getParent());
                }
                Files.createDirectories(script.getParent());
            }
        }
        if (!silent) {
            if (Files.exists(script)) {
                appContext.out().printf("Install (with override) script ==%s== for ==%s== at ==%s==\n", script.getFileName(), desc, script);
            } else {
                appContext.out().printf("Install script ==%s== for ==%s== at ==%s==\n", script.getFileName(), desc, script);
            }
        }

        try (BufferedWriter w = Files.newBufferedWriter(script)) {
            w.write("#!/bin/sh\n");
            w.write("# THIS FILE IS GENERATED BY\n");
            w.write("#      net.vpc.app.nuts.toolbox.ndi\n");
            w.write("# DO NOT EDIT IT MANUALLY\n");
            w.write("#\n");
            w.write("# START-ID\n");
            for (String s : idContent.split("\n")) {
                w.write("# " + s + "\n");
            }
            w.write("# END-ID\n");
            w.write("#\n");
            w.write("# START-COMMAND\n");
            w.write("\n");
            w.write(content);
            if (!content.endsWith("\n") && !content.endsWith("\r")) {
                w.write("\n");
            }
            w.write("\n");
            w.write("# END-COMMAND\n");
            w.write("\n");
        }
        NdiUtils.setExecutable(script);
        return script;
    }
}
