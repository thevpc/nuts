package net.vpc.app.nuts.toolbox.ndesced;

import net.vpc.app.nuts.*;

import java.io.File;
import java.util.Arrays;

public class NdedMain {

    private NutsWorkspace ws;
    private String[] args;
    private NutsWorkspaceExtensionManager f;
    private NutsSession session;
    private NutsFormattedPrintStream out;
    private NutsFormattedPrintStream err;
    private NutsTerminal terminal;

    private String home = null;
    private boolean interactive = false;

    public static void main(String[] args) {
        NutsWorkspace ws = Nuts.openWorkspace(args);
        args = ws.getBootOptions().getApplicationArguments();
        new NdedMain(ws, args).main();
    }

    public NdedMain(NutsWorkspace ws, String[] args) {
        this.ws = ws;
        this.args = args;
        f = ws.getExtensionManager();
        session = ws.createSession();
        terminal = session.getTerminal();
        out = terminal.getFormattedOut();
        err = terminal.getFormattedErr();
    }

    public void fillArgs(NutsDescriptorBuilder builder0) {
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--nuts-bootstrap": {
                    i++;
                    home = (args[i]);
                    break;
                }
                case "--id": {
                    i++;
                    builder0.setId(args[i]);
                    break;
                }
                case "--ext": {
                    i++;
                    builder0.setExt(args[i]);
                    break;
                }
                case "--packaging": {
                    i++;
                    builder0.setPackaging(args[i]);
                    break;
                }
                case "--name": {
                    i++;
                    builder0.setName(args[i]);
                    break;
                }
                case "--platform": {
                    i++;
                    builder0.addPlatform(args[i]);
                    break;
                }
                case "--os": {
                    i++;
                    builder0.addOs(args[i]);
                    break;
                }
                case "--osdist": {
                    i++;
                    builder0.addOsdist(args[i]);
                    break;
                }
                case "--arch": {
                    i++;
                    builder0.addArch(args[i]);
                    break;
                }
                case "--location": {
                    i++;
                    builder0.addLocation(args[i]);
                    break;
                }
                case "--interactive": {
                    interactive = true;
                    break;
                }
            }
        }
    }

    public String checkParam(String name, String lastValue) {
        return terminal.readLine("Enter " + name + (lastValue == null ? "" : (" " + lastValue)) + " : ");
    }

    public void fillInteractive(NutsDescriptorBuilder b, boolean nullOnly) {
        if (!interactive) {
            return;
        }
        if (!nullOnly || isEmpty(home)) {
            String s = checkParam("nuts-bootstrap", home);
            if (!isEmpty(s)) {
                home = s;
            }
        }
        if (!nullOnly || b.getId() == null) {
            String s = checkParam("id", b.getId() == null ? null : b.getId().toString());
            if (!isEmpty(s)) {
                try {
                    b.setId(s);
                }catch (Exception ex){
                    err.printf(ex.getMessage());
                }
            }
        }
        if (!nullOnly || isEmpty(b.getPackaging())) {
            String s = checkParam("packaging", b.getPackaging());
            if (!isEmpty(s)) {
                b.setPackaging(s);
            }
        }
        if (!nullOnly || isEmpty(b.getExt())) {
            String s = checkParam("ext", b.getExt());
            if (s != null) {
                b.setExt(s);
            }
        }
        if (!nullOnly || b.getArch().length == 0) {
            String s = checkParam("arch", Arrays.toString(b.getArch()));
            if (!isEmpty(s)) {
                b.addArch(s);
            }
        }
        if (!nullOnly || b.getOs().length == 0) {
            String s = checkParam("os", Arrays.toString(b.getOs()));
            if (!isEmpty(s)) {
                b.addOs(s);
            }
        }
        if (!nullOnly || b.getOsdist().length == 0) {
            String s = checkParam("osdist", Arrays.toString(b.getOs()));
            if (!isEmpty(s)) {
                b.addOsdist(s);
            }
        }
        if (!nullOnly || b.getLocations().length == 0) {
            String s = checkParam("location", Arrays.toString(b.getLocations()));
            if (!isEmpty(s)) {
                b.addLocation(s);
            }
        }

    }

    public boolean check(NutsDescriptorBuilder b) {
        boolean error = false;
        if (b.getId() == null) {
            error = true;
            err.printf("Missing id\n");
        } else {
            if (b.getId().getName() == null) {
                error = true;
                err.printf("Missing id name\n");
            }
            if (b.getId().getGroup() == null) {
                error = true;
                err.printf("Missing id group\n");
            }
            if (b.getId().getVersion() == null) {
                error = true;
                err.printf("Missing id version\n");
            }

        }
        if (isEmpty(b.getPackaging())) {
            error = true;
            err.printf("Missing packaging\n");
        }
        if (isEmpty(b.getExt())) {
            error = true;
            err.printf("Missing ext\n");
        }
        if(isEmpty(home)){
            error = true;
            err.printf("Missing nuts-bootstrap\n");
        }
        return !error;
    }

    private boolean confirm(String message) {
        while (true) {
            String o = terminal.readLine(message + " (y/n) : ");
            if (o == null) {
                o = "";
            }
            o = o.trim();
            if (
                    "yes".equalsIgnoreCase(o)
                            || "y".equalsIgnoreCase(o)
                            || "o".equalsIgnoreCase(o)
                            || "oui".equalsIgnoreCase(o)
                    ) {
                //exit
                return true;
            } else if (
                    "no".equalsIgnoreCase(o)
                            || "n".equalsIgnoreCase(o)
                            || "non".equalsIgnoreCase(o)
                    ) {
                return false;
            }

        }
    }

    public void main() {
        NutsDescriptorBuilder b = f.createDescriptorBuilder();
        fillArgs(b);
        out.printf("[[Creating new Nuts descriptor...]]\n");
        while (true) {
            fillInteractive(b, true);
            if (check(b)) {
                break;
            }
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                //
            }
            if (confirm("Abort?")) {
                return;
            }
        }
        String path = b.getId().getGroup().replace('.', '/')
                + '/' + b.getId().getName()
                + '/' + b.getId().getVersion()
                + '/' + b.getId().getName() + "-" + b.getId().getVersion() + ".json";
        File file = new File(home,path);
        file.getParentFile().mkdirs();
        NutsDescriptor desc = b.build();
        out.printf("Writing to : ==%s==[[%s]]\n", getFilePath(new File(home)),("/"+path).replace('/',File.separatorChar));
        out.printf("id         : ==%s==\n", desc.getId());
        out.printf("packaging  : ==%s==\n", desc.getPackaging());
        out.printf("ext        : ==%s==\n", desc.getExt());
        if (desc.getLocations().length > 0) {
            out.printf("locations  : \n");
            for (String s : b.getLocations()) {
                out.printf("             ==%s==\n", s);
            }
        }
        if (!confirm("Confirm ?")) {
            return;
        }
        desc.write(file);
        desc.write(out);
//        if (!home.equals("stdout")) {
//            return;
//        }
//
//        NutsDescriptorBuilder builder = f.createDescriptorBuilder();
//
//        builder.setId(
//                f.createIdBuilder()
//                        .setName("name")
//                        .setVersion("version")
//                        .build()
//        )
//                .setFace("face")
//                .setName("Application Full Name")
//                .setDescription("Application Description")
//                .setExecutable(true)
//                .setPackaging("jar")
//                .setExt("exe")
//                .setArch(new String[]{"64bit"})
//                .setOs(new String[]{"linux#4.6"})
//                .setOsdist(new String[]{"opensuse#42"})
//                .setPlatform(new String[]{"java#8"})
//                .setExecutor(new NutsExecutorDescriptor(
//
//                        f.createIdBuilder().setName("java").setVersion("8").build(),
//                        new String[]{"-jar"}
//                ))
//                .setInstaller(new NutsExecutorDescriptor(
//                        f.createIdBuilder().setName("java").setVersion("8").build(),
//                        new String[]{"-jar"}
//                ))
//                .setLocations(new String[]{
//                        "http://server/somelink"
//                })
//                .setDependencies(
//                        new NutsDependency[]{
//                                f.createDependencyBuilder()
//                                        .setNamespace("namespace")
//                                        .setName("name")
//                                        .setVersion("version")
//                                        .setScope("compile")
//                                        .build()
//                        }
//                )
//                .build()
//                .write(System.out);
    }

    private static String getFilePath(File s){
        String p = null;
        try {
            p = s.getCanonicalPath();
        }catch (Exception ex) {
            p = s.getAbsolutePath();
        }
        return p;
    }
    private static boolean isEmpty(String s){
        return s==null|| s.trim().isEmpty();
    }
}
