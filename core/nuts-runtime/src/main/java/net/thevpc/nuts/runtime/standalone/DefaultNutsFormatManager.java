package net.thevpc.nuts.runtime.standalone;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.DefaultNutsIncrementalOutputFormat;
import net.thevpc.nuts.runtime.core.format.DefaultNutsObjectFormat;
//import net.thevpc.nuts.runtime.core.format.DefaultNutsStringFormat;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFormat;
import net.thevpc.nuts.runtime.core.format.tree.DefaultTreeFormat;
import net.thevpc.nuts.runtime.core.format.props.DefaultPropertiesFormat;
import net.thevpc.nuts.runtime.core.format.table.DefaultTableFormat;
import net.thevpc.nuts.runtime.core.format.text.DefaultNutsTextManager;
import net.thevpc.nuts.runtime.core.format.text.bloc.HadraBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.JavaBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.JsonCodeFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.PlainBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.ShellBlocTextFormatter;
import net.thevpc.nuts.runtime.core.format.text.bloc.XmlCodeFormatter;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.DefaultNutsTextFormatTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatPropertiesTheme;
import net.thevpc.nuts.runtime.core.format.text.stylethemes.NutsTextFormatThemeWrapper;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.spi.NutsComponent;

public class DefaultNutsFormatManager implements NutsFormatManager {

    private NutsWorkspace ws;
    private NutsTextFormatTheme styleTheme;
    private List<NutsCodeFormat> codeFormats = new ArrayList<>();
    private JavaBlocTextFormatter javaBlocTextFormatter;
    private HadraBlocTextFormatter hadraBlocTextFormatter;
    private XmlCodeFormatter xmlBlocTextFormatter;
    private JsonCodeFormatter jsonBlocTextFormatter;
    private ShellBlocTextFormatter shellBlocTextFormatter;
    private PlainBlocTextFormatter plainBlocTextFormatter;
    private NutsWorkspaceInitInformation info;
    private DefaultNutsTextFormatTheme defaultTheme;

    public DefaultNutsFormatManager(NutsWorkspace ws, NutsWorkspaceInitInformation info) {
        this.ws = ws;
        String y = info.getOptions().getTheme();
        if (!CoreStringUtils.isBlank(y)) {
            if ("default".equals(y)) {
                //default always refers to the this implementation
                styleTheme = getDefaultTheme();
            } else {
                styleTheme = new NutsTextFormatThemeWrapper(new NutsTextFormatPropertiesTheme(y, null, ws));
            }
        } else {
            styleTheme = getDefaultTheme();
        }
    }

    public final DefaultNutsTextFormatTheme getDefaultTheme() {
        if (defaultTheme == null) {
            defaultTheme = new DefaultNutsTextFormatTheme(ws);
        }
        return defaultTheme;
    }

//    @Override
//    public NutsJsonFormat json() {
//        return new DefaultNutsJsonFormat(ws);
//    }
//
//    @Override
//    public NutsXmlFormat xml() {
//        return new DefaultNutsXmlFormat(ws);
//    }
    @Override
    public NutsElementFormat element() {
        return new DefaultNutsElementFormat(ws);
    }
//
//    @Override
//    public NutsStringFormat str() {
//        return new DefaultNutsStringFormat(ws);
//    }

    @Override
    public NutsTreeFormat tree() {
        return new DefaultTreeFormat(ws);
    }

    @Override
    public NutsTableFormat table() {
        return new DefaultTableFormat(ws);
    }

    @Override
    public NutsPropertiesFormat props() {
        return new DefaultPropertiesFormat(ws);
    }

    @Override
    public NutsObjectFormat object() {
        return new DefaultNutsObjectFormat(ws);
    }

    @Override
    public NutsIterableOutput iter() {
        return new DefaultNutsIncrementalOutputFormat(ws);
    }

    @Override
    public NutsTextFormatTheme getTheme() {
        return styleTheme;
    }

    @Override
    public NutsFormatManager setTheme(NutsTextFormatTheme styleTheme) {
        if (styleTheme == null) {
            styleTheme = getDefaultTheme();
        }
        this.styleTheme = styleTheme;
        return this;
    }

    @Override
    public NutsTextManager text() {
        return new DefaultNutsTextManager(ws, styleTheme);
    }

    @Override
    public NutsCodeFormat getCodeFormat(String kind) {
        DefaultNutsSupportLevelContext<String> ctx = new DefaultNutsSupportLevelContext<String>(ws, kind);
        int bestCode = NutsComponent.NO_SUPPORT;
        NutsCodeFormat format = null;
        for (NutsCodeFormat codeFormat : getCodeFormats()) {
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
                case "sh": {
                    if (shellBlocTextFormatter == null) {
                        shellBlocTextFormatter = new ShellBlocTextFormatter(ws);
                    }
                    return shellBlocTextFormatter;
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

    @Override
    public NutsFormatManager addCodeFormat(NutsCodeFormat format) {
        codeFormats.add(format);
        return this;
    }

    @Override
    public NutsFormatManager removeCodeFormat(NutsCodeFormat format) {
        codeFormats.remove(format);
        return this;
    }

    @Override
    public NutsCodeFormat[] getCodeFormats() {
        return codeFormats.toArray(new NutsCodeFormat[0]);
    }
}
