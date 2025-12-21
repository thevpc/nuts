//package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;
//
//import net.thevpc.nuts.artifact.NId;
//import net.thevpc.nuts.command.NExecTargetInfo;
//import net.thevpc.nuts.platform.NEnv;
//import net.thevpc.nuts.platform.NOsFamily;
//import net.thevpc.nuts.platform.NShellFamily;
//import net.thevpc.nuts.util.NStringUtils;
//
//class LocalNExecTargetInfo implements NExecTargetInfo {
//    public static final LocalNExecTargetInfo INSTANCE = new LocalNExecTargetInfo();
//
//    @Override
//    public NOsFamily getOsFamily() {
//        return NEnv.of().getOsFamily();
//    }
//
//    @Override
//    public NId getOsId() {
//        return NEnv.of().getOs();
//    }
//
//    @Override
//    public NId getShellId() {
//        return NId.of(null, NStringUtils.firstNonBlank(NEnv.of().getShellFamily().id()));
//    }
//
//    @Override
//    public NShellFamily getShellFamily() {
//        return NEnv.of().getShellFamily();
//    }
//
//    @Override
//    public String getUserName() {
//        return System.getProperty("user.name");
//    }
//
//    @Override
//    public String getRootUserName() {
//        switch (getOsFamily()) {
//            case WINDOWS: {
//                switch (NStringUtils.trim(getUserName()).toLowerCase()) {
//                    case "adminitrateur": {
//                        return "Administrateur";
//                    }
//                    case "administrador": {
//                        return "Administrador";
//                    }
//                    case "administratör": {
//                        return "Administratör";
//                    }
//                    case "järjestelmänvalvoja": {
//                        return "Järjestelmänvalvoja";
//                    }
//                    case "rendszergazda": {
//                        return "Rendszergazda";
//                    }
//                    case "администратор": {
//                        return "Администратор";
//                    }
//                }
//                return "Administrateur";
//            }
//            default: {
//                return "root";
//            }
//        }
//    }
//
//    @Override
//    public String getUserHome() {
//        return System.getProperty("user.home");
//    }
//
//}
