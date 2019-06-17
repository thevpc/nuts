package net.vpc.app.nuts.toolbox.ndesced;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.NutsApplication;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

public class NdedMain extends NutsApplication {

    private NutsApplicationContext context;
//    private NutsWorkspaceExtensionManager f;

    private String home = null;
    private boolean interactive = false;

    public static void main(String[] args) {
        new NdedMain().runAndExit(args);
    }

    public void fillArgs(NutsDescriptorBuilder builder0) {
        NutsCommandLine commandLine = context.commandLine();
        NutsArgument a;
        while (commandLine.hasNext()) {
            if (context.configureFirst(commandLine)) {

            } else if ((a = commandLine.nextString("--home")) != null) {
                home = a.getStringValue();
            } else if ((a = commandLine.nextString("--id")) != null) {
                String v = a.getStringValue();
                builder0.setId(v);
            } else if ((a = commandLine.nextString("--alternative")) != null) {
                String v = a.getStringValue();
                builder0.setAlternative(v);
            } else if ((a = commandLine.nextString("--name")) != null) {
                String v = a.getStringValue();
                builder0.setName(v);
            } else if ((a = commandLine.nextString("--packaging")) != null) {
                String v = a.getStringValue();
                builder0.setPackaging(v);
            } else if ((a = commandLine.nextString("--platform")) != null) {
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if (s.length() > 0) {
                        builder0.addPlatform(s);
                    }
                }
            } else if ((a = commandLine.nextString("--os")) != null) {
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if (s.length() > 0) {
                        builder0.addOs(s);
                    }
                }
            } else if ((a = commandLine.nextString("--osdist")) != null) {
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if (s.length() > 0) {
                        builder0.addOsdist(s);
                    }
                }
            } else if ((a = commandLine.nextString("--arch")) != null) {
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if (s.length() > 0) {
                        builder0.addArch(s);
                    }
                }
            } else if ((a = commandLine.nextString("--location")) != null) {
                String v = a.getStringValue();
                builder0.addLocation(v);
            } else if ((a = commandLine.nextBoolean("-i", "--interactive")) != null) {
                interactive = a.getBooleanValue();
            } else {
                commandLine.setCommandName("nded").unexpectedArgument();
            }
        }
    }

    public String checkParam(String name, String lastValue) {
        return context.session().getTerminal().readLine("Enter %s%s : ", name, (lastValue == null ? "" : (" " + lastValue)));
    }

    public void fillInteractive(NutsDescriptorBuilder b, boolean nullOnly) {
        if (!interactive) {
            return;
        }
        if (!nullOnly || isBlank(home)) {
            String s = checkParam("home", home);
            if (!isBlank(s)) {
                home = s;
            }
        }
        if (!nullOnly || b.getId() == null) {
            String s = checkParam("id", b.getId() == null ? null : b.getId().toString());
            if (!isBlank(s)) {
                try {
                    b.setId(s);
                } catch (Exception ex) {
                    context.session().err().println(ex.getMessage());
                }
            }
        }
        if (!nullOnly || isBlank(b.getPackaging())) {
            String s = checkParam("packaging", b.getPackaging());
            if (!isBlank(s)) {
                b.setPackaging(s);
            }
        }
//        if (!nullOnly || isEmpty(b.getExt())) {
//            String s = checkParam("ext", b.getExt());
//            if (s != null) {
//                b.setExt(s);
//            }
//        }
        if (!nullOnly || b.getArch().length == 0) {
            String s = checkParam("arch", Arrays.toString(b.getArch()));
            if (!isBlank(s)) {
                b.addArch(s);
            }
        }
        if (!nullOnly || b.getOs().length == 0) {
            String s = checkParam("os", Arrays.toString(b.getOs()));
            if (!isBlank(s)) {
                b.addOs(s);
            }
        }
        if (!nullOnly || b.getOsdist().length == 0) {
            String s = checkParam("osdist", Arrays.toString(b.getOs()));
            if (!isBlank(s)) {
                b.addOsdist(s);
            }
        }
        if (!nullOnly || b.getLocations().length == 0) {
            String s = checkParam("location", Arrays.toString(b.getLocations()));
            if (!isBlank(s)) {
                b.addLocation(s);
            }
        }

    }

    public boolean check(NutsDescriptorBuilder b) {
        boolean error = false;
        if (b.getId() == null) {
            error = true;
            context.session().err().print("Missing id\n");
        } else {
            if (b.getId().getName() == null) {
                error = true;
                context.session().err().print("Missing id name\n");
            }
            if (b.getId().getGroup() == null) {
                error = true;
                context.session().err().print("Missing id group\n");
            }
            if (b.getId().getVersion() == null) {
                error = true;
                context.session().err().print("Missing id version\n");
            }

        }
        if (isBlank(b.getPackaging())) {
            error = true;
            context.session().err().print("Missing packaging\n");
        }
        if (isBlank(home)) {
            error = true;
            context.session().err().print("Missing nuts-bootstrap\n");
        }
        return !error;
    }

    private boolean confirm(String message) {
        while (true) {
            String o = context.session().terminal().readLine(message + " (y/n) : ");
            if (o == null) {
                o = "";
            }
            o = o.trim();
            if ("yes".equalsIgnoreCase(o)
                    || "y".equalsIgnoreCase(o)
                    || "o".equalsIgnoreCase(o)
                    || "oui".equalsIgnoreCase(o)) {
                //exit
                return true;
            } else if ("no".equalsIgnoreCase(o)
                    || "n".equalsIgnoreCase(o)
                    || "non".equalsIgnoreCase(o)) {
                return false;
            }

        }
    }

    public void run(NutsApplicationContext context) {
        this.context = context;
        String[] args = context.getArguments();
//        f = this.appContext.getWorkspace().getExtensionManager();
        NutsDescriptorBuilder b = this.context.getWorkspace().format().descriptor().descriptorBuilder();
        fillArgs(b);
        final PrintStream out = this.context.session().out();
        out.print("[[Creating new Nuts descriptor...]]\n");
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
                throw new NutsExecutionException(context.getWorkspace(),"Cancelled", 1);
            }
        }
        String path = b.getId().getGroup().replace('.', '/')
                + '/' + b.getId().getName()
                + '/' + b.getId().getVersion()
                + '/' + b.getId().getName() + "-" + b.getId().getVersion() + ".json";
        File file = new File(home, path);
        file.getParentFile().mkdirs();
        NutsDescriptor desc = b.build();
        out.printf("Writing to : ==%s==[[%s]]%n", getFilePath(new File(home)), ("/" + path).replace('/', File.separatorChar));
        out.printf("id         : ==%s==%n", desc.getId());
        out.printf("packaging  : ==%s==%n", desc.getPackaging() == null ? "" : desc.getPackaging());
        if (desc.getLocations().length > 0) {
            out.println("locations  : ");
            for (String s : b.getLocations()) {
                out.printf("             ==%s==%n", s);
            }
        }
        if (!confirm("Confirm ?")) {
            throw new NutsUserCancelException(context.getWorkspace());
        }
        NutsDescriptorFormat nutsDescriptorFormat = context.getWorkspace().format().descriptor();
        nutsDescriptorFormat.set(desc).print(file);
        nutsDescriptorFormat.set(desc).print(out);
    }

    private static String getFilePath(File s) {
        String p = null;
        try {
            p = s.getCanonicalPath();
        } catch (Exception ex) {
            p = s.getAbsolutePath();
        }
        return p;
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
