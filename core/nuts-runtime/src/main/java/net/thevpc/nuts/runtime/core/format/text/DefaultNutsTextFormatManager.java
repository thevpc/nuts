package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.parser.*;
import net.thevpc.nuts.runtime.standalone.io.NutsWorkspaceVarExpansionFunction;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;

import java.io.*;
import java.net.URL;
import java.text.MessageFormat;
import java.time.temporal.Temporal;
import java.util.*;

public class DefaultNutsTextFormatManager implements NutsTextFormatManager {
    private NutsWorkspace ws;
    private NutsWorkspaceVarExpansionFunction pathExpansionConverter;
    private NutsTextNodeFactory nodesFactory;

    public DefaultNutsTextFormatManager(NutsWorkspace ws) {
        this.ws = ws;
        pathExpansionConverter=new NutsWorkspaceVarExpansionFunction(ws);
        nodesFactory=new DefaultNutsTextNodeFactory(ws);
    }


    @Override
    public NutsTextNodeFactory factory() {
        return nodesFactory;
    }

    @Override
    public NutsTextNodeBuilder builder() {
        return new DefaultNutsTextNodeBuilder(ws);
    }

    @Override
    public String loadFormattedString(Reader is, ClassLoader classLoader) {
        return loadHelp(is, classLoader, true, 36, true,null);
    }

    @Override
    public String loadFormattedString(String resourcePath, ClassLoader classLoader, String defaultValue) {
        return loadHelp(resourcePath, classLoader, false, true, defaultValue);
    }


    private String loadHelp(String urlPath, ClassLoader clazz, boolean err, boolean vars, String defaultValue) {
        return loadHelp(urlPath, clazz, err, 36, vars, defaultValue);
    }

    private String loadHelp(String urlPath, ClassLoader classLoader, boolean err, int depth, boolean vars, String defaultValue) {
        if (depth <= 0) {
            throw new IllegalArgumentException("Unable to load " + urlPath + ". Too many recursions");
        }
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        if (urlPath.startsWith("/")) {
            urlPath = urlPath.substring(1);
        }
        int interr = urlPath.indexOf('?');
        String anchor = null;
        if (interr > 0) {
            anchor = urlPath.substring(interr + 1);
            urlPath = urlPath.substring(interr + 1);
        }
        URL resource = classLoader.getResource(urlPath);
        Reader in = null;
        if (resource == null) {
            if (err) {
                return "```error not found resource " + escapeText(urlPath) + "```";
            }
            if (defaultValue == null) {
                return null;
            }
            in=new StringReader(defaultValue);
        } else {
            try {
                in = new InputStreamReader(resource.openStream());
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
        try (Reader is = in) {
            return loadHelp(is, classLoader, true, depth, vars,anchor);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private String loadHelp(Reader is, ClassLoader classLoader, boolean err, int depth, boolean vars,String anchor) {
        return processHelp(CoreIOUtils.loadString(is, true), classLoader, err, depth, vars,anchor);
    }

    private String processHelp(String s, ClassLoader classLoader, boolean err, int depth, boolean vars,String anchor) {
        if (classLoader == null) {
            classLoader = Thread.currentThread().getContextClassLoader();
        }
        StringBuilder sb = new StringBuilder();
        if (s != null) {
            StringTokenizer st = new StringTokenizer(s, "\n\r", true);
            while (st.hasMoreElements()) {
                String e = st.nextToken();
                if (e.length() > 0) {
                    if (e.charAt(0) == '\n' || e.charAt(0) == '\r') {
                        sb.append(e);
                    } else if (e.startsWith("#!include<") && e.trim().endsWith(">")) {
                        e = e.trim();
                        e = e.substring("#!include<".length(), e.length() - 1);
                        sb.append(loadHelp(e, classLoader, err, depth - 1, false, "```error NOT FOUND``` <" + escapeText(e) + ">"));
                    } else {
                        sb.append(e);
                    }
                }
            }
        }
        String help = sb.toString();
        if (vars) {
            help = CoreStringUtils.replaceDollarPlaceHolders(help, pathExpansionConverter);
        }
        NutsTextNodeParser p=new DefaultNutsTextNodeParser(ws);
        NutsTextNode node = p.parse(new StringReader(help));
        if(anchor!=null){
            List<NutsTextNode> ok=new ArrayList<>();
            boolean start=false;
            if(node.getType()== NutsTextNodeType.LIST){
                for (NutsTextNode o : ((NutsTextNodeList)node)) {
                    if(start){
                        ok.add(o);
                    }else if(o.getType()==NutsTextNodeType.ANCHOR){
                        if(anchor.equals(((DefaultNutsTextNodeAnchor) o).getValue())){
                            start=true;
                        }
                    }
                }
            }
            if(start){
                node= factory().list(ok);
            }
            help= NutsTextNodeWriterStringer.toString(node,ws);
        }
        return help;
    }




    @Override
    public int textLength(String value) {
        return filterText(value).length();
    }

    /**
     * extract plain text from formatted text
     *
     * @param value value
     * @return filtered text
     */
    @Override
    public String filterText(String value) {
        return parser().filterText(value);
    }

    @Override
    public NutsTextNodeParser parser() {
        return new DefaultNutsTextNodeParser(ws);
    }

    /**
     * transform plain text to formatted text so that the result is rendered as
     * is
     *
     * @param text text
     * @return escaped text
     */    @Override
    public String escapeText(String text) {
        if (text == null) {
            return "";
        }
        NutsTextNode node = new DefaultNutsTextNodeParser(ws).parse(new StringReader(text));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriter w = new NutsTextNodeWriterStringer(out, ws)
                .setWriteConfiguration(
                        new NutsTextNodeWriteConfiguration()
                        .setFiltered(true)
                );
        w.writeNode(node);
        return out.toString();
    }

    @Override
    public String escapeCodeText(String text) {
        if (text == null) {
            return "";
        }
        //TODO...
        return text;
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence() {
        return new DefaultNutsTitleNumberSequence("");
    }

    @Override
    public NutsTitleNumberSequence createTitleNumberSequence(String pattern) {
        return new DefaultNutsTitleNumberSequence((pattern==null || pattern.isEmpty())?"1.1.1.a.1":pattern);
    }

    @Override
    public String filterText(NutsMessage value, NutsSession session) {
        return filterText(toString(value,session).toString());
    }

    public NutsString toString(Object instance, NutsSession session) {
        if(instance==null) {
            return NutsString.of("");
        }else if(instance instanceof Number || instance instanceof Date || instance instanceof Temporal) {
            //do nothing
            return NutsString.of(escapeText(String.valueOf(instance)));
        }else if(instance instanceof Throwable) {
            return NutsString.of(escapeText(CoreStringUtils.exceptionToString((Throwable) instance)));
        }else if(instance instanceof NutsString){
            return (NutsString) instance;
        }else if(instance instanceof NutsTextNode){
            return NutsString.of(
                    builder().append((NutsTextNode) instance).toString()
            );
        }else if(instance instanceof NutsFormattable){
            return _NutsFormattable_toString((NutsFormattable) instance, session);
        }else if(instance instanceof NutsMessage){
            return _NutsFormattedMessage_toString((NutsMessage) instance, session);
        }else {
            return NutsString.of(escapeText(String.valueOf(instance)));
        }
    }


    private NutsString _NutsFormattable_toString(NutsFormattable a,NutsSession session) {
        if(session==null){
            return NutsString.of(escapeText(String.valueOf(a)));
        }else{
            try {
                return  NutsString.of(a.formatter().setSession(session).format());
            }catch (Exception ex){
                return NutsString.of(escapeText(String.valueOf(a)));
            }
        }
    }
    private NutsString _NutsFormattedMessage_toString(NutsMessage m, NutsSession session) {
        if(session==null){
            throw new RuntimeException("missing session");
        }
        NutsTextFormatStyle style = m.getStyle();
        if(style==null){
            style=NutsTextFormatStyle.JSTYLE;
        }
        Object[] params = m.getParams();
        if(params==null){
            params=new Object[0];
        }
        NutsString msg = m.getMessage();
        String sLocale = session.getLocale();
        Locale locale=CoreStringUtils.isBlank(sLocale)?null:new Locale(sLocale);
        Object[] args2=new Object[params.length];
        NutsTextFormatManager text = session.getWorkspace().formats().text();
        for (int i = 0; i < args2.length; i++) {
            Object a=params[i];
            if(a instanceof Number || a instanceof Date  || a instanceof Temporal) {
                //do nothing, support format pattern
                args2[i]=a;
            }else {
                args2[i]= text.toString(a,session).toString();
            }
        }
        switch (style){
            case CSTYLE:{
                StringBuilder sb = new StringBuilder();
                new Formatter(sb, locale).format(msg.toString(), args2);
                return NutsString.of(sb.toString());
            }
            case JSTYLE:{
                return NutsString.of(MessageFormat.format(msg.toString(), args2));
            }
        }
        throw new NutsUnsupportedEnumException(session.getWorkspace(),style);
    }
}
