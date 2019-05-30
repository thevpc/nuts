package net.vpc.app.nuts.toolbox.ndi;

import java.io.BufferedWriter;
import net.vpc.app.nuts.*;
import net.vpc.common.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LinuxNdi implements SystemNdi {

    private NutsApplicationContext appContext;

    public LinuxNdi(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public void createNutsScript(NdiScriptOptions options) throws IOException {
        if ("nuts".equals(options.getId())) {
            createBootScript(options.isForceBoot() || options.isForce(), true);
        } else {
            createBootScript(false, false);
            NutsId nutsId = appContext.getWorkspace().parser().parseId(options.getId());
            NutsDefinition fetched = null;
            if (nutsId.getVersion().isBlank()) {
                fetched = appContext.getWorkspace().search().id(options.getId()).latest().getResultDefinitions().required();
                nutsId = fetched.getId().getSimpleNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
//            if (options.isFetch()) {
//                if (fetched == null) {
//                    fetched = appContext.getWorkspace().fetch().id(options.getId()).getResultDefinition();
//                }
//                //appContext.out().printf("==%s== resolved as ==%s==\n", id,fetched.getId());
//            }
            String n = nutsId.getName();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            if (!options.isForce() && exists) {
                if (options.isTrace()) {
                    appContext.out().printf("Script already exists ==%s==%n", ff);
                }
            } else {
                final NutsId fnutsId = nutsId;
                createScript(n, options.isTrace(), nutsId.toString(),
                        x -> {
                            switch (x) {
                                case "NUTS_ID":
                                    return "RUN : " + fnutsId;
                                case "BODY": {
                                    StringBuilder command = new StringBuilder();
                                    command.append("nuts ");
                                    if (options.getExecType() != null) {
                                        command.append("--").append(options.getExecType().name().toLowerCase());
                                    }
                                    command.append(" \"").append(fnutsId).append("\"");
                                    command.append(" \"$@\"");
                                    return command.toString();
                                }
                            }
                            return null;
                        }
                );
            }
        }
    }

    public void createBootScript(boolean force, boolean trace) throws IOException {
        NutsId b = appContext.getWorkspace().config().getContext(NutsBootContextType.RUNTIME).getApiId();
        NutsDefinition f = appContext.getWorkspace().search().id(b).setOptional(false).latest().getResultDefinitions().required();
        Path ff = getScriptFile("nuts");
        if (!force && Files.exists(ff)) {
            if (trace) {
                appContext.out().printf("Script already exists ==%s==%n", ff);
            }
        } else {
            createScript("nuts", trace, f.getId().getLongName(),
                    x -> {
                        switch (x) {
                            case "NUTS_ID":
                                return "BOOT : " + f.getId().toString();
                            case "BODY":
                                return NdiUtils.generateScriptAsString("/net/vpc/app/nuts/toolbox/template_nuts_linux.text",
                                        ss -> {
                                            switch (ss) {
                                                case "NUTS_JAR":
                                                    return f.getPath().toString();
                                            }
                                            return null;
                                        }
                                );
                        }
                        return null;
                    }
            );
        }
        Path ff2 = appContext.getWorkspace().config().getWorkspaceLocation().resolve("nuts");
        if (!force && Files.exists(ff2)) {
            if (trace) {
                appContext.out().printf("Script already exists ==%s==%n", ff2);
            }
        } else {
            try (BufferedWriter w = Files.newBufferedWriter(ff2)) {
                NdiUtils.generateScript("/net/vpc/app/nuts/toolbox/template_body_linux.text", w, x -> {
                    switch (x) {
                        case "NUTS_ID":
                            return "BOOT : " + f.getId().toString();
                        case "BODY": {
                            String s = longuestCommonParent(ff.toString(), ff2.toString());
                            if (s.length() > 0) {
                                return ff.toString().substring(s.length());
                            } else {
                                return ff.toString();
                            }
                        }
                    }
                    return null;
                });
            }
            NdiUtils.setExecutable(ff2);
        }
    }

    public String longuestCommonParent(String path1, String path2) {
        int latestSlash = -1;
        final int len = Math.min(path1.length(), path2.length());
        for (int i = 0; i < len; i++) {
            if (path1.charAt(i) != path2.charAt(i)) {
                break;
            } else if (path1.charAt(i) == '/') {
                latestSlash = i;
            }
        }
        if (latestSlash <= 0) {
            return "";
        }
        return path1.substring(0, latestSlash + 1);
    }

    @Override
    public void configurePath(boolean force, boolean trace) throws IOException {
        File bashrc = new File(System.getProperty("user.home"), ".bashrc");
        boolean found = false;
        boolean updatedBashrc = false;
        boolean updatedNdirc = false;
        List<String> lines = new ArrayList<>();
        Path programsFolder = appContext.getProgramsFolder();
        String goodLine = "source ~/.nuts-ndirc";
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
                        if (!fileRows[i].trim().equals(goodLine)) {
                            updatedBashrc = true;
                        }
                    }
                    lines.add(goodLine);
                    i++;
                    for (; i < fileRows.length; i++) {
                        lines.add(fileRows[i]);
                    }
                } else {
                    lines.add(row);
                }
            }
        }
        if (!found) {
            lines.add("# net.vpc.app.nuts.toolbox.ndi configuration");
            lines.add(goodLine);
            updatedBashrc = true;
        }
        if (force || updatedBashrc) {
            IOUtils.saveString(lines.stream().collect(Collectors.joining("\n")), bashrc);
        }
        File nutsndirc = new File(System.getProperty("user.home"), ".nuts-ndirc");
        StringBuilder goodNdiRc = new StringBuilder();
        goodNdiRc.append("# This File is generated by nuts ndi companion tool.\n");
        goodNdiRc.append("# Do not edit it manually. All changes will be lost when ndi runs again\n");
        goodNdiRc.append("# This file aims to prepare bash environment against current nuts\n");
        goodNdiRc.append("# workspace installation.\n");
        goodNdiRc.append("#\n");
        goodNdiRc.append("NUTS_VERSION='").append(appContext.getWorkspace().config().getApiId().getVersion().getValue()).append("'\n");
        goodNdiRc.append("NUTS_JAR='").append(appContext.getWorkspace().search().id(appContext.getWorkspace().config().getApiId()).getResultFiles().required()).append("'\n");
        goodNdiRc.append("NUTS_WORKSPACE='").append(appContext.getWorkspace().config().getWorkspaceLocation().toString()).append("'\n");
        goodNdiRc.append("PATH='").append(programsFolder).append("':$PATH\n");
        goodNdiRc.append("export PATH NUTS_VERSION NUTS_JAR NUTS_WORKSPACE \n");

        String fileContent = (nutsndirc.isFile()) ? IOUtils.loadString(nutsndirc) : "";
        if (!fileContent.trim().equals(goodNdiRc.toString().trim())) {
            updatedNdirc = true;
        }
        if (force || updatedNdirc) {
            IOUtils.saveString(goodNdiRc.toString(), nutsndirc);
        }
        if ((force || updatedBashrc || updatedNdirc) && trace) {
            if (updatedNdirc) {
                appContext.out().printf("Updating ==%s== file to point to workspace ==%s==%n", "~/.nuts-ndirc", appContext.getWorkspace().config().getWorkspaceLocation());
            } else if (force) {
                appContext.out().printf("Force updating ==%s== file to point to workspace ==%s==%n", "~/.nuts-ndirc", appContext.getWorkspace().config().getWorkspaceLocation());
            }
            if (updatedBashrc) {
                appContext.out().printf("Updating ==%s== file to point to workspace ==%s==%n", "~/.bashrc", appContext.getWorkspace().config().getWorkspaceLocation());
            } else {
                appContext.out().printf("Force updating ==%s== file to point to ==%s==%n", "~/.bashrc", "~/.nuts-ndirc");
            }
            appContext.out().printf("@@ATTENTION@@ You may need to re-run terminal or issue \\\"==%s==\\\" in your current terminal for new environment to take effect.%n", ". ~/.bashrc");
            if (updatedNdirc || updatedBashrc) {
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
                        appContext.out().printf("\\\"==%s==\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", ".bashrc");
                        appContext.out().print("It helps configuring environment variables. ==Nuts== make usage of such facility to update your **PATH** env variable\n");
                        appContext.out().print("to point to current ==Nuts== workspace, so that when you call a ==Nuts== command it will be resolved correctly...\n");
                        appContext.out().printf("However updating \\\"==%s==\\\" does not affect the running process/terminal. So you have basically two choices :%n", ".bashrc");
                        appContext.out().print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                        appContext.out().printf(" - Or to run by your self the \\\"==%s==\\\" script (don\\'t forget the leading dot)%n", ". ~/.bashrc");
                    } else if ("cancel".equalsIgnoreCase(r)) {
                        return;
                    } else {
                        appContext.out().print(" @@Sorry...@@ but you need to type 'ok', 'why' or 'cancel' !%n");
                    }
                }
            }
        }
    }

    public Path getScriptFile(String name) {
        Path bin = appContext.getProgramsFolder();
        return bin.resolve(name);
    }

    public Path createScript(String name, boolean trace, String desc, Function<String, String> mapper) throws IOException {
        Path script = getScriptFile(name);
        if (script.getParent() != null) {
            if (!Files.exists(script.getParent())) {
                if (trace) {
                    appContext.out().printf("Creating folder ==%s==%n", script.getParent());
                }
                Files.createDirectories(script.getParent());
            }
        }
        if (trace) {
            appContext.out().printf("Install %s script ==%s== for ==%s== at ==%s==%n", Files.exists(script) ? "(with override)" : "", script.getFileName(), desc, script);
        }

        try (BufferedWriter w = Files.newBufferedWriter(script)) {
            NdiUtils.generateScript("/net/vpc/app/nuts/toolbox/template_body_linux.text", w, mapper);
        }
        NdiUtils.setExecutable(script);
        return script;
    }

}
