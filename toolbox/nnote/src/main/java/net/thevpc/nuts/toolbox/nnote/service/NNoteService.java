package net.thevpc.nuts.toolbox.nnote.service;

import net.thevpc.nuts.toolbox.nnote.service.security.NNoteObfuscator;
import net.thevpc.nuts.toolbox.nnote.service.security.NNoteObfuscatorDefault;
import net.thevpc.nuts.toolbox.nnote.service.security.PasswordHandler;
import net.thevpc.nuts.toolbox.nnote.model.CypherInfo;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import net.thevpc.common.i18n.I18n;
import net.thevpc.echo.ItemPath;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsContentType;
import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsLogVerb;
import net.thevpc.nuts.NutsStoreLocation;
import net.thevpc.nuts.toolbox.nnote.gui.util.NNoteError;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDocument;
import net.thevpc.nuts.toolbox.nnote.model.NNoteField;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObject;
import net.thevpc.nuts.toolbox.nnote.model.NNoteConfig;
import net.thevpc.nuts.toolbox.nnote.model.NNote;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteFieldDescriptor;
import net.thevpc.nuts.toolbox.nnote.model.NNoteObjectFieldType;
import net.thevpc.nuts.toolbox.nnote.model.NNoteListModel;
import net.thevpc.nuts.toolbox.nnote.model.VNNote;
import net.thevpc.nuts.toolbox.nnote.service.search.DefaultVNoteSearchFilter;
import net.thevpc.nuts.toolbox.nnote.service.search.VNNoteSearchResult;
import net.thevpc.nuts.toolbox.nnote.service.search.VNoteSearchFilter;
import net.thevpc.nuts.toolbox.nnote.service.search.strsearch.StringSearchResult;
import net.thevpc.nuts.toolbox.nnote.util.OtherUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class NNoteService {

    public static final String SECURE_ALGO = NNoteObfuscatorDefault.ID;
    private NutsApplicationContext context;
    private I18n i18n;

    public NNoteService(NutsApplicationContext context, I18n i18n) {
        this.context = context;
        this.i18n = i18n;
    }

    public NutsApplicationContext getContext() {
        return context;
    }

    public NNote shrinkNote(NNote n) {
        if (NNoteTypes.NNOTE_DOCUMENT.equals(n.getContentType())) {
            n.getChildren().clear();
        }
        for (NNote c : n.getChildren()) {
            shrinkNote(c);
        }
        return n;
    }

    public NNote expandNote(NNote n, PasswordHandler passwordHandler) {
        if (NNoteTypes.NNOTE_DOCUMENT.equals(n.getContentType())) {
            String c = n.getContent();
            c = c == null ? "" : c.trim();
            if (c.length() > 0) {
                try {
                    NNote p = loadDocument(new File(c), passwordHandler);
                    n.getChildren().clear();
                    n.getChildren().addAll(p.getChildren());
                } catch (Exception ex) {
                    n.error = new NNoteError(ex);
                }
            }
        }
        for (NNote c : n.getChildren()) {
            expandNote(c, passwordHandler);
        }
        return n;
    }

    public File getDefaultDocumentsFolder() {
        return new File(getContext().getWorkspace().locations().getStoreLocation(NutsStoreLocation.VAR));
    }

    public NNote createSampleDocumentNote() {
        NNote n = createDocumentNote();
        for (String contentType : NNoteTypes.ALL_CONTENT_TYPES) {
            NNote cc = new NNote().setName(
                    i18n.getString("NNoteTypeFamily." + contentType)
            ).setContentType(contentType);
            if (NNoteTypes.NOTE_LIST.equals(contentType)) {
                for (int i = 0; i < 5; i++) {
                    NNote cc2 = new NNote().setName(NNoteTypes.STRING).setContentType(NNoteTypes.STRING);
                    cc.getChildren().add(cc2);
                }
//                for (String contentType2 : NNoteTypes.ALL_CONTENT_TYPES) {
//                    NNote cc2 = new NNote().setName(contentType2).setContentType(contentType2);
//                    cc.getChildren().add(cc2);
//                }

            } else if (NNoteTypes.OBJECT_LIST.equals(contentType)) {
                NNoteObjectDescriptor od = new NNoteObjectDescriptor();
                od.setName("example");
                List<NNoteFieldDescriptor> fields = new ArrayList<>();
                fields.add(
                        new NNoteFieldDescriptor()
                                .setName("name of the website")
                                .setType(NNoteObjectFieldType.TEXT)
                );
                fields.add(
                        new NNoteFieldDescriptor()
                                .setName("protocol")
                                .setType(NNoteObjectFieldType.COMBOBOX)
                                .setValues(new ArrayList<>(Arrays.asList("http", "ftp")))
                );
                fields.add(
                        new NNoteFieldDescriptor()
                                .setName("description")
                                .setType(NNoteObjectFieldType.TEXTAREA)
                );
                fields.add(
                        new NNoteFieldDescriptor()
                                .setName("types")
                                .setType(NNoteObjectFieldType.CHECKBOX)
                                .setValues(new ArrayList<>(Arrays.asList("top", "down", "left", "right")))
                );
                od.setFields(fields);
                NNoteObjectDocument dd = new NNoteObjectDocument();
                dd.setDescriptor(od);
                //add some dynamic values;
                List<NNoteObject> os = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    NNoteObject d = new NNoteObject();
                    d.addField(new NNoteField().setName("name of the website").setValue("my website " + (i + 1)));
                    d.addField(new NNoteField().setName("protocol").setValue("http"));
                    d.addField(new NNoteField().setName("types").setValue("top\nright"));
                    os.add(d);
                }
                dd.setValues(os);
                cc.setContent(stringifyDescriptor(dd));
            }
            n.getChildren().add(cc);
        }
        n.getChildren().add(new NNote().setName("unknown-type").setContentType("unknown-type")
        );
        n.getChildren().add(new NNote().setName("with-icon").setIcon("star")
        );
        return n;
    }

    public NNote createDocumentNote() {
        return new NNote().setContentType(NNoteTypes.NNOTE_DOCUMENT);
    }

    public boolean isDocumentNote(NNote n) {
        return NNoteTypes.NNOTE_DOCUMENT.equals(n.getContentType());
    }

    public String stringifyNoteListInfo(NNoteListModel value) {
        return stringifyAny(value);
    }

    public String stringifyDescriptor(NNoteObjectDocument value) {
        return stringifyAny(value);
    }

    public String stringifyAny(Object value) {
        return context.getWorkspace().formats().element().setValue(value)
                .setContentType(NutsContentType.JSON)
                .setSession(context.getSession())
                .setCompact(true)
                .format();
    }

    public NNoteListModel parseNoteListModel(String s) {
        return parseAny(s, NNoteListModel.class);
    }

    public NNoteObjectDocument parseObjectDocument(String s) {
        return parseAny(s, NNoteObjectDocument.class);
    }

    public <T> T parseAny(String s, Class<T> cls) {
        if (s == null || s.trim().isEmpty()) {
            return null;
        }
        try {
            return context.getWorkspace().formats().element()
                    .setSession(context.getSession())
                    .setContentType(NutsContentType.JSON)
                    .parse(s, cls);
        } catch (Exception ex) {
            return null;
        }
    }

    public static class SaveError {

        NNote n;
        Exception error;
        ItemPath path;

        public SaveError(NNote n, ItemPath path, Exception error) {
            this.n = n;
            this.path = path;
            this.error = error;
        }

    }

    public static class SaveException extends RuntimeException {

        private List<SaveError> errors = new ArrayList<>();

        public SaveException(List<SaveError> errors, I18n i18n) {
            super(buildMessage(errors, i18n));
            this.errors = new ArrayList<>(errors);
        }

        private static String buildMessage(List<SaveError> errors, I18n i18n) {
            for (SaveError error : errors) {
                if (error.path.size() == 0) {
                    String m = error.error.getMessage();
                    if (m == null || m.length() < 3) {
                        m = error.error.toString();
                    }
                    return m;
                }
            }
            return MessageFormat.format(
                    i18n.getString("Message.saveError"), errors.size()
            );
        }

        public List<SaveError> getErrors() {
            return errors;
        }
    }

    public boolean saveDocument(NNote n, PasswordHandler handler) {
        List<SaveError> errors = new ArrayList<>();
        boolean b = saveDocument(n.copy(), ItemPath.of(), handler, errors);
        if (errors.size() > 0) {
            throw new SaveException(errors, i18n);
        }
        return b;
    }

    private boolean saveDocument(NNote n, ItemPath path, PasswordHandler passwordHandler, List<SaveError> errors) {
        List<NNote> children = new ArrayList<>(n.getChildren());
        for (NNote c : children) {
            saveDocument(c, path.child(String.valueOf(c.getName())), passwordHandler, errors);
        }
        boolean saved = false;
        if (NNoteTypes.NNOTE_DOCUMENT.equals(n.getContentType())) {
            String c = n.getContent();
            if (c != null && c.trim().length() > 0) {
                c = c.trim();
                try {
                    File f = new File(c);
                    File pf = f.getParentFile();
                    if (pf != null) {
                        pf.mkdirs();
                    }

                    if (n.getVersion() == null || n.getVersion().length() == 0) {
                        n.setVersion(context.getAppVersion().toString());
                    }
                    Instant now = Instant.now();
                    if (n.getCreationTime() == null) {
                        n.setCreationTime(now);
                    }
                    n.setLastModified(now);
                    n.setContent(null);
                    CypherInfo cypherInfo = n.getCypherInfo();
                    NNoteObfuscator obs = resolveCypherImpl(cypherInfo == null ? null : cypherInfo.getAlgo());
                    if (obs != null) {
                        n.setCypherInfo(new CypherInfo(cypherInfo.getAlgo(), ""));
                        CypherInfo ci = obs.encrypt(n, new PasswordHandler() {
                            @Override
                            public String askForPassword(String path) {
                                return passwordHandler.askForPassword(f.getPath());
                            }
                        });
                        n.setCypherInfo(ci);
                        NNote n2 = new NNote();
                        n2.setContentType(NNoteTypes.NNOTE_DOCUMENT);
                        n2.setCreationTime(n.getCreationTime());
                        n2.setLastModified(n.getLastModified());
                        n2.setCypherInfo(ci);
                        getContext().getWorkspace().formats().element()
                                .setContentType(NutsContentType.JSON)
                                .setValue(n2)
                                .setSession(context.getSession())
                                .println(f);
                    } else {
                        getContext().getWorkspace().formats().element()
                                .setContentType(NutsContentType.JSON)
                                .setValue(n)
                                .setSession(context.getSession())
                                .println(f);
                    }
//                    if (cypherInfo != null && ((cypherInfo.getAlgo() == null) ||)

                    saved = true;
                } catch (Exception ex) {
                    errors.add(new SaveError(n, path, ex));
                }
                n.setContent(c);//push back the path
            } else {
                errors.add(new SaveError(n, path, new IOException("missing file path for " + n.getName())));
            }
            if (path.size() > 0) {
                n.getChildren().clear();
            }
        }
        return saved;
    }

    public NNoteObfuscator resolveCypherImpl(String algo) {
        if (algo == null) {
            return null;
        }
        switch (algo) {
            case NNoteService.SECURE_ALGO:
                return new NNoteObfuscatorDefault(context);
        }
        return null;
    }

    public void saveConfig(NNoteConfig c) {
        if (c == null) {
            c = new NNoteConfig();
        }
        Path configFilePath = getConfigFilePath();
        File pf = configFilePath.toFile().getParentFile();
        if (pf != null) {
            pf.mkdirs();
        }
        getContext().getWorkspace().formats().element()
                .setContentType(NutsContentType.JSON)
                .setValue(c)
                .setSession(context.getSession())
                .println(configFilePath);
    }

    public NNoteConfig loadConfig() {
        return loadConfig(() -> new NNoteConfig());
    }

    public NNoteConfig loadConfig(Supplier<NNoteConfig> defaultValue) {
        try {
            NNoteConfig n = getContext().getWorkspace().formats().element()
                    .setContentType(NutsContentType.JSON).setSession(context.getSession())
                    .parse(getConfigFilePath(),
                            NNoteConfig.class);
            if (n != null) {
                return n;
            }
        } catch (Exception ex) {
            //
        }
        return defaultValue == null ? null : defaultValue.get();
    }

    public Path getConfigFilePath() {
        return Paths.get(getContext().getConfigFolder()).resolve("nnote.config");
    }

    public NNote loadDocument(File file, PasswordHandler passwordHandler) {
        NNote n = getContext().getWorkspace().formats().element()
                .setContentType(NutsContentType.JSON).setSession(context.getSession())
                .parse(file, NNote.class);
        try {
            if (!NNoteTypes.NNOTE_DOCUMENT.equals(n.getContentType())) {
                throw new IOException("Invalid content type. Expected " + NNoteTypes.NNOTE_DOCUMENT + ". got " + n.getContentType());
            }
            CypherInfo cypherInfo = n.getCypherInfo();
            NNoteObfuscator impl = resolveCypherImpl(cypherInfo == null ? null : cypherInfo.getAlgo());
            if (impl != null) {
                NNote a = impl.decrypt(cypherInfo, n, new PasswordHandler() {
                    @Override
                    public String askForPassword(String path) {
                        String hint = file.getPath();
                        return passwordHandler.askForPassword(path);
                    }
                });
                a.setCypherInfo(new CypherInfo(cypherInfo.getAlgo(), ""));
                a.setContent(file.getCanonicalPath());
                n = a;
            } else {
                n.setContent(file.getCanonicalPath());
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (n.getChildren() == null) {
            n.setChildren(new ArrayList<>());
        }
        for (NNote c : n.getChildren()) {
            expandNote(c, passwordHandler);
        }
        return n;
    }

    private static void setLenientFeature(DocumentBuilderFactory dbFactory, String s, boolean b) {
        try {
            dbFactory.setFeature(s, b);
        } catch (Exception ex) {
            //
        }
    }

    public NNote loadCherryTreeXmlFile(File file) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            boolean safe = true;
            if (safe) {
                documentFactory.setExpandEntityReferences(false);
                // This is the PRIMARY defense. If DTDs (doctypes) are disallowed, almost all XML entity attacks are prevented
                // Xerces 2 only - http://xerces.apache.org/xerces2-j/features.html#disallow-doctype-decl
                setLenientFeature(documentFactory, "http://apache.org/xml/features/disallow-doctype-decl", true);

                // If you can't completely disable DTDs, then at least do the following:
                // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-general-entities
                // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-general-entities
                // JDK7+ - http://xml.org/sax/features/external-general-entities
                setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-general-entities", false);
                setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-general-entities", false);
                setLenientFeature(documentFactory, "http://xml.org/sax/features/external-general-entities", false);

                // Xerces 1 - http://xerces.apache.org/xerces-j/features.html#external-parameter-entities
                // Xerces 2 - http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities
                // JDK7+ - http://xml.org/sax/features/external-parameter-entities
                setLenientFeature(documentFactory, "http://xerces.apache.org/xerces-j/features.html#external-parameter-entities", false);
                setLenientFeature(documentFactory, "http://xml.org/sax/features/external-parameter-entities", false);
                setLenientFeature(documentFactory, "http://xerces.apache.org/xerces2-j/features.html#external-parameter-entities", false);

                // Disable external DTDs as well
                setLenientFeature(documentFactory, "http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
                // and these as well, per Timothy Morgan's 2014 paper: "XML Schema, DTD, and Entity Attacks"
                documentFactory.setXIncludeAware(false);
                documentFactory.setValidating(false);
            }
            DocumentBuilder b;
            try {
                b = documentFactory.newDocumentBuilder();
            } catch (ParserConfigurationException ex) {
                throw new NutsIOException(context.getSession().getWorkspace(), ex);
            }

            b.setErrorHandler(new ErrorHandler() {
                @Override
                public void warning(SAXParseException exception) throws SAXException {
                    context.getSession().getWorkspace().log().of(NNoteService.class).with().session(context.getSession())
                            .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
                }

                @Override
                public void error(SAXParseException exception) throws SAXException {
                    context.getSession().getWorkspace().log().of(NNoteService.class).with().session(context.getSession())
                            .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
                }

                @Override
                public void fatalError(SAXParseException exception) throws SAXException {
                    context.getSession().getWorkspace().log().of(NNoteService.class).with().session(context.getSession())
                            .level(Level.FINEST).verb(NutsLogVerb.WARNING).log(exception.toString());
                }
            });

            Document doc = b.parse(file);
            NodeList childNodes = doc.getDocumentElement().getChildNodes();
            NNote cherryNNoteDocument = NNote.newDocument();
            for (int i = 0; i < childNodes.getLength(); i++) {
                Node n = childNodes.item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;
//                    if (e.getTagName().equals("node") || e.getTagName().equals("rich_text")) {
                    NNote a = parseCherryTreeXmlNote(e);
                    if (a != null) {
                        cherryNNoteDocument.getChildren().add(a);
                    }
//                    }
                }
            }
            return cherryNNoteDocument;
        } catch (SAXException ex) {
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    static class RichText {

        Map<String, String> style = new HashMap<>();
        String text;
        boolean escaped;

        public boolean isEscaped() {
            return escaped;
        }

        public RichText setEscaped(boolean escaped) {
            this.escaped = escaped;
            return this;
        }

        public Map<String, String> getStyle() {
            return style;
        }

        public RichText setStyle(Map<String, String> style) {
            this.style = style;
            return this;
        }

        public RichText addStyle(String k, String v) {
            if (!OtherUtils.isBlank(v)) {
                this.style.put(k, v);
            }
            return this;
        }

        public String getText() {
            return text;
        }

        public RichText setText(String text) {
            this.text = text;
            return this;
        }

    }

    public NNote parseCherryTreeXmlNote(org.w3c.dom.Element e) {
        switch (e.getTagName()) {
            case "node": {
                NNote nn = new NNote();
                nn.setContent(NNoteTypes.HTML);
                //custom_icon_id="0" foreground="" is_bold="False" name="commandes et factures" prog_lang="custom-colors" readonly="False" tags="" ts_creation="0.0" ts_lastsave="0.0" unique_id="5"
                //custom_icon_id="0" foreground="" is_bold="False" prog_lang="custom-colors" readonly="False" ts_creation="0.0" ts_lastsave="0.0" unique_id="5"
                NamedNodeMap attrs = e.getAttributes();
                Map<String, String> noteContentStyle = new HashMap<>();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node attr = attrs.item(i);
                    if (attr instanceof Attr) {
                        Attr a = (Attr) attr;
                        String k = a.getName();
                        String v = a.getValue();
                        if (!OtherUtils.isBlank(k)) {
                            switch (k) {
                                case "name": {
                                    nn.setName(v);
                                    break;
                                }
                                case "tags": {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.setTags(
                                                Arrays.asList(
                                                        v.split("[ ,;:]")
                                                ).stream().filter(x -> x.length() > 0)
                                                        .collect(Collectors.toSet())
                                        );
                                    }
                                    break;
                                }
                                case "custom_icon_id": {
                                    if (!OtherUtils.isBlank(v)) {
                                        int x = 0;
                                        try {
                                            x = Integer.parseInt(v);
                                        } catch (Exception ex) {
                                            //
                                        }
                                        if (x > 0) {
                                            x = (x - 1) % NNoteTypes.ALL_USER_ICONS.size();
                                            nn.setIcon((String) (NNoteTypes.ALL_USER_ICONS.toArray()[x]));
                                        }
                                    }
                                    break;
                                }
                                case "foregournd": {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.setTitleForeground(v);
                                    }
                                    break;
                                }
                                case "background": {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.setTitleBackground(v);
                                    }
                                    break;
                                }
                                case "readonly": {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.setReadOnly(Boolean.parseBoolean(v));
                                    }
                                    break;
                                }
                                case "is_bold": {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.setTitleBold(Boolean.parseBoolean(v));
                                    }
                                    break;
                                }
                                case "ts_creation": {
                                    if (!OtherUtils.isBlank(v)) {
                                        double d = 0;
                                        try {
                                            d = Double.parseDouble(v);
                                        } catch (Exception ex) {
                                            //ignore
                                        }
                                        if (d != 0) {
                                            long ln = Double.doubleToLongBits(d);
                                            try {
                                                nn.setCreationTime(Instant.ofEpochMilli(ln));
                                            } catch (Exception ex) {
                                                //ignore
                                            }
                                        }
                                        nn.setTitleBold(Boolean.parseBoolean(v));
                                    }
                                    break;
                                }
                                case "ts_lastsave": {
                                    if (!OtherUtils.isBlank(v)) {
                                        double d = 0;
                                        try {
                                            d = Double.parseDouble(v);
                                        } catch (Exception ex) {
                                            //ignore
                                        }
                                        if (d != 0) {
                                            long ln = Double.doubleToLongBits(d);
                                            try {
                                                nn.setLastModified(Instant.ofEpochMilli(ln));
                                            } catch (Exception ex) {
                                                //ignore
                                            }
                                        }
                                        nn.setTitleBold(Boolean.parseBoolean(v));
                                    }
                                    break;
                                }
                                case "prog_lang": {
                                    if (!OtherUtils.isBlank(v)) {
                                        switch (v) {
                                            case "java": {
                                                nn.setContentType(NNoteTypes.JAVA);
                                                break;
                                            }
                                            case "c": {
                                                nn.setContentType(NNoteTypes.C);
                                                break;
                                            }
                                            case "cpp": {
                                                nn.setContentType(NNoteTypes.CPP);
                                                break;
                                            }
                                            case "custom-colors": {
                                                nn.setContentType(NNoteTypes.HTML);
                                                break;
                                            }
                                            default: {
                                                nn.setContentType(NNoteTypes.PLAIN);
                                                break;
                                            }
                                        }
                                    }
                                    break;
                                }
                                default: {
                                    if (!OtherUtils.isBlank(v)) {
                                        nn.getProperties().put(k, v);
                                    }
                                }
                            }
                        }
                    }
                }
                List<RichText> richTexts = new ArrayList<>();
                NodeList childNodes = e.getChildNodes();
                for (int i = 0; i < childNodes.getLength(); i++) {
                    Node c = childNodes.item(i);
                    if (c instanceof Element) {
                        Element e2 = (Element) c;
                        if (e2.getTagName().equals("node")) {
                            nn.getChildren().add(parseCherryTreeXmlNote(e2));
                        } else if (e2.getTagName().equals("rich_text")) {
                            String link = e.getAttribute("link");
                            if (!OtherUtils.isBlank(link)) {
                                richTexts.add(
                                        new RichText()
                                                .addStyle("foreground", e2.getAttribute("foreground"))
                                                .addStyle("background", e2.getAttribute("background"))
                                                .setText(
                                                        "<a href='" + link + "'>"
                                                        + OtherUtils.escapeHtml(e.getTextContent())
                                                        + "/>"
                                                ).setEscaped(true)
                                );
                            } else {
                                richTexts.add(
                                        new RichText()
                                                .addStyle("foreground", e2.getAttribute("foreground"))
                                                .addStyle("background", e2.getAttribute("background"))
                                                .setText(e.getTextContent())
                                );
                            }

                        }
                    }
                }
                if (nn.getContentType().equals(NNoteTypes.HTML)) {
                    nn.setContent(
                            "<html><head>"
                            + (noteContentStyle.isEmpty() ? "" : ("<style>" + buildStyle(noteContentStyle) + "</style>"))
                            + "</head><body>"
                            + richTexts.stream().map(x -> {
                                String t = x.getText();
                                if (!t.isEmpty()) {
                                    if (x.style.isEmpty()) {
                                        return x.isEscaped() ? t
                                                : ("<pre>"
                                                + OtherUtils.escapeHtml(t)
                                                + "</pre>");
                                    } else {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append("<div style='");
                                        sb.append(buildStyle(x.style));
                                        sb.append("'>");
                                        sb.append(
                                                x.isEscaped() ? t
                                                : ("<pre>"
                                                + OtherUtils.escapeHtml(t)
                                                + "</pre>")
                                        );
                                        sb.append("</div>");
                                        return sb;
                                    }
                                }
                                return t;
                            }).collect(Collectors.joining())
                            + "</body></html>"
                    );
                } else {
                    nn.setContent(
                            richTexts.stream().map(x -> x.getText()).collect(Collectors.joining())
                    );
                }
                return nn;
            }
            default: {
//                System.out.println("ignored");
            }
        }
        return null;
    }

    private String buildStyle(Map<String, String> s) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : s.entrySet()) {
            sb.append(entry.getKey());
            sb.append(" : ");
            sb.append(entry.getValue());
            sb.append("; ");
        }
        return sb.toString();
    }

    public VNNoteSearchResult search(VNNote n, String query) {
        return search(n, new DefaultVNoteSearchFilter(query));
    }

    public VNNoteSearchResult search(VNNote n, VNoteSearchFilter filter) {
        Stream<StringSearchResult<VNNote>> curr = Stream.of();

        List<StringSearchResult<VNNote>> all = new ArrayList<>();
        if (filter != null) {
            Stream<StringSearchResult<VNNote>> r = filter.search(n, this);
            if (r != null) {
                curr = Stream.concat(curr, r);
            }
        }
        for (VNNote vNNote : n.getChildren()) {
            curr = Stream.concat(curr, search(vNNote, filter).stream());
        }
        return new VNNoteSearchResult(n, curr);
    }

    public void updateNoteProperties(VNNote toUpdate, NNote headerValues) {
        String oldName = toUpdate.getName();
        toUpdate.setName(headerValues.getName());
        toUpdate.setIcon(headerValues.getIcon());
        toUpdate.setReadOnly(headerValues.isReadOnly());
        toUpdate.setTitleForeground(headerValues.getTitleForeground());
        toUpdate.setTitleBackground(headerValues.getTitleBackground());
        toUpdate.setTitleBold(headerValues.isTitleBold());
        toUpdate.setTitleItalic(headerValues.isTitleItalic());
        toUpdate.setTitleUnderlined(headerValues.isTitleUnderlined());
        toUpdate.setTitleStriked(headerValues.isTitleStriked());
        prepareChildForInsertion(toUpdate.getParent(), toUpdate);
        String newName = toUpdate.getName();
        if (NNoteTypes.NOTE_LIST.equals(toUpdate.getParent().getContentType())) {
            NNoteListModel oldModel = parseNoteListModel(toUpdate.getParent().getContent());
            if (oldModel == null) {
                oldModel = new NNoteListModel();
            }
            if (oldModel.getSelectedNames().contains(oldName)) {
                oldModel.getSelectedNames().remove(oldName);
                oldModel.getSelectedNames().add(newName);
                toUpdate.getParent().setContent(stringifyNoteListInfo(oldModel));
            }
        }
    }

    public String prepareChildForInsertion(VNNote parent, VNNote child) {
        String name = child.getName();
        String contentType = child.getContentType();
        if (name == null) {
            name = "";
        }
        Pattern p = Pattern.compile("^(?<base>.*) [0-9]$");
        Matcher m = p.matcher(name);
        String base = name;
        if (m.find()) {
            base = m.group("base");
        }
        contentType = NNoteTypes.normalizeContentType(contentType);
        if (base.isEmpty()) {
            base = i18n.getString("NNoteTypeFamily." + contentType);
        }
        Set<String> existingNames = parent.getChildren() == null ? new HashSet<>()
                : parent.getChildren().stream()
                        .filter(x -> x != child) // !!!
                        .map(x -> x.getName() == null ? "" : x.getName())
                        .collect(Collectors.toSet());
        int i = 1;
        while (true) {
            String n = (i == 1) ? base : base + (" " + i);
            if (!existingNames.contains(n)) {
                return n;
            }
            i++;
        }
    }

    public String generateNewChildName(NNote note, String name, String contentType) {
        if (name == null) {
            name = "";
        }
        Pattern p = Pattern.compile("^(?<base>.*) [0-9]$");
        Matcher m = p.matcher(name);
        String base = name;
        if (m.find()) {
            base = m.group("base");
        }
        contentType = NNoteTypes.normalizeContentType(contentType);
        if (base.isEmpty()) {
            base = i18n.getString("NNoteTypeFamily." + contentType);
        }
        Set<String> existingNames = note.getChildren() == null ? new HashSet<>()
                : note.getChildren().stream().map(x -> x.getName() == null ? "" : x.getName())
                        .collect(Collectors.toSet());
        int i = 1;
        while (true) {
            String n = (i == 1) ? base : base + (" " + i);
            if (!existingNames.contains(n)) {
                return n;
            }
            i++;
        }
    }

}
