package net.vpc.app.nuts.toolbox.ndesced;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;
import net.vpc.common.commandline.Argument;
import net.vpc.common.commandline.CommandLine;

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
        CommandLine commandLine=new CommandLine(appContext.getArgs(),appContext.getAutoComplete());
        Argument a;
        while (commandLine.hasNext()){
            if(appContext.configure(commandLine)){

            }else if((a=commandLine.readStringOption("--home"))!=null){
                home = a.getStringValue();
            }else if((a=commandLine.readStringOption("--id"))!=null){
                String v = a.getStringValue();
                builder0.setId(v);
            }else if((a=commandLine.readStringOption("--alternative"))!=null){
                String v = a.getStringValue();
                builder0.setAlternative(v);
            }else if((a=commandLine.readStringOption("--name"))!=null){
                String v = a.getStringValue();
                builder0.setName(v);
            }else if((a=commandLine.readStringOption("--packaging"))!=null){
                String v = a.getStringValue();
                builder0.setPackaging(v);
            }else if((a=commandLine.readStringOption("--platform"))!=null){
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if(s.length()>0){
                        builder0.addPlatform(s);
                    }
                }
            }else if((a=commandLine.readStringOption("--os"))!=null){
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if(s.length()>0){
                        builder0.addOs(s);
                    }
                }
            }else if((a=commandLine.readStringOption("--osdist"))!=null){
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if(s.length()>0){
                        builder0.addOsdist(s);
                    }
                }
            }else if((a=commandLine.readStringOption("--arch"))!=null){
                String v = a.getStringValue();
                for (String s : v.split(" ,;")) {
                    if(s.length()>0){
                        builder0.addArch(s);
                    }
                }
            }else if((a=commandLine.readStringOption("--location"))!=null){
                String v = a.getStringValue();
                builder0.addLocation(v);
            }else if((a=commandLine.readBooleanOption("-i","--interactive"))!=null){
                interactive = a.getBooleanValue();
            }else{
                commandLine.unexpectedArgument("nded");
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
            String s = checkParam("home", home);
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
                    appContext.err().println(ex.getMessage());
                }
            }
        }
        if (!nullOnly || isEmpty(b.getPackaging())) {
            String s = checkParam("packaging", b.getPackaging());
            if (!isEmpty(s)) {
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
            appContext.err().print("Missing id\n");
        } else {
            if (b.getId().getName() == null) {
                error = true;
                appContext.err().print("Missing id name\n");
            }
            if (b.getId().getGroup() == null) {
                error = true;
                appContext.err().print("Missing id group\n");
            }
            if (b.getId().getVersion() == null) {
                error = true;
                appContext.err().print("Missing id version\n");
            }

        }
        if (isEmpty(b.getPackaging())) {
            error = true;
            appContext.err().print("Missing packaging\n");
        }
        if(isEmpty(home)){
            error = true;
            appContext.err().print("Missing nuts-bootstrap\n");
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
        this.appContext.out().print("[[Creating new Nuts descriptor...]]\n");
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
        this.appContext.out().printf("packaging  : ==%s==\n", desc.getPackaging()==null?"":desc.getPackaging());
        if (desc.getLocations().length > 0) {
            this.appContext.out().print("locations  : \n");
            for (String s : b.getLocations()) {
                this.appContext.out().printf("             ==%s==\n", s);
            }
        }
        if (!confirm("Confirm ?")) {
            return 1;
        }
        NutsDescriptorFormat nutsDescriptorFormat = appContext.getWorkspace().getFormatManager().createDescriptorFormat().setPretty(true);
        nutsDescriptorFormat.format(desc,file);
        nutsDescriptorFormat.format(desc,this.appContext.out());
        return 0;
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
