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
import net.thevpc.nuts.runtime.core.format.text.highlighters.*;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.core.format.xml.DefaultXmlNutsElementStreamFormat;
import net.thevpc.nuts.runtime.core.format.yaml.SimpleYaml;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

/**
 * @author vpc
 */
public class DefaultNutsTextManagerModel {

    private final NutsWorkspaceInitInformation info;
    private final NutsWorkspace ws;
    private String styleThemeName;
    private NutsTextFormatTheme styleTheme;
    private NutsTextFormatTheme defaultTheme;
    private NutsElementFactoryService elementFactoryService;
    private NutsElementStreamFormat jsonMan;
    private NutsElementStreamFormat yamlMan;
    private NutsElementStreamFormat xmlMan;
    private final Map<String, String> kindToHighlighter = new HashMap<>();

    private final Map<String, NutsCodeHighlighter> highlighters = new HashMap<>();

    public DefaultNutsTextManagerModel(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.ws = ws;
        this.info = info;
        for (NutsCodeHighlighter h : new NutsCodeHighlighter[]{
                new JavaCodeHighlighter(ws),
                new HadraCodeHighlighter(ws),
                new XmlCodeHighlighter(ws),
                new JsonCodeHighlighter(ws),
                new BashCodeHighlighter(ws),
                new FishCodeHighlighter(ws),
                new WinCmdBlocTextHighlighter(ws),
                new PlainCodeHighlighter(ws),
                new NtfCodeHighlighter()
        }) {
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
            throw new NutsIOException(NutsWorkspaceUtils.defaultSession(ws), ex);
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

    public String getCodeHighlighterId(String kind, NutsSession session) {
        String lc = kind.toLowerCase();
        NutsCodeHighlighter h = highlighters.get(lc);
        if (h != null) {
            return h.getId();
        }
        String a = kindToHighlighter.get(lc);
        if (a != null) {
            h = highlighters.get(a);
            if (h != null) {
                return h.getId();
            }
        }
        if("system".equals(kind)){
            NutsShellFamily shellFamily = session.env().getShellFamily();
            return getCodeHighlighterId(shellFamily.id(),session);
        }

        h = highlighters.get("plain");
        if (h != null) {
            return h.getId();
        }
        throw new NutsIllegalArgumentException(session, NutsMessage.plain("not found plain highlighter"));
    }

    public NutsCodeHighlighter getCodeHighlighter(String highlighterId, NutsSession session) {
        highlighterId = getCodeHighlighterId(highlighterId, session);
        return highlighters.get(highlighterId);

//        NutsDefaultSupportLevelContext<String> ctx = new NutsDefaultSupportLevelContext<String>(session, highlighterId);
//        int bestCode = NutsComponent.NO_SUPPORT;
//        NutsCodeHighlighter format = null;
//        for (NutsCodeHighlighter codeFormat : getCodeFormats(session)) {
//            int s = codeFormat.getSupportLevel(ctx);
//            if (s > bestCode) {
//                format = codeFormat;
//                bestCode = s;
//            }
//        }
//        if (format != null) {
//            return format;
//        }
//        if (highlighterId.length() > 0) {
//            String q = getCodeHighlighterId(highlighterId, session);
//            return highlighters.get(q);
//        }
//        return null;
    }


    public void addCodeHighlighter(NutsCodeHighlighter format, NutsSession session) {
        highlighters.put(format.getId(),format);
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
