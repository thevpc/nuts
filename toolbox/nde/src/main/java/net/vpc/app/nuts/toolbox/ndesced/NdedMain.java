package net.vpc.app.nuts.toolbox.ndesced;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;

import java.io.File;
import java.util.Arrays;

public class NdedMain extends NutsApplication {

    private NutsApplicationContext appContext;
    private NutsWorkspaceExtensionManager f;

    private String home = null;
    private boolean interactive = false;

    public static void main(String[] args) {
        new NdedMain().launch(args);
    }

    public void fillArgs(NutsDescriptorBuilder builder0) {
        for (int i = 0; i < appContext.getArgs().length; i++) {
            switch (appContext.getArgs()[i]) {
                case "--nuts-bootstrap": {
                    i++;
                    home = (appContext.getArgs()[i]);
                    break;
                }
                case "--id": {
                    i++;
                    builder0.setId(appContext.getArgs()[i]);
                    break;
                }
                case "--ext": {
                    i++;
                    builder0.setExt(appContext.getArgs()[i]);
                    break;
                }
                case "--packaging": {
                    i++;
                    builder0.setPackaging(appContext.getArgs()[i]);
                    break;
                }
                case "--name": {
                    i++;
                    builder0.setName(appContext.getArgs()[i]);
                    break;
                }
                case "--platform": {
                    i++;
                    builder0.addPlatform(appContext.getArgs()[i]);
                    break;
                }
                case "--os": {
                    i++;
                    builder0.addOs(appContext.getArgs()[i]);
                    break;
                }
                case "--osdist": {
                    i++;
                    builder0.addOsdist(appContext.getArgs()[i]);
                    break;
                }
                case "--arch": {
                    i++;
                    builder0.addArch(appContext.getArgs()[i]);
                    break;
                }
                case "--location": {
                    i++;
                    builder0.addLocation(appContext.getArgs()[i]);
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
        return appContext.getTerminal().readLine("Enter %s%s : ",name,(lastValue == null ? "" : (" " + lastValue)));
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
                    appContext.err().printf(ex.getMessage());
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
            appContext.err().printf("Missing id\n");
        } else {
            if (b.getId().getName() == null) {
                error = true;
                appContext.err().printf("Missing id name\n");
            }
            if (b.getId().getGroup() == null) {
                error = true;
                appContext.err().printf("Missing id group\n");
            }
            if (b.getId().getVersion() == null) {
                error = true;
                appContext.err().printf("Missing id version\n");
            }

        }
        if (isEmpty(b.getPackaging())) {
            error = true;
            appContext.err().printf("Missing packaging\n");
        }
        if (isEmpty(b.getExt())) {
            error = true;
            appContext.err().printf("Missing ext\n");
        }
        if(isEmpty(home)){
            error = true;
            appContext.err().printf("Missing nuts-bootstrap\n");
        }
        return !error;
    }

    private boolean confirm(String message) {
        while (true) {
            String o = appContext.getTerminal().readLine(message + " (y/n) : ");
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

    public int launch(NutsApplicationContext appContext) {
        this.appContext = appContext;
        String[] args=appContext.getArgs();
        f = this.appContext.getWorkspace().getExtensionManager();
        NutsDescriptorBuilder b = this.appContext.getWorkspace().createDescriptorBuilder();
        fillArgs(b);
        this.appContext.out().printf("[[Creating new Nuts descriptor...]]\n");
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
                return 1;
            }
        }
        String path = b.getId().getGroup().replace('.', '/')
                + '/' + b.getId().getName()
                + '/' + b.getId().getVersion()
                + '/' + b.getId().getName() + "-" + b.getId().getVersion() + ".json";
        File file = new File(home,path);
        file.getParentFile().mkdirs();
        NutsDescriptor desc = b.build();
        this.appContext.out().printf("Writing to : ==%s==[[%s]]\n", getFilePath(new File(home)),("/"+path).replace('/',File.separatorChar));
        this.appContext.out().printf("id         : ==%s==\n", desc.getId());
        this.appContext.out().printf("packaging  : ==%s==\n", desc.getPackaging());
        this.appContext.out().printf("ext        : ==%s==\n", desc.getExt());
        if (desc.getLocations().length > 0) {
            this.appContext.out().printf("locations  : \n");
            for (String s : b.getLocations()) {
                this.appContext.out().printf("             ==%s==\n", s);
            }
        }
        if (!confirm("Confirm ?")) {
            return 1;
        }
        desc.write(file);
        desc.write(this.appContext.out());
        return 0;
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
