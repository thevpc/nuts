/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.core.format;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsOutputFormat;
import net.vpc.app.nuts.NutsTableFormat;
import net.vpc.app.nuts.NutsTreeFormat;
import net.vpc.app.nuts.NutsTreeModel;
import net.vpc.app.nuts.NutsUnsupportedArgumentException;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import org.w3c.dom.Document;

/**
 *
 * @author vpc
 */
public class MapStringObjectOutputFormatWriter extends NutsOutputFormatWriterBase {

    public static final String OPTION_MULTILINE_PROPERTY = "--multiline-property";

    final NutsOutputFormat t;
    final NutsWorkspace ws;
    final Map<Object, Object> data;
    private String rootName = "";
    private Map<String, String> multilineProperties = new HashMap<>();
    private List<String> extraConfig = new ArrayList<>();

    public MapStringObjectOutputFormatWriter(NutsOutputFormat t, NutsWorkspace ws, Map<Object, Object> data) {
        this.t = t == null ? NutsOutputFormat.PLAIN : t;
        this.ws = ws;
        this.data = data;
    }

    public MapStringObjectOutputFormatWriter addMultilineProperty(String property, String separator) {
        multilineProperties.put(property, separator);
        return this;
    }

    @Override
    public boolean configureFirst(NutsCommandLine commandLine) {
        extraConfig.addAll(Arrays.asList(commandLine.getArgs()));
        NutsArgument a;
        if ((a = commandLine.readStringOption(OPTION_MULTILINE_PROPERTY)) != null) {
            NutsArgument i = a.getValue();
            addMultilineProperty(i.getKey().getString(), i.getValue().getString());
            return true;
        }
        return false;
    }

    @Override
    public void write(Writer w) {
        switch (t) {
            case PLAIN: {
                PrintWriter out = (w instanceof PrintWriter) ? ((PrintWriter) w) : new PrintWriter(w);
                printMap(out, "", (Map) ObjectOutputFormatWriterHelper.explodeMap(data));
                break;
            }
            case JSON: {
                ws.io().json().pretty().write(data, w);
                break;
            }
            case PROPS: {
                CoreIOUtils.storeProperties(ObjectOutputFormatWriterHelper.explodeMap(data), w);
                break;
            }
            case TABLE: {
                NutsTableFormat t = ws.formatter().createTableFormat();
                t.configure(ws.parser().parseCommandLine(extraConfig), true);
                t.addHeaderCells("==Name==", "==Value=");
                for (Map.Entry<String, String> entry : ObjectOutputFormatWriterHelper.indentMap(data, "").entrySet()) {
                    t.newRow();
                    t.addCells(ObjectOutputFormatWriterHelper.stringValue(entry.getKey()), ObjectOutputFormatWriterHelper.stringValue(entry.getValue()));
                }
                t.print(w);
                break;
            }
            case TREE: {
                NutsTreeFormat t = ws.formatter().createTreeFormat();
                t.configure(ws.parser().parseCommandLine(extraConfig), true);

                class XNode {

                    String name;
                    Object value;

                    public XNode(String name, Object value) {
                        this.name = name;
                        this.value = value;
                    }

                    @Override
                    public String toString() {
                        if (value instanceof Map) {
                            return ws.parser().escapeText(ObjectOutputFormatWriterHelper.stringValue(name));
                        } else if (value instanceof Collection) {
                            return ws.parser().escapeText(ObjectOutputFormatWriterHelper.stringValue(name));
                        } else {
                            return "##" + ws.parser().escapeText(ObjectOutputFormatWriterHelper.stringValue(name)) + "##" + "\\=" + ws.parser().escapeText(ObjectOutputFormatWriterHelper.stringValue(value));
                        }
                    }
                }
                XNode root = new XNode(rootName, data);
                t.setTree(new NutsTreeModel() {
                    @Override
                    public Object getRoot() {
                        return root;
                    }

                    @Override
                    public List getChildren(Object o) {
                        XNode t = (XNode) o;
                        if (t.value instanceof Map) {
                            List<XNode> all = new ArrayList<>();
                            for (Map.Entry<Object, Object> me : ((Map<Object, Object>) t.value).entrySet()) {
                                all.add(new XNode(ObjectOutputFormatWriterHelper.stringValue(me.getKey()), me.getValue()));
                            }
                            return all;
                        } else if (t.value instanceof Collection) {
                            List<XNode> all = new ArrayList<>();
                            for (Object me : ((Collection) t.value)) {
                                all.add(new XNode(ObjectOutputFormatWriterHelper.stringValue(me), me));
                            }
                            return all;
                        } else {
                            return null;
                        }
                    }
                });
                t.print(w);
                break;
            }
            case XML: {
                try {
                    DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
                    Document document = documentBuilder.newDocument();
                    document.appendChild(ObjectOutputFormatWriterHelper.createElement(CoreStringUtils.isBlank(rootName) ? "root" : rootName, data, document));
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource domSource = new DOMSource(document);
                    StreamResult streamResult = new StreamResult(w);
                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty(OutputKeys.STANDALONE, "false");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
                    transformer.transform(domSource, streamResult);
                } catch (ParserConfigurationException | TransformerException ex) {
                    throw new UncheckedIOException(new IOException(ex));
                }
                break;
            }
            default: {
                throw new NutsUnsupportedArgumentException("Unsupported " + t);
            }
        }
    }

    private void printMap(PrintWriter out, String prefix, Map<String, Object> props) {
        int len = 23;
        for (String extraKey : props.keySet()) {
            int x = ws.parser().escapeText(extraKey).length();
            if (x > len) {
                len = x;
            }
        }
        boolean first = true;
        for (Map.Entry<String, Object> e : props.entrySet()) {
            if (first) {
                first = false;
            } else {
                out.print("\n");
            }
            printKeyValue(out, prefix, len, e.getKey(), "" + e.getValue());
        }
    }

    private void printKeyValue(PrintWriter out, String prefix, int len, String key, String value) {
        boolean requireFancy = false;
        String sep = multilineProperties.get(key);
        if (sep != null && sep.length() == 0) {
            sep = ":|;";
        }
        //            String fancySep = ":";
        //            switch (key) {
        //                case "nuts-runtime-path": {
        //                    requireFancy = true;
        //                    fancySep = ":|;";
        //                    break;
        //                }
        //                case "nuts-boot-runtime-path": {
        //                    requireFancy = true;
        //                    fancySep = ":|;";
        //                    break;
        //                }
        //                case "java.class.path":
        //                case "java-class-path": {
        //                    requireFancy = true;
        //                    fancySep = File.pathSeparator;
        //                    break;
        //                }
        //                case "java.library.path":
        //                case "java-library-path": {
        //                    requireFancy = true;
        //                    fancySep = File.pathSeparator;
        //                    break;
        //                }
        //            }
        printKeyValue(out, prefix, len, sep, key, value);
    }

    private void printKeyValue(PrintWriter out, String prefix, int len, String fancySep, String key, String value) {
        if (prefix == null) {
            prefix = "";
        }
        if (fancySep != null) {
            String space = prefix + CoreStringUtils.alignLeft("", len + 3) + "[[%s]]";
            String[] split = value.split(fancySep);
            if (split.length == 0) {
                out.print(prefix + CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : ");
            } else {
                for (int i = 0; i < split.length; i++) {
                    String s = split[i];
                    if (i == 0) {
                        out.printf(prefix + CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : [[%s]]", s);
                    } else {
                        out.print("\n");
                        out.printf(space, s);
                    }
                    //                    }
                }
            }
        } else {
            out.printf(prefix + CoreStringUtils.alignLeft(key, len - key.length() + ws.parser().escapeText(key).length()) + " : [[%s]]", value);
        }
    }

}
