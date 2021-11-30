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
package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.NutsElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.NutsElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultJsonElementFormat;
import net.thevpc.nuts.runtime.standalone.text.highlighters.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.standalone.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.standalone.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultXmlNutsElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.yaml.SimpleYaml;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsDefaultSupportLevelContext;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNutsTextManagerModel {

    private final NutsWorkspace ws;
    private final Map<String, String> kindToHighlighter = new HashMap<>();
    private final Map<String, NutsCodeHighlighter> highlighters = new HashMap<>();
    private final Map<String, NutsCodeHighlighter> _cachedHighlighters = new HashMap<>();
    private String styleThemeName;
    private NutsTextFormatTheme styleTheme;
    private NutsTextFormatTheme defaultTheme;
    private NutsElementFactoryService elementFactoryService;
    private NutsElementStreamFormat jsonMan;
    private NutsElementStreamFormat yamlMan;
    private NutsElementStreamFormat xmlMan;

    public DefaultNutsTextManagerModel(NutsWorkspace ws) {
        this.ws = ws;
        NutsSession session = NutsWorkspaceUtils.defaultSession(ws);
        List<NutsCodeHighlighter> all = session.extensions().createAllSupported(NutsCodeHighlighter.class, null);
        for (NutsCodeHighlighter h : all) {
            highlighters.put(h.getId().toLowerCase(), h);
        }
        try {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(
                            getClass().getResourceAsStream(
                                    "/net/thevpc/nuts/runtime/highlighter-mappings.ini"
                            )
                    )
            )) {
                String line;
                String group = null;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.startsWith("#") && line.length() > 0) {
                        if (line.startsWith("[") && line.endsWith("]")) {
                            group = line.substring(1, line.length() - 1).trim();
                        } else if (group != null) {
                            for (String s : line.split("[,;]")) {
                                s = s.trim();
                                kindToHighlighter.put(s, group);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
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
                NutsWorkspaceOptions bootOptions = NutsWorkspaceExt.of(this.ws).getModel().bootModel.getBootOptions();
                styleThemeName = bootOptions.getTheme();
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

    public NutsCodeHighlighter getCodeHighlighter(String highlighterId, NutsSession session) {
        String lc = NutsUtilStrings.trim(highlighterId).toLowerCase();
        NutsCodeHighlighter old = _cachedHighlighters.get(lc);
        if (old != null) {
            return old;
        }
        NutsCodeHighlighter h = highlighters.get(lc);
        if (h != null) {
            _cachedHighlighters.put(lc, h);
            return h;
        }
        int best = -1;
        for (NutsCodeHighlighter hh : highlighters.values()) {
            int lvl = hh.getSupportLevel(new NutsDefaultSupportLevelContext(
                    session, lc
            ));
            if (lvl > 0 && best < lvl) {
                best = lvl;
                h = hh;
            }
        }
        if (best > 0) {
            _cachedHighlighters.put(lc, h);
            return h;
        }
        String a = kindToHighlighter.get(lc);
        if (a != null) {
            h = highlighters.get(a);
            if (h != null) {
                _cachedHighlighters.put(lc, h);
                return h;
            }
        }
        if ("system".equals(lc)) {
            NutsShellFamily shellFamily = session.env().getShellFamily();
            h = getCodeHighlighter(shellFamily.id(), session);
            _cachedHighlighters.put(lc, h);
            return h;
        }

        if (lc.length() > 0) {
            try {
                NutsTextStyle found = NutsTextStyle.parseLenient(lc);
                if (found != null) {
                    h = new CustomStyleCodeHighlighter(found, session);
                    _cachedHighlighters.put(lc, h);
                    return h;
                }
            } catch (Exception ex) {
                //ignore
            }
        }

        h = highlighters.get("plain");
        if (h != null) {
            return h;
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.plain("not found plain highlighter"));
    }

    private String expandAlias(String ss) {
        switch (ss.toUpperCase()) {
            case "BOOL": {
                ss = "BOOLEAN";
                break;
            }
            case "KW": {
                ss = "KEYWORD";
                break;
            }
        }
        return ss;
    }


    public void addCodeHighlighter(NutsCodeHighlighter format, NutsSession session) {
        highlighters.put(format.getId(), format);
    }

    public void removeCodeHighlighter(String id, NutsSession session) {
        highlighters.remove(id);
    }

    public NutsCodeHighlighter[] getCodeHighlighters(NutsSession session) {
        return highlighters.values().toArray(new NutsCodeHighlighter[0]);
    }

    public NutsElementFactoryService getElementFactoryService(NutsSession session) {
        if (elementFactoryService == null) {
            elementFactoryService = new DefaultNutsElementFactoryService(ws, session);
        }
        return elementFactoryService;
    }

    public NutsElementStreamFormat getJsonMan(NutsSession session) {
        if (jsonMan == null) {
            jsonMan = new DefaultJsonElementFormat(ws);
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
