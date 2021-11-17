package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NutsInstallStatus;

public class NutsInstallStatuses {
    public static NutsInstallStatus S____=NutsInstallStatus.of(false,false,false,false);
    public static NutsInstallStatus S__R_=NutsInstallStatus.of(false,true,false,false);
    public static NutsInstallStatus S__RO=NutsInstallStatus.of(false,true,false,true);
    public static NutsInstallStatus SD___=NutsInstallStatus.of(false,false,true,false);
    public static NutsInstallStatus SD_R_=NutsInstallStatus.of(false,true,true,false);
    public static NutsInstallStatus SD_RO=NutsInstallStatus.of(false,true,true,true);
    public static NutsInstallStatus SDI__=NutsInstallStatus.of(true,false,true,false);
    public static NutsInstallStatus SDI_O=NutsInstallStatus.of(true,false,true,true);
    public static NutsInstallStatus SDIR_=NutsInstallStatus.of(true,true,true,false);
    public static NutsInstallStatus SDIRO=NutsInstallStatus.of(true,true,true,true);
    public static NutsInstallStatus[] ALL_DEPLOYED={SDIR_, SDIRO, SDI__, SDI_O, SD_R_, SD_RO, S__R_, S__RO, SD___};
    public static NutsInstallStatus[] ALL_UNDEPLOYED={S____};

}
