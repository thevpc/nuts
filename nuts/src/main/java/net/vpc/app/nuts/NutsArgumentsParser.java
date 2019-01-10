package net.vpc.app.nuts;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NutsArgumentsParser {
    private NutsArgumentsParser() {
    }

    public static NutsWorkspaceOptions parseNutsArguments(String[] bootArguments) {
        List<String> showError = new ArrayList<>();
        NutsWorkspaceOptions o = new NutsWorkspaceOptions().setCreateIfNotFound(true);
        HashSet<String> excludedExtensions = new HashSet<>();
        HashSet<String> excludedRepositories = new HashSet<>();
        HashSet<String> tempRepositories = new HashSet<>();
        List<String> executorOptions = new ArrayList<>();
        NutsLogConfig logConfig = null;
        o.setSaveIfCreated(true);
        NutsMinimalCommandLine.Arg cmdArg;
        CmdArgList2 cmdArgList = new CmdArgList2(bootArguments);
        while ((cmdArg = cmdArgList.next()) != null) {
            if (cmdArg.isOption()) {
                switch (cmdArg.getKey()) {
                    //dash  should be the very last argument
                    case "--boot-version":
                    case "--boot-api-version": {
                        o.setRequiredBootVersion(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "-": {
                        if (cmdArg.getValue() != null) {
                            throw new NutsIllegalArgumentException("Invalid argument for workspace : " + cmdArg.getArg());
                        }
                        cmdArgList.applicationArguments.add(NutsConstants.NUTS_SHELL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--home": {
                        o.setHome(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--workspace": {
                        o.setWorkspace(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--archetype": {
                        o.setArchetype(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--login": {
                        o.setLogin(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--boot-runtime": {
                        String br = cmdArgList.getValueFor(cmdArg);
                        if (br.indexOf("#") > 0) {
                            //this is a full id
                        } else {
                            br = NutsConstants.NUTS_ID_BOOT_RUNTIME + "#" + br;
                        }
                        o.setBootRuntime(br);
                        break;
                    }
                    case "--runtime-source-url": {
                        o.setBootRuntimeSourceURL(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--java":
                    case "--boot-java": {
                        o.setBootJavaCommand(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--java-home":
                    case "--boot-java-home": {
                        o.setBootJavaCommand(NutsUtils.resolveJavaCommand(cmdArgList.getValueFor(cmdArg)));
                        break;
                    }
                    case "--java-options":
                    case "--boot-java-options": {
                        o.setBootJavaOptions(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--save": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setSaveIfCreated(true);
                        break;
                    }
                    case "--no-save":
                    case "--!save": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setSaveIfCreated(false);
                        break;
                    }
                    case "--!colors":
                    case "--no-colors": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setNoColors(true);
                        break;
                    }
                    case "--read-only": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setReadOnly(true);
                        break;
                    }
                    case "-version":
                    case "--version": {
                        o.setBootCommand(NutsBootCommand.VERSION);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--info": {
                        o.setBootCommand(NutsBootCommand.INFO);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--update": {
                        o.setBootCommand(NutsBootCommand.UPDATE);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--clean": {
                        o.setBootCommand(NutsBootCommand.CLEAN);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--reset": {
                        o.setBootCommand(NutsBootCommand.RESET);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--install-companions": {
                        o.setBootCommand(NutsBootCommand.INSTALL_COMPANIONS);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--check-updates": {
                        o.setBootCommand(NutsBootCommand.CHECK_UPDATES);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--install": {
                        o.setBootCommand(NutsBootCommand.INSTALL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--uninstall": {
                        o.setBootCommand(NutsBootCommand.UNINSTALL);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--exec": {
                        o.setBootCommand(NutsBootCommand.EXEC);
                        while ((cmdArg = cmdArgList.next()) != null) {
                            if (cmdArg.isOption()) {
                                executorOptions.add(cmdArg.getArg());
                            } else {
                                cmdArgList.applicationArguments.add(cmdArg.getArg());
                                cmdArgList.consumeApplicationArguments();
                            }
                        }
                        break;
                    }
                    case "--help": {
                        o.setBootCommand(NutsBootCommand.HELP);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--license": {
                        o.setBootCommand(NutsBootCommand.LICENSE);
                        cmdArgList.consumeApplicationArguments();
                        break;
                    }
                    case "--verbose":
                    case "--log-finest":
                    case "--log-finer":
                    case "--log-fine":
                    case "--log-info":
                    case "--log-warning":
                    case "--log-severe":
                    case "--log-all":
                    case "--log-off":
                    case "--log-size":
                    case "--log-name":
                    case "--log-folder":
                    case "--log-count":
                    case "--log-inherited": {
                        logConfig = new NutsLogConfig();
                        parseLogLevel(logConfig, cmdArg, cmdArgList);
                        break;
                    }
                    case "--exclude-extension": {
                        excludedExtensions.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--exclude-repository": {
                        excludedRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--repository": {
                        tempRepositories.add(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--perf": {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        o.setPerf(true);
                        break;
                    }
                    case "--auto-config": {
                        o.setAutoConfig(cmdArg.getKey() == null ? "" : cmdArg.getKey());
                        break;
                    }
                    case "--store-layout": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setStoreLocationLayout(v.isEmpty() ? null : StoreLocationLayout.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--store-strategy": {
                        String v = cmdArgList.getValueFor(cmdArg);
                        o.setStoreLocationStrategy(v.isEmpty() ? null : StoreLocationStrategy.valueOf(v.toUpperCase()));
                        break;
                    }
                    case "--config-location": {
                        o.setConfigStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--programs-location": {
                        o.setProgramsStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--cache-location": {
                        o.setCacheStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--temp-location": {
                        o.setTempStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--var-location": {
                        o.setVarStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--logs-location": {
                        o.setLogsStoreLocation(cmdArgList.getValueFor(cmdArg));
                        break;
                    }
                    case "--system-layout": {
                        o.setStoreLocationLayout(StoreLocationLayout.SYSTEM);
                        break;
                    }
                    case "--windows-layout": {
                        o.setStoreLocationLayout(StoreLocationLayout.WINDOWS);
                        break;
                    }
                    case "--linux-layout": {
                        o.setStoreLocationLayout(StoreLocationLayout.LINUX);
                        break;
                    }
                    case "--bundle-strategy": {
                        o.setStoreLocationStrategy(StoreLocationStrategy.BUNDLE);
                        break;
                    }
                    case "--system-strategy": {
                        o.setStoreLocationStrategy(StoreLocationStrategy.SYSTEM);
                        break;
                    }
                    case "--no-create": {
                        o.setCreateIfNotFound(false);
                        break;
                    }
                    default: {
                        cmdArgList.bootOnlyArgsList.add(cmdArg.getArg());
                        showError.add("nuts: invalid option [[" + cmdArg.getArg() + "]]");
                    }
                }
            } else {
                cmdArgList.applicationArguments.add(cmdArg.getArg());
                cmdArgList.consumeApplicationArguments();
            }
        }

        o.setLogConfig(logConfig);
        //NutsUtils.split(bootArguments[i], " ,;")
        o.setExcludedExtensions(excludedExtensions.toArray(new String[0]));
        o.setExcludedRepositories(excludedRepositories.toArray(new String[0]));
        o.setTransientRepositories(tempRepositories.toArray(new String[0]));
        if (o.getBootCommand() != NutsBootCommand.HELP) {
            if (!showError.isEmpty()) {
                StringBuilder errorMessage = new StringBuilder();
                for (String s : showError) {
                    errorMessage.append(s).append("\n");
                }
                errorMessage.append("Try 'nuts --help' for more information.\n");
                throw new NutsIllegalArgumentException(errorMessage.toString());
            }
        }
        o.setBootArguments(cmdArgList.bootOnlyArgsList.toArray(new String[0]));
        o.setApplicationArguments(cmdArgList.applicationArguments.toArray(new String[0]));
        o.setExecutorOptions(executorOptions.toArray(new String[0]));
        return o;
    }


    private static void parseLogLevel(NutsLogConfig logConfig, NutsMinimalCommandLine.Arg cmdArg, NutsMinimalCommandLine cmdArgList) {
        switch (cmdArg.getKey()) {
            case "--log-size": {
                logConfig.setLogSize(Integer.parseInt(cmdArgList.getValueFor(cmdArg)));
                break;
            }
            case "--log-count": {
                logConfig.setLogCount(Integer.parseInt(cmdArgList.getValueFor(cmdArg)));
                break;
            }
            case "--log-name": {
                logConfig.setLogName(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-folder": {
                logConfig.setLogFolder(cmdArgList.getValueFor(cmdArg));
                break;
            }
            case "--log-inherited": {
                logConfig.setLogInherited(true);
                break;
            }
            case "--verbose":
            case "--log-finest":
            case "--log-finer":
            case "--log-fine":
            case "--log-info":
            case "--log-warning":
            case "--log-severe":
            case "--log-all":
            case "--log-off": {
                if (cmdArgList instanceof CmdArgList2) {
                    ((CmdArgList2) cmdArgList).bootOnlyArgsList.add(cmdArg.getArg());
                }
                String id = cmdArg.getKey();
                if (cmdArg.getKey().startsWith("--log-")) {
                    id = id.substring("--log-".length());
                } else if (cmdArg.getKey().equals("--log")) {
                    id = cmdArg.getValue();
                    if (id == null) {
                        id = "";
                    }
                } else if (id.startsWith("--")) {
                    id = cmdArg.getKey().substring(2);
                } else {
                    id = cmdArg.getKey();
                }
                switch (id.toLowerCase()) {
                    case "verbose": {
                        logConfig.setLogLevel(Level.FINEST);
                        break;
                    }
                    case "finest": {
                        logConfig.setLogLevel(Level.FINEST);
                        break;
                    }
                    case "finer": {
                        logConfig.setLogLevel(Level.FINER);
                        break;
                    }
                    case "fine": {
                        logConfig.setLogLevel(Level.FINE);
                        break;
                    }
                    case "info": {
                        logConfig.setLogLevel(Level.INFO);
                        break;
                    }
                    case "warning": {
                        logConfig.setLogLevel(Level.WARNING);
                        break;
                    }
                    case "config": {
                        logConfig.setLogLevel(Level.CONFIG);
                        break;
                    }
                    case "all": {
                        logConfig.setLogLevel(Level.ALL);
                        break;
                    }
                    case "off": {
                        logConfig.setLogLevel(Level.OFF);
                        break;
                    }
                    default: {
                        logConfig.setLogLevel(Level.INFO);
                        break;
                    }
                }
                break;
            }
        }
    }

    private static class CmdArgList2 extends NutsMinimalCommandLine {
        List<String> bootOnlyArgsList = new ArrayList<>();
        List<String> applicationArguments = new ArrayList<>();

        public CmdArgList2(String[] args) {
            super(args);
        }

        public String getValueFor(Arg cmdArg) {
            String v = super.getValueFor(cmdArg);
            bootOnlyArgsList.add(cmdArg.getKey() + "=" + v);
            return v;
        }

        public void consumeApplicationArguments() {
            applicationArguments.addAll(removeAll());
        }
    }

}
