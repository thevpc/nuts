package net.vpc.app.nuts.toolbox.ndi;

import java.io.BufferedWriter;
import net.vpc.app.nuts.*;
import net.vpc.common.io.IOUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LinuxNdi implements SystemNdi {

    private NutsApplicationContext context;

    public LinuxNdi(NutsApplicationContext appContext) {
        this.context = appContext;
    }

    @Override
    public NdiScriptnfo[] createNutsScript(NdiScriptOptions options) throws IOException {
        NutsId nid = context.getWorkspace().id().parse(options.getId());
        if ("nuts".equals(nid.getShortName()) || "net.vpc.app.nuts:nuts".equals(nid.getShortName())) {
            return createBootScript(options.isForceBoot() || options.getSession().isYes(), options.getSession().isTrace());
        } else {
            List<NdiScriptnfo> r = new ArrayList<>();
            r.addAll(Arrays.asList(createBootScript(false, false)));
            NutsDefinition fetched = null;
            if (nid.getVersion().isBlank()) {
                fetched = context.getWorkspace().search()
                        .session(context.getSession().copy().trace(false))
                        .id(options.getId()).latest().getResultDefinitions().required();
                nid = fetched.getId().getShortNameId();
                //nutsId=fetched.getId().getLongNameId();
            }
//            if (options.isFetch()) {
//                if (fetched == null) {
//                    fetched = appContext.getWorkspace().fetch().parse(options.getId()).getResultDefinition();
//                }
//                //appContext.out().printf("==%s== resolved as ==%s==\n", parse,fetched.getId());
//            }
            String n = nid.getName();
            Path ff = getScriptFile(n);
            boolean exists = Files.exists(ff);
            if (!options.getSession().isYes() && exists) {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("Script already exists ==%s==%n", ff);
                }
            } else {
                final NutsId fnutsId = nid;
                NdiScriptnfo p = createScript(n, fnutsId, options.getSession().isTrace(), nid.toString(),
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
                r.add(p);
            }
            return r.toArray(new NdiScriptnfo[0]);
        }
    }

    @Override
    public void removeNutsScript(String id, NutsSession session) throws IOException {
        NutsId nid = context.getWorkspace().id().parse(id);
        Path f = getScriptFile(nid.getName());
        if (Files.isRegularFile(f)) {
            if (session.terminal().ask().forBoolean("Tool ==%s== will be removed. Confirm", f.toString())
                    .defaultValue(true)
                    .getBooleanValue()) {
                Files.delete(f);
                if (session.isPlainTrace()) {
                    session.out().printf("Tool ==%s== removed.%n", f.toString());
                }
            }
        }
    }

    public NdiScriptnfo[] createBootScript(boolean force, boolean trace) throws IOException {
        NutsId b = context.getWorkspace().config().getApiId();
        NutsDefinition f = context.getWorkspace().search()
                .session(context.getSession().copy().trace(false))
                .id(b).setOptional(false).latest().content().getResultDefinitions().required();
        Path ff = getScriptFile("nuts");
        List<NdiScriptnfo> all = new ArrayList<>();
        if (!force && Files.exists(ff)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.session().out().printf("Script already exists ==%s==%n", ff);
            }
        } else {
            all.add(
                    createScript("nuts", b, trace, f.getId().getLongName(),
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
                    ));
        }
        Path ff2 = context.getWorkspace().config().getWorkspaceLocation().resolve("nuts");
        if (!force && Files.exists(ff2)) {
            if (trace && context.getSession().isPlainTrace()) {
                context.session().out().printf("Script already exists ==%s==%n", ff2);
            }
        } else {
            if (trace && context.getSession().isPlainTrace()) {
                if (force) {
                    context.session().out().printf("Force update script ==%s== %n", ff2.toString());
                } else {
                    context.session().out().printf("Update script ==%s== %n", ff2.toString());
                }
            }
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
            all.add(new NdiScriptnfo("nuts", b, ff2));
        }
        return all.toArray(new NdiScriptnfo[0]);
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
    public void configurePath(NutsSession session) throws IOException {
        File bashrc = new File(System.getProperty("user.home"), ".bashrc");
        boolean found = false;
        boolean updatedBashrc = false;
        boolean updatedNdirc = false;
        List<String> lines = new ArrayList<>();
        Path appsFolder = context.getAppsFolder();
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
        if (session.isYes() || updatedBashrc) {
            IOUtils.saveString(lines.stream().collect(Collectors.joining("\n")) + "\n", bashrc);
        }
        File nutsndirc = new File(System.getProperty("user.home"), ".nuts-ndirc");
        StringBuilder goodNdiRc = new StringBuilder();
        goodNdiRc.append("# This File is generated by nuts ndi companion tool.\n");
        goodNdiRc.append("# Do not edit it manually. All changes will be lost when ndi runs again\n");
        goodNdiRc.append("# This file aims to prepare bash environment against current nuts\n");
        goodNdiRc.append("# workspace installation.\n");
        goodNdiRc.append("#\n");
        goodNdiRc.append("NUTS_VERSION='").append(context.getWorkspace().config().getApiVersion()).append("'\n");
        goodNdiRc.append("NUTS_JAR='").append(context.getWorkspace().search()
                .session(context.getSession().copy().trace(false))
                .id(context.getWorkspace().config().getApiId()).getResultPaths().required()).append("'\n");
        goodNdiRc.append("NUTS_WORKSPACE='").append(context.getWorkspace().config().getWorkspaceLocation().toString()).append("'\n");
        goodNdiRc.append("[[ \":$PATH:\" != *\":" + appsFolder + ":\"* ]] && PATH=\"" + appsFolder + ":${PATH}\"\n");
        goodNdiRc.append("export PATH NUTS_VERSION NUTS_JAR NUTS_WORKSPACE \n");

        String fileContent = (nutsndirc.isFile()) ? IOUtils.loadString(nutsndirc) : "";
        if (!fileContent.trim().equals(goodNdiRc.toString().trim())) {
            updatedNdirc = true;
        }
        if (session.isYes() || updatedNdirc) {
            IOUtils.saveString(goodNdiRc.toString(), nutsndirc);
        }
        if ((session.isYes() || updatedBashrc || updatedNdirc) && session.isTrace()) {
            if (session.isYes()) {
                if (context.getSession().isPlainTrace()) {
                    context.session().out().printf("Force updating ==%s== and ==%s== files to point to workspace ==%s==%n", "~/.nuts-ndirc", "~/.bashrc", context.getWorkspace().config().getWorkspaceLocation());
                }
            } else {
                if (context.getSession().isPlainTrace()) {
                    if (updatedNdirc && updatedBashrc) {
                        context.session().out().printf("Updating ==%s== and ==%s== files to point to workspace ==%s==%n", "~/.nuts-ndirc", "~/.bashrc", context.getWorkspace().config().getWorkspaceLocation());
                    } else if (updatedNdirc) {
                        context.session().out().printf("Updating ==%s== file to point to workspace ==%s==%n", "~/.nuts-ndirc", context.getWorkspace().config().getWorkspaceLocation());
                    } else if (updatedBashrc) {
                        context.session().out().printf("Updating ==%s== file to point to workspace ==%s==%n", "~/.bashrc", context.getWorkspace().config().getWorkspaceLocation());
                    }
                }
            }
            if (updatedNdirc || updatedBashrc) {
                context.session().terminal().ask()
                        .forBoolean(
                                "@@ATTENTION@@ You may need to re-run terminal or issue \\\"==%s==\\\" in your current terminal for new environment to take effect.%n"
                                + "Please type 'ok' if you agree, 'why' if you need more explanation or 'cancel' to cancel updates.",
                                ". ~/.bashrc"
                        )
                        .session(context.getSession())
                        .parser(new NutsQuestionParser<Boolean>() {
                            @Override
                            public Boolean parse(Object response, Boolean defaultValue, NutsQuestion<Boolean> question) {
                                if (response instanceof Boolean) {
                                    return (Boolean) response;
                                }
                                if (response == null || ((response instanceof String) && response.toString().length() == 0)) {
                                    response = defaultValue;
                                }
                                if (response == null) {
                                    throw new NutsValidationException(context.getWorkspace(), "Sorry... but you need to type 'ok', 'why' or 'cancel'");
                                }
                                String r = response.toString();
                                if ("ok".equalsIgnoreCase(r)) {
                                    return true;
                                }
                                if ("why".equalsIgnoreCase(r)) {
                                    PrintStream out = context.session().out();
                                    out.printf("\\\"==%s==\\\" is a special file in your home that is invoked upon each interactive terminal launch.%n", ".bashrc");
                                    out.print("It helps configuring environment variables. ==Nuts== make usage of such facility to update your **PATH** env variable\n");
                                    out.print("to point to current ==Nuts== workspace, so that when you call a ==Nuts== command it will be resolved correctly...\n");
                                    out.printf("However updating \\\"==%s==\\\" does not affect the running process/terminal. So you have basically two choices :%n", ".bashrc");
                                    out.print(" - Either to restart the process/terminal (konsole, term, xterm, sh, bash, ...)%n");
                                    out.printf(" - Or to run by your self the \\\"==%s==\\\" script (don\\'t forget the leading dot)%n", ". ~/.bashrc");
                                    throw new NutsValidationException(context.getWorkspace(), "Try again...'");
                                } else if ("cancel".equalsIgnoreCase(r) || "cancel!".equalsIgnoreCase(r)) {
                                    throw new NutsUserCancelException(context.getWorkspace());
                                } else {
                                    throw new NutsValidationException(context.getWorkspace(), "Sorry... but you need to type 'ok', 'why' or 'cancel'");
                                }
                            }
                        })
                        .getValue();
            }
        }
    }

    public Path getScriptFile(String name) {
        Path bin = context.getAppsFolder();
        return bin.resolve(name);
    }

    public NdiScriptnfo createScript(String name, NutsId fnutsId, boolean trace, String desc, Function<String, String> mapper) throws IOException {
        Path script = getScriptFile(name);
        if (script.getParent() != null) {
            if (!Files.exists(script.getParent())) {
                Files.createDirectories(script.getParent());
            }
        }
        if (trace && context.getSession().isPlainTrace()) {
            context.session().out().printf("Install %s script ==%s== for ==%s== at ==%s==%n", Files.exists(script) ? "(with override)" : "", script.getFileName(), desc, script);
        }

        try (BufferedWriter w = Files.newBufferedWriter(script)) {
            NdiUtils.generateScript("/net/vpc/app/nuts/toolbox/template_body_linux.text", w, mapper);
        }
        NdiUtils.setExecutable(script);
        return new NdiScriptnfo(name, fnutsId, script);
    }

}
