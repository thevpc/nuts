package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.NInstallStatus;

public class NInstallStatuses {
    public static NInstallStatus S____= NInstallStatus.of(false,false,false,false);
    public static NInstallStatus S__R_= NInstallStatus.of(false,true,false,false);
    public static NInstallStatus S__RO= NInstallStatus.of(false,true,false,true);
    public static NInstallStatus SD___= NInstallStatus.of(false,false,true,false);
    public static NInstallStatus SD_R_= NInstallStatus.of(false,true,true,false);
    public static NInstallStatus SD_RO= NInstallStatus.of(false,true,true,true);
    public static NInstallStatus SDI__= NInstallStatus.of(true,false,true,false);
    public static NInstallStatus SDI_O= NInstallStatus.of(true,false,true,true);
    public static NInstallStatus SDIR_= NInstallStatus.of(true,true,true,false);
    public static NInstallStatus SDIRO= NInstallStatus.of(true,true,true,true);
    public static NInstallStatus[] ALL_DEPLOYED={SDIR_, SDIRO, SDI__, SDI_O, SD_R_, SD_RO, S__R_, S__RO, SD___};
    public static NInstallStatus[] ALL_UNDEPLOYED={S____};

}
