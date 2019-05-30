/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommand;
import net.vpc.app.nuts.NutsIdFormat;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

/**
 *
 * @author vpc
 */
public class NutsFetchDisplayOptions {

    private NutsIdFormat idFormat;
    //        boolean omitGroup = false;
    //        boolean omitNamespace = true;
    //        boolean omitImportedGroup = false;
    //        boolean highlightImportedGroup = true;
    private List<NutsDisplayType> displays = new ArrayList<>();
    private NutsWorkspace ws;
    private static NutsDisplayType[] DISPLAY_LONG=new NutsDisplayType[]{
                    NutsDisplayType.STATUS,
                    NutsDisplayType.INSTALL_DATE,
                    NutsDisplayType.INSTALL_USER,
                    NutsDisplayType.ID
                };
    private static NutsDisplayType[] DISPLAY_MIN=new NutsDisplayType[]{
                    NutsDisplayType.ID
                };

    public NutsFetchDisplayOptions(NutsWorkspace ws) {
        this.ws = ws;
        this.idFormat = ws.formatter().createIdFormat();
    }

    public void setIdFormat(NutsIdFormat idFormat) {
        this.idFormat = idFormat;
    }

    public NutsIdFormat getIdFormat() {
        return idFormat;
    }

    public NutsDisplayType[] getDisplays() {
        if (displays.isEmpty()) {
            return new NutsDisplayType[]{NutsDisplayType.ID};
        }
        return displays.toArray(new NutsDisplayType[0]);
    }

    public void setDisplay(NutsDisplayType display) {
        if (display == null) {
            setDisplay(new NutsDisplayType[0]);
        } else {
            setDisplay(new NutsDisplayType[]{display});
        }
    }

    public void setDisplay(NutsDisplayType[] display) {
        if (display == null) {
            displays.clear();
        } else {
            for (NutsDisplayType t : display) {
                if (t != null) {
                    displays.add(t);
                }
            }
        }
    }

    public void setDisplayLong(boolean longFormat) {
        if(longFormat){
            setDisplay(DISPLAY_LONG);
        }else{
            setDisplay(DISPLAY_MIN);
        }
    }

    public boolean isRequireDefinition() {
        for (NutsDisplayType display : getDisplays()) {
            if (!NutsDisplayType.ID.equals(display)) {
                return true;
            }
        }
        return false;
    }

    public final NutsFetchDisplayOptions configure(String[] args) {
        configure(ws.parser().parseCommand(args), false);
        return this;
    }

    public final boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        boolean conf = false;
        while (commandLine.hasNext()) {
            if (!configureFirst(commandLine)) {
                if (skipIgnored) {
                    commandLine.skip();
                } else {
                    commandLine.unexpectedArgument();
                }
            } else {
                conf = true;
            }
        }
        return conf;
    }

    public boolean configureFirst(NutsCommand cmdLine) {
        if (idFormat.configureFirst(cmdLine)) {
            return true;
        }
        NutsArgument a = cmdLine.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "-l":
            case "--long": 
            case "--display-long": 
            {
                setDisplayLong(cmdLine.nextBoolean().getValue().getBoolean());
                return true;
            }
            case "--display-id": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.ID);
                return true;
            }
            case "--display-name": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.NAME);
                return true;
            }
            case "--display-exec-entry": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.ID);
                return true;
            }
            case "--display-arch": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.ARCH);
                return true;
            }
            case "--display-file": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.FILE);
                return true;
            }
            case "--display-file-name": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.FILE_NAME);
                return true;
            }
            case "--display-packaging": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.PACKAGING);
                return true;
            }
            case "--display-os": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.OS);
                return true;
            }
            case "--display-osdist": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.OSDIST);
                return true;
            }
            case "--display-platform": {
                cmdLine.skip();
                setDisplay(NutsDisplayType.PLATFORM);
                return true;
            }
            case "--display": {
                String[] dispNames = cmdLine.nextString().getValue().getString().split("[,|; ]");
                //first pass, check is ALL is visited. In that case will be replaced by all non visited types
                Set<NutsDisplayType> visited = new HashSet<NutsDisplayType>();
                for (int i = 0; i < dispNames.length; i++) {
                    switch (dispNames[i]) {
                        case "all": {
                            //ignore in this pass
                            break;
                        }
                        case "long": {
                            visited.addAll(Arrays.asList(DISPLAY_LONG));
                            break;
                        }
                        default: {
                            visited.add(CoreCommonUtils.parseEnumString(dispNames[i], NutsDisplayType.class, true));
                        }
                    }
                }
                List<NutsDisplayType> all2 = new ArrayList<>();
                for (int i = 0; i < dispNames.length; i++) {
                    switch (dispNames[i]) {
                        case "all": {
                            for (NutsDisplayType value : NutsDisplayType.values()) {
                                if (!visited.contains(value)) {
                                    all2.add(value);
                                }
                            }
                            break;
                        }
                        case "long": {
                            all2.addAll(Arrays.asList(DISPLAY_LONG));
                            break;
                        }
                        default: {
                            all2.add(CoreCommonUtils.parseEnumString(dispNames[i], NutsDisplayType.class, false));
                        }
                    }
                }
                setDisplay(all2.toArray(new NutsDisplayType[0]));
                return true;
            }

        }
        return false;
    }

    public String[] toCommandLineOptions() {
        List<String> displayOptionsArgs = new ArrayList<>();
        if (this.getIdFormat() != null) {
            if (this.getIdFormat().isHighlightImportedGroup()) {
                displayOptionsArgs.add("--highlight-imported-group");
            }
            if (this.getIdFormat().isHighlightOptional()) {
                displayOptionsArgs.add("--highlight-optional");
            }
            if (this.getIdFormat().isHighlightScope()) {
                displayOptionsArgs.add("--highlight-scope");
            }
            if (this.getIdFormat().isOmitEnv()) {
                displayOptionsArgs.add("--omit-env");
            }
            if (this.getIdFormat().isOmitFace()) {
                displayOptionsArgs.add("--omit-face");
            }
            if (this.getIdFormat().isOmitGroup()) {
                displayOptionsArgs.add("--omit-group");
            }
            if (this.getIdFormat().isOmitImportedGroup()) {
                displayOptionsArgs.add("--omit-imported-group");
            }
            if (this.getIdFormat().isOmitNamespace()) {
                displayOptionsArgs.add("--omit-namespace");
            }

            displayOptionsArgs.add("--display=" + CoreStringUtils.join(",", Arrays.asList(getDisplays()).stream().map(x -> CoreCommonUtils.getEnumString(x)).collect(Collectors.toList())));
        }
        return displayOptionsArgs.toArray(new String[0]);
    }

}
