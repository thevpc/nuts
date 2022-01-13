package net.thevpc.nuts.installer;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

public class InstallData {
    public boolean installStable;
    public boolean optionZ;
    public boolean optionS;
    public boolean optionk;
    public String installVersion;
    public String workspace;
    public Set<String> recommendedIds=new LinkedHashSet<>();
    public String otherOptions;

    public static InstallData of(InstallerContext context){
        InstallData c=(InstallData) context.getVars().get(InstallData.class.getName());
        if(c==null){
            c=new InstallData();
            context.getVars().put(InstallData.class.getName(),c);
        }
        return c;
    }
}
