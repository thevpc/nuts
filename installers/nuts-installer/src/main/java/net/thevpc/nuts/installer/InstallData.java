package net.thevpc.nuts.installer;

import net.thevpc.nuts.installer.util.VerInfo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InstallData {
    public boolean installStable;
    public boolean optionZ;
    public boolean optionS;
    public boolean optionVerbose;
    public boolean optionk;
    public boolean optionSwitch;
    public List<String> otherOptions=new ArrayList<>();
    public VerInfo installVersion;
    public String workspace;
    public Set<App> recommendedIds=new LinkedHashSet<>();

    public static InstallData of(InstallerContext context){
        InstallData c=(InstallData) context.getVars().get(InstallData.class.getName());
        if(c==null){
            c=new InstallData();
            context.getVars().put(InstallData.class.getName(),c);
        }
        return c;
    }
}
