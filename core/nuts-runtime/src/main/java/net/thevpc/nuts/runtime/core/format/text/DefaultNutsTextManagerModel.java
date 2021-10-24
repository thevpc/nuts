/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementFactoryService;
import net.thevpc.nuts.runtime.core.format.elem.NutsElementStreamFormat;
import net.thevpc.nuts.runtime.core.format.json.SimpleJson;
import net.thevpc.nuts.runtime.core.format.text.bloc.*;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.core.format.xml.DefaultXmlNutsElementStreamFormat;
import net.thevpc.nuts.runtime.core.format.yaml.SimpleYaml;
import net.thevpc.nuts.spi.NutsComponent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vpc
 */
public class DefaultNutsTextManagerModel {

    private final List<NutsCodeFormat> codeFormats = new ArrayList<>();
    private final NutsWorkspaceInitInformation info;
    private final NutsWorkspace ws;
    private String styleThemeName;
    private NutsTextFormatTheme styleTheme;
    private JavaBlocTextFormatter javaBlocTextFormatter;
    private HadraBlocTextFormatter hadraBlocTextFormatter;
    private XmlCodeFormatter xmlBlocTextFormatter;
    private JsonCodeFormatter jsonBlocTextFormatter;
    private BashBlocTextFormatter bashBlocTextFormatter;
    private BashBlocTextFormatter fishBlocTextFormatter;
    private BatBlocTextFormatter batBlocTextFormatter;
    private PlainBlocTextFormatter plainBlocTextFormatter;
    private NutsTextFormatTheme defaultTheme;
    private NutsElementFactoryService elementFactoryService;
    private NutsElementStreamFormat jsonMan;
    private NutsElementStreamFormat yamlMan;
    private NutsElementStreamFormat xmlMan;

    public DefaultNutsTextManagerModel(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.ws = ws;
        this.info = info;
    }


    public NutsTextFormatTheme createTheme(String y, NutsSession session) {
        y = y == null ? "" : y.trim();
        if (NutsBlankable.isBlank(y)) {
            y = "default";
        }
        if ("default".equals(y)) {
            //default always refers to this implementation
            if (defaultTheme == null) {
                if (session.env().getOsFamily() == NutsOsFamily.WINDOWS) {
                    //dark blue and red are very ugly under windows, replace them with green tones !
                    defaultTheme = new NutsTextFormatThemeWrapper(new NutsTextFormatPropertiesTheme("grass", null, session));
                } else {
                    defaultTheme = new DefaultNutsTextFormatTheme(ws);
                }
            }
            return defaultTheme;
        } else {
            return new NutsTextFormatThemeWrapper(new NutsTextFormatPropertiesTheme(y, null, session));
        }
    }

    public NutsTextFormatTheme getTheme(NutsSession session) {
        if (styleTheme == null) {
            if (styleThemeName == null) {
                styleThemeName = info.getOptions().getTheme();
            }
            styleTheme = createTheme(styleThemeName, session);
        }
        return styleTheme;
    }

    public void setTheme(NutsTextFormatTheme styleTheme, NutsSession session) {
        this.styleTheme = styleTheme;
    }

    public void setTheme(String styleThemeName, NutsSession session) {
        if (styleThemeName == null || styleThemeName.trim().isEmpty()) {
            styleThemeName = "default";
        }
        styleThemeName = styleThemeName.trim();
        styleTheme = createTheme(styleThemeName, session);
        this.styleThemeName = styleThemeName;
    }

    public NutsCodeFormat getCodeFormat(String kind, NutsSession session) {
        NutsDefaultSupportLevelContext<String> ctx = new NutsDefaultSupportLevelContext<String>(session, kind);
        int bestCode = NutsComponent.NO_SUPPORT;
        NutsCodeFormat format = null;
        for (NutsCodeFormat codeFormat : getCodeFormats(session)) {
            int s = codeFormat.getSupportLevel(ctx);
            if (s > bestCode) {
                format = codeFormat;
                bestCode = s;
            }
        }
        if (format != null) {
            return format;
        }
        if (kind.length() > 0) {
            switch (kind.toLowerCase()) {
                case "sh":
                case "ksh":
                case "zsh":
                case "csh":
                case "bash": {
                    return getBashBlocTextFormatter();
                }
                case "fish": {
                    return getFishBlocTextFormatter();
                }

                case "bat":
                case "cmd":
                case "powsershell": {
                    return getBatBlocTextFormatter();
                }

                case "system": {
                    switch (NutsShellFamily.getCurrent()) {
                        case SH:
                        case BASH:
                        case CSH:
                        case KSH:
                        case ZSH: {
                            return getBashBlocTextFormatter();
                        }
                        case FISH: {
                            return getFishBlocTextFormatter();
                        }
                        case WIN_CMD:
                        case WIN_POWER_SHELL: {
                            return getBatBlocTextFormatter();
                        }
                    }
                    return getBashBlocTextFormatter();
                }

                case "json": {
                    if (jsonBlocTextFormatter == null) {
                        jsonBlocTextFormatter = new JsonCodeFormatter(ws);
                    }
                    return jsonBlocTextFormatter;
                }

                case "xml": {
                    if (xmlBlocTextFormatter == null) {
                        xmlBlocTextFormatter = new XmlCodeFormatter(ws);
                    }
                    return xmlBlocTextFormatter;
                }

                case "java": {
                    if (javaBlocTextFormatter == null) {
                        javaBlocTextFormatter = new JavaBlocTextFormatter(ws);
                    }
                    return javaBlocTextFormatter;
                }
                case "hadra": {
                    if (hadraBlocTextFormatter == null) {
                        hadraBlocTextFormatter = new HadraBlocTextFormatter(ws);
                    }
                    return hadraBlocTextFormatter;
                }
                case "text":
                case "plain": {
                    if (plainBlocTextFormatter == null) {
                        plainBlocTextFormatter = new PlainBlocTextFormatter(ws);
                    }
                    return plainBlocTextFormatter;
                }
            }
        }
        return null;
    }

    private NutsCodeFormat getBatBlocTextFormatter() {
        if (batBlocTextFormatter == null) {
            batBlocTextFormatter = new BatBlocTextFormatter(ws);
        }
        return batBlocTextFormatter;
    }

    public BashBlocTextFormatter getFishBlocTextFormatter() {
        if (fishBlocTextFormatter == null) {
            fishBlocTextFormatter = new BashBlocTextFormatter(ws);
        }
        return fishBlocTextFormatter;
    }

    private NutsCodeFormat getBashBlocTextFormatter() {
        if (bashBlocTextFormatter == null) {
            bashBlocTextFormatter = new BashBlocTextFormatter(ws);
        }
        return bashBlocTextFormatter;
    }

    public void addCodeFormat(NutsCodeFormat format, NutsSession session) {
        codeFormats.add(format);
    }

    public void removeCodeFormat(NutsCodeFormat format, NutsSession session) {
        codeFormats.remove(format);
    }

    public NutsCodeFormat[] getCodeFormats(NutsSession session) {
        return codeFormats.toArray(new NutsCodeFormat[0]);
    }

    public NutsElementFactoryService getElementFactoryService(NutsSession session) {
        if (elementFactoryService == null) {
            elementFactoryService = new DefaultNutsElementFactoryService(ws, session);
        }
        return elementFactoryService;
    }

    public NutsElementStreamFormat getJsonMan(NutsSession session) {
        if (jsonMan == null) {
            jsonMan = new SimpleJson(ws);
        }
        return jsonMan;
    }

    public NutsElementStreamFormat getYamlMan(NutsSession session) {
        if (yamlMan == null) {
            yamlMan = new SimpleYaml(ws);
        }
        return yamlMan;
    }

    public NutsElementStreamFormat getXmlMan(NutsSession session) {
        if (xmlMan == null) {
            xmlMan = new DefaultXmlNutsElementStreamFormat();
        }
        return xmlMan;
    }

    public NutsWorkspace getWorkspace() {
        return ws;
    }

}
