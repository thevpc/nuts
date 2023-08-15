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
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.NElementFactoryService;
import net.thevpc.nuts.runtime.standalone.elem.NElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.json.DefaultJsonElementFormat;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.text.highlighter.CustomStyleCodeHighlighter;
import net.thevpc.nuts.runtime.standalone.text.theme.DefaultNTextFormatTheme;
import net.thevpc.nuts.runtime.standalone.text.theme.NTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.standalone.text.theme.NTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.standalone.format.xml.DefaultXmlNElementStreamFormat;
import net.thevpc.nuts.runtime.standalone.format.yaml.SimpleYaml;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.spi.NDefaultSupportLevelContext;
import net.thevpc.nuts.text.NTextFormatTheme;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOsFamily;
import net.thevpc.nuts.util.NStringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author thevpc
 */
public class DefaultNTextManagerModel {

    private final NWorkspace ws;
    private final Map<String, String> kindToHighlighter = new HashMap<>();
    private final Map<String, NCodeHighlighter> highlighters = new HashMap<>();
    private final Map<String, NCodeHighlighter> _cachedHighlighters = new HashMap<>();
    private String styleThemeName;
    private NTextFormatTheme styleTheme;
    private NTextFormatTheme defaultTheme;
    private NElementFactoryService elementFactoryService;
    private NElementStreamFormat jsonMan;
    private NElementStreamFormat yamlMan;
    private NElementStreamFormat xmlMan;

    public DefaultNTextManagerModel(NWorkspace ws) {
        this.ws = ws;
    }
    public void loadExtensions(){
        NSession session = NSessionUtils.defaultSession(ws);
        List<NCodeHighlighter> all = session.extensions().createComponents(NCodeHighlighter.class, null);
        for (NCodeHighlighter h : all) {
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
                            for (String s : StringTokenizerUtils.splitDefault(line)) {
                                s = s.trim();
                                kindToHighlighter.put(s, group);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            throw new NIOException(session, ex);
        }
    }


    public NTextFormatTheme createTheme(String y, NSession session) {
        y = y == null ? "" : y.trim();
        if (NBlankable.isBlank(y)) {
            y = "default";
        }
        if ("default".equals(y)) {
            //default always refers to this implementation
            if (defaultTheme == null) {
                if (NEnvs.of(session).getOsFamily() == NOsFamily.WINDOWS) {
                    //dark blue and red are very ugly under windows, replace them with green tones !
                    defaultTheme = new NTextFormatThemeWrapper(new NTextFormatPropertiesTheme("grass", null, session));
                } else {
                    defaultTheme = new DefaultNTextFormatTheme(ws);
                }
            }
            return defaultTheme;
        } else {
            return new NTextFormatThemeWrapper(new NTextFormatPropertiesTheme(y, null, session));
        }
    }

    public NTextFormatTheme getTheme(NSession session) {
        if (styleTheme == null) {
            if (styleThemeName == null) {
                NWorkspaceOptions bootOptions = NWorkspaceExt.of(this.ws).getModel().bootModel.getBootUserOptions();
                styleThemeName = bootOptions.getTheme().orNull();
            }
            styleTheme = createTheme(styleThemeName, session);
        }
        return styleTheme;
    }

    public void setTheme(NTextFormatTheme styleTheme, NSession session) {
        this.styleTheme = styleTheme;
    }

    public void setTheme(String styleThemeName, NSession session) {
        if (styleThemeName == null || styleThemeName.trim().isEmpty()) {
            styleThemeName = "default";
        }
        styleThemeName = styleThemeName.trim();
        styleTheme = createTheme(styleThemeName, session);
        this.styleThemeName = styleThemeName;
    }

    public NCodeHighlighter getCodeHighlighter(String highlighterId, NSession session) {
        String lc = NStringUtils.trim(highlighterId).toLowerCase();
        NCodeHighlighter old = _cachedHighlighters.get(lc);
        if (old != null) {
            return old;
        }
        NCodeHighlighter h = highlighters.get(lc);
        if (h != null) {
            _cachedHighlighters.put(lc, h);
            return h;
        }
        int best = -1;
        for (NCodeHighlighter hh : highlighters.values()) {
            int lvl = hh.getSupportLevel(new NDefaultSupportLevelContext(
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
            NShellFamily shellFamily = NEnvs.of(session).getShellFamily();
            h = getCodeHighlighter(shellFamily.id(), session);
            _cachedHighlighters.put(lc, h);
            return h;
        }

        if (lc.length() > 0) {
            try {
                NTextStyle found = NTextStyle.parse(lc).orNull();
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
        throw new NIllegalArgumentException(session, NMsg.ofPlain("not found plain highlighter"));
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


    public void addCodeHighlighter(NCodeHighlighter format, NSession session) {
        highlighters.put(format.getId(), format);
    }

    public void removeCodeHighlighter(String id, NSession session) {
        highlighters.remove(id);
    }

    public NCodeHighlighter[] getCodeHighlighters(NSession session) {
        return highlighters.values().toArray(new NCodeHighlighter[0]);
    }

    public NElementFactoryService getElementFactoryService(NSession session) {
        if (elementFactoryService == null) {
            elementFactoryService = new DefaultNElementFactoryService(session);
        }
        return elementFactoryService;
    }

    public NElementStreamFormat getJsonMan(NSession session) {
        if (jsonMan == null) {
            jsonMan = new DefaultJsonElementFormat(ws);
        }
        return jsonMan;
    }

    public NElementStreamFormat getYamlMan(NSession session) {
        if (yamlMan == null) {
            yamlMan = new SimpleYaml(ws);
        }
        return yamlMan;
    }

    public NElementStreamFormat getXmlMan(NSession session) {
        if (xmlMan == null) {
            xmlMan = new DefaultXmlNElementStreamFormat();
        }
        return xmlMan;
    }

    public NWorkspace getWorkspace() {
        return ws;
    }

}
