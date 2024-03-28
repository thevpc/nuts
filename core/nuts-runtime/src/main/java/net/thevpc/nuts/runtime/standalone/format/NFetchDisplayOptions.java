/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.format;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.util.CoreEnumUtils;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;

/**
 *
 * @author thevpc
 */
public class NFetchDisplayOptions {

    public static NDisplayProperty[] DISPLAY_LONG = new NDisplayProperty[]{
        NDisplayProperty.STATUS,
        NDisplayProperty.INSTALL_DATE,
        NDisplayProperty.INSTALL_USER,
        NDisplayProperty.REPOSITORY,
        NDisplayProperty.ID
    };
    public static NDisplayProperty[] DISPLAY_LONG_LONG = new NDisplayProperty[]{
        NDisplayProperty.LONG_STATUS,
        NDisplayProperty.INSTALL_DATE,
        NDisplayProperty.INSTALL_USER,
        NDisplayProperty.REPOSITORY,
        NDisplayProperty.ID
    };
    public static NDisplayProperty[] DISPLAY_MIN = new NDisplayProperty[]{
        NDisplayProperty.ID
    };

    private NIdFormat idFormat;
    private List<NDisplayProperty> displays = new ArrayList<>();
    private NSession session;

    public NFetchDisplayOptions(NSession session) {
        this.session = session;
        this.idFormat = NIdFormat.of(session);
        this.idFormat.setHighlightImportedGroupId(true);
        this.idFormat.setOmitOtherProperties(true);
        this.idFormat.setOmitFace(true);
        this.idFormat.setOmitRepository(true);
//        this.idFormat.setOmitAlternative(false);
        this.idFormat.setOmitClassifier(false);
        this.idFormat.setOmitGroupId(false);
        this.idFormat.setOmitImportedGroupId(false);
    }

    public void setIdFormat(NIdFormat idFormat) {
        this.idFormat = idFormat;
    }

    public NIdFormat getIdFormat() {
        return idFormat;
    }

    public NDisplayProperty[] getDisplayProperties() {
        if (displays.isEmpty()) {
            return new NDisplayProperty[]{NDisplayProperty.ID};
        }
        return displays.toArray(new NDisplayProperty[0]);
    }

    public void addDisplay(String[] columns) {
        if (columns != null) {
            addDisplay(parseNutsDisplayProperty(Arrays.stream(columns).collect(Collectors.joining(","))));
        }
    }

    public void setDisplay(NDisplayProperty display) {
        if (display == null) {
            setDisplay(new NDisplayProperty[0]);
        } else {
            setDisplay(new NDisplayProperty[]{display});
        }
    }

    public void setDisplay(NDisplayProperty[] display) {
        displays.clear();
        addDisplay(display);
    }

    public void addDisplay(NDisplayProperty[] display) {
        if (display != null) {
            for (NDisplayProperty t : display) {
                if (t != null) {
                    displays.add(t);
                }
            }
        }
    }

    public void setDisplayLong(boolean longFormat) {
        if (longFormat) {
            setDisplay(DISPLAY_LONG);
        } else {
            setDisplay(DISPLAY_MIN);
        }
    }

    public boolean isRequireDefinition() {
        for (NDisplayProperty display : getDisplayProperties()) {
            if (!NDisplayProperty.ID.equals(display)) {
                return true;
            }
        }
        return false;
    }

    public final NFetchDisplayOptions configure(boolean skipUnsupported, String... args) {
        configure(false, NCmdLine.of(args));
        return this;
    }

    public final boolean configure(boolean skipUnsupported, NCmdLine cmdLine) {
        boolean conf = false;
        while (cmdLine.hasNext()) {
            if (!configureFirst(cmdLine)) {
                if (skipUnsupported) {
                    cmdLine.skip();
                } else {
                    cmdLine.throwUnexpectedArgument();
                }
            } else {
                conf = true;
            }
        }
        return conf;
    }

    public boolean configureFirst(NCmdLine cmdLine) {
        if (idFormat.configureFirst(cmdLine)) {
            return true;
        }
        NArg a = cmdLine.peek().get(session);
        if (a == null) {
            return false;
        }
        switch(a.key()) {
            case "-l":
            case "--long": {
                a = cmdLine.nextFlag().get(session);
                if(a.isActive()) {
                    if(a.getBooleanValue().get(session)){
                        setDisplay(DISPLAY_LONG);
                    }else {
                        setDisplay(DISPLAY_MIN);
                    }
                }
                return true;
            }
            case "--ll":
            case "--long-long": {
                a = cmdLine.nextFlag().get(session);
                if(a.isActive()) {
                    if(a.getBooleanValue().get(session)){
                        setDisplay(DISPLAY_LONG_LONG);
                    }else {
                        setDisplay(DISPLAY_MIN);
                    }
                }
                return true;
            }
            case "--display": {
                a = cmdLine.nextEntry().get(session);
                if(a.isActive()) {
                    setDisplay(parseNutsDisplayProperty(a.getStringValue().get(session)));
                }
                return true;
            }

        }
        return false;
    }

    public String[] toCmdLineOptions() {
        List<String> displayOptionsArgs = new ArrayList<>();
        if (this.getIdFormat() != null) {
            if (this.getIdFormat().isHighlightImportedGroupId()) {
                displayOptionsArgs.add("--highlight-imported-group");
            }
            if (this.getIdFormat().isOmitOtherProperties()) {
                displayOptionsArgs.add("--omit-env");
            }
            if (this.getIdFormat().isOmitFace()) {
                displayOptionsArgs.add("--omit-face");
            }
            if (this.getIdFormat().isOmitGroupId()) {
                displayOptionsArgs.add("--omit-group");
            }
            if (this.getIdFormat().isOmitImportedGroupId()) {
                displayOptionsArgs.add("--omit-imported-group");
            }
            if (this.getIdFormat().isOmitRepository()) {
                displayOptionsArgs.add("--omit-repo");
            }

            displayOptionsArgs.add("--display=" + String.join(",", Arrays.asList(getDisplayProperties()).stream().map(x -> CoreEnumUtils.getEnumString(x)).collect(Collectors.toList())));
        }
        return displayOptionsArgs.toArray(new String[0]);
    }

    public static NDisplayProperty[] parseNutsDisplayProperty(String str) {
        String[] dispNames = StringTokenizerUtils.splitDefault(str).toArray(new String[0]);
        //first pass, check is ALL is visited. In that case will be replaced by all non visited types
        Set<NDisplayProperty> visited = new HashSet<NDisplayProperty>();
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
                case "long-long": {
                    visited.addAll(Arrays.asList(DISPLAY_LONG_LONG));
                    break;
                }
                default: {
                    visited.add(CoreEnumUtils.parseEnumString(dispNames[i], NDisplayProperty.class, true));
                }
            }
        }
        List<NDisplayProperty> all2 = new ArrayList<>();
        for (int i = 0; i < dispNames.length; i++) {
            switch (dispNames[i]) {
                case "all": {
                    for (NDisplayProperty value : NDisplayProperty.values()) {
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
                case "long-long": {
                    all2.addAll(Arrays.asList(DISPLAY_LONG_LONG));
                    break;
                }
                default: {
                    all2.add(CoreEnumUtils.parseEnumString(dispNames[i], NDisplayProperty.class, false));
                }
            }
        }
        return (all2.toArray(new NDisplayProperty[0]));
    }

    @Override
    public String toString() {
        return "NutsFetchDisplayOptions{" +
                "idFormat=" + idFormat +
                ", displays=" + displays +
                '}';
    }
}
