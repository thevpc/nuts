package net.thevpc.nuts.toolbox.nadmin.temp;

import net.thevpc.nuts.*;

import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;

public class NdedMain  {

    private NutsApplicationContext context;
//    private NutsWorkspaceExtensionManager f;

    private String home = null;
    private boolean interactive = false;

//    public static void main(String[] args) {
//        new NdedMain().runAndExit(args);
//    }

    public void fillArgs(NutsDescriptorBuilder builder0) {
        NutsCommandLine commandLine = context.getCommandLine();
        NutsArgument a;
        while (commandLine.hasNext()) {
            if ((a = commandLine.nextString("--home")) != null) {
                home = a.getStringValue();
            } else if ((a = commandLine.nextString("--id")) != null) {
                String v = a.getStringValue();
                builder0.setId(v);
//            } else if ((a = commandLine.nextString("--alternative")) != null) {
//                String v = a.getStringValue();
//                builder0.setAlternative(v);
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
                builder0.addLocation(
                        context.getWorkspace().descriptor().locationBuilder().setUrl(v).setClassifier(null).build()
                        );
            } else if ((a = commandLine.nextBoolean("-i", "--interactive")) != null) {
                interactive = a.getBooleanValue();
            } else if (commandLine.peek().isOption()) {
                context.configureLast(commandLine);
            } else {
                commandLine.setCommandName("nded").unexpectedArgument();
            }
        }
    }

    public String checkParam(String name, String lastValue) {
        return context.getSession().getTerminal().readLine("Enter %s%s : ", name, (lastValue == null ? "" : (" " + lastValue)));
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
                    context.getSession().err().println(ex.getMessage());
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
                b.addLocation(context.getWorkspace().descriptor().locationBuilder().setUrl(s).build());
            }
        }

    }

    public boolean check(NutsDescriptorBuilder b) {
        boolean error = false;
        if (b.getId() == null) {
            error = true;
            context.getSession().err().print("missing id\n");
        } else {
            if (b.getId().getArtifactId() == null) {
                error = true;
                context.getSession().err().print("missing id name\n");
            }
            if (b.getId().getGroupId() == null) {
                error = true;
                context.getSession().err().print("missing id group\n");
            }
            if (b.getId().getVersion() == null) {
                error = true;
                context.getSession().err().print("missing id version\n");
            }

        }
        if (isBlank(b.getPackaging())) {
            error = true;
            context.getSession().err().print("missing packaging\n");
        }
        if (isBlank(home)) {
            error = true;
            context.getSession().err().print("missing nuts-bootstrap\n");
        }
        return !error;
    }

    private boolean confirm(String message) {
        while (true) {
            String o = context.getSession().getTerminal().readLine(message + " (y/n) : ");
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
        NutsDescriptorBuilder b = this.context.getWorkspace().descriptor().descriptorBuilder();
        fillArgs(b);
        final PrintStream out = this.context.getSession().out();
        NutsTextNodeFactory factory = context.getWorkspace().formats().text().factory();
        out.print(factory.styled("creating new nuts descriptor...\n",NutsTextNodeStyle.primary(3)));
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
            if (confirm("abort?")) {
                throw new NutsExecutionException(context.getWorkspace(), "cancelled", 1);
            }
        }
        String path = b.getId().getGroupId().replace('.', '/')
                + '/' + b.getId().getArtifactId()
                + '/' + b.getId().getVersion()
                + '/' + b.getId().getArtifactId() + "-" + b.getId().getVersion() + ".json";
        File file = new File(home, path);
        file.getParentFile().mkdirs();
        NutsDescriptor desc = b.build();
        NutsTextFormatManager text = context.getWorkspace().formats().text();
        out.printf("writing to : %s%n",
                text.factory().styled(
                        getFilePath(new File(home))+("/" + path).replace('/', File.separatorChar)
                        ,NutsTextNodeStyle.path())
                );
        out.printf("id         : %s%n", desc.getId());
        out.printf("packaging  : %s%n",
                factory.styled(
                desc.getPackaging() == null ? "" : desc.getPackaging(),NutsTextNodeStyle.primary(3)));
        if (desc.getLocations().length > 0) {
            out.println("locations  : ");
            for (NutsIdLocation s : b.getLocations()) {
                out.printf("             %s %s%n",
                        factory.styled(s.getClassifier(),NutsTextNodeStyle.primary(3)),
                        text.factory().styled(s.getUrl(),NutsTextNodeStyle.path()));
            }
        }
        if (!confirm("confirm ?")) {
            throw new NutsUserCancelException(context.getWorkspace());
        }
        NutsDescriptorFormat nutsDescriptorFormat = context.getWorkspace().descriptor().formatter(desc);
        nutsDescriptorFormat.print(file);
        nutsDescriptorFormat.print(out);
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
