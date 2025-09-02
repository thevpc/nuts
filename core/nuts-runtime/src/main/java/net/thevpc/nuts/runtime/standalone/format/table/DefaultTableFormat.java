/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.format.table;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NPairElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.format.DefaultFormatBase;
import net.thevpc.nuts.runtime.standalone.text.art.table.DefaultNTextArtTableRenderer;
import net.thevpc.nuts.runtime.standalone.text.util.NTextUtils;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NMsg;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by vpc on 2/17/17.
 */
public class DefaultTableFormat extends DefaultFormatBase<NTableFormat> implements NTableFormat {

    private Object model;
    private DefaultNTextArtTableRenderer helper = new DefaultNTextArtTableRenderer();

    public DefaultTableFormat(NWorkspace workspace) {
        super("table-format");
    }


    @Override
    public NTableModel getModel() {
        return createTableModel(model);
    }

    @Override
    public NTableFormat setValue(Object value) {
        this.model = value;
        return this;
    }


    @Override
    public void print(NPrintStream w) {
        NPrintStream out = getValidPrintStream(w);
        out.print(helper.render(getModel()));
        out.flush();
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        OutputStreamWriter w = new OutputStreamWriter(out);
        print(w);
        try {
            w.flush();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
        return out.toString();
    }


    private static class SimpleRow {
        List<SimpleCell> cells = new ArrayList<>();
    }

    private static class SimpleCell {
        private String title;
        private NText value;

        public SimpleCell(String title, NText value) {
            this.title = title;
            this.value = value;
        }
    }

    private NTableModel createTableModel(Object o) {
        if (o == null) {
            return NMutableTableModel.of();
        }
        if (o instanceof NTableModel) {
            return (NTableModel) o;
        }
        if (o instanceof String || o instanceof Number || o instanceof Date || o instanceof Temporal || o instanceof Path || o instanceof File) {
            List<NElement> a = new ArrayList<>();
            a.add(NElements.of().toElement(o));
            return createTableModel(NElements.of().toElement(a));
        }
        o = NElements.of().destruct(o);
        if (o instanceof Collection) {
            return _model2(o);
        }
        if (o instanceof Map) {
            NMutableTableModel model = NMutableTableModel.of();
            LinkedHashSet<String> columns = new LinkedHashSet<>();
            columns.add("Name");
            columns.add("Value");
//            resolveColumns(o, columns);
            for (String column : columns) {
                model.addHeaderCell(NText.of(column));
            }
            for (Map.Entry<Object, Object> eoelem2 : ((Map<Object, Object>) o).entrySet()) {
                model.newRow();
                model.addCell(formatObject(eoelem2.getKey()));
                model.addCell(formatObject(eoelem2.getValue()));
            }
            return model;
        }
        if (
                (o instanceof NMsg)
                        || (o instanceof NText)
                        || (o instanceof NFormattable)
        ) {
            NMutableTableModel model = NMutableTableModel.of();
            model.newRow();
            model.addCell(formatObject(o));
            return model;
        }
        if (!(o instanceof NElement)) {
            return createTableModel(NElements.of().toElement(o));
        }
        NElement elem = (NElement) o;
        switch (elem.type()) {
            case BOOLEAN:
            case INSTANT:
            case DOUBLE_QUOTED_STRING:
            case SINGLE_QUOTED_STRING:
            case ANTI_QUOTED_STRING:
            case TRIPLE_DOUBLE_QUOTED_STRING:
            case TRIPLE_SINGLE_QUOTED_STRING:
            case TRIPLE_ANTI_QUOTED_STRING:
            case LINE_STRING:
//            case NUTS_STRING:
            case INT:
            case FLOAT:
            case NULL: {
                List<NElement> a = new ArrayList<>();
                a.add(elem);
                return createTableModel(NElements.of().toElement(a));
            }
            case OBJECT: {
                return createTableModel(NElements.of().toElement(elem.asObject().get().children()));
            }
            case ARRAY: {

                return _model2(elem);
            }
            default: {
                throw new NUnsupportedArgumentException(NMsg.ofC("unsupported %s", elem.type()));
            }
        }
    }

    public NMutableTableModel _model2(Object obj) {
        NMutableTableModel model = NMutableTableModel.of();
        List<SimpleRow> rows = resolveColumnsFromRows(obj);
        List<String> titles = new ArrayList<>();
        Set<String> titlesSet = new HashSet<>();
        if (rows.size() > 0) {
            titles.addAll(rows.get(0).cells.stream().map(x -> x.title).collect(Collectors.toList()));
            titlesSet.addAll(titles);
        }
        for (SimpleRow row : rows) {
            for (SimpleCell cell : row.cells) {
                if (!titlesSet.contains(cell.title)) {
                    titlesSet.add(cell.title);
                    titles.add(cell.title);
                }
            }
        }
        for (String column : titles) {
            model.addHeaderCell(NText.of(column));
        }
        for (SimpleRow row : rows) {
            model.newRow();
            Boolean[] visited = new Boolean[row.cells.size()];
            for (String title : titles) {
                List<SimpleCell> cells = row.cells;
                for (int i = 0; i < cells.size(); i++) {
                    SimpleCell cell = cells.get(i);
                    if (visited[i] == null) {
                        if (Objects.equals(cell.title, title)) {
                            if (cell.value != null) {
                                model.addCell(formatObject(cell.value));
                            } else {
                                model.addCell(NText.of(""));
                            }
                            visited[i] = true;
                            break;
                        }
                    }
                }
            }
        }
        return model;
    }

    public List<SimpleRow> resolveColumnsFromRows(Object obj) {
        List<SimpleRow> rows = new ArrayList<>();
        if (obj instanceof NElement) {
            NElement value = (NElement) obj;
            switch (value.type()) {
                case ARRAY: {
                    for (NElement value2 : value.asArray().get().children()) {
                        rows.add(resolveColumnsFromRow(value2));
                    }
                    break;
                }
                default: {
                    rows.add(resolveColumnsFromRow(value));
                    break;
                }
            }
        } else if (obj instanceof Collection) {
            for (Object value2 : ((Collection) obj)) {
                rows.add(resolveColumnsFromRow(value2));
            }
        } else {
            rows.add(resolveColumnsFromRow(obj));
        }
        return rows;
    }

    public SimpleRow resolveColumnsFromRow(Object obj) {
        if (obj instanceof NElement) {
            NElement value = (NElement) obj;
            switch (value.type()) {
                case OBJECT: {
                    SimpleRow e = new SimpleRow();
                    int column = 1;
                    for (NElement ne : value.asObject().get().children()) {
                        if (ne instanceof NPairElement) {
                            NPairElement nee = (NPairElement) ne;
                            NElement k = nee.key();
                            if (!k.isString()) {
                                k = NElement.ofString(
                                        k.toString()
                                );
                            }
                            e.cells.add(resolveColumnsFromCell(k.asStringValue().get(), nee.value()));
                        } else {
                            e.cells.add(resolveColumnsFromCell("COL " + column, ne));
                        }
                        column++;
                    }
                    return e;
                }
                case ARRAY: {
                    SimpleRow e = new SimpleRow();
                    int column = 1;
                    for (NElement value2 : value.asArray().get().children()) {
                        e.cells.add(resolveColumnsFromCell("COL " + column, value2));
                        column++;
                    }
                    return e;
                }
                default: {
                    SimpleRow e = new SimpleRow();
                    e.cells.add(resolveColumnsFromCell("value", value));
                    return e;
                }
            }
        } else if (obj instanceof Collection) {
            SimpleRow e = new SimpleRow();
            int column = 1;
            for (Object value2 : ((Collection) obj)) {
                e.cells.add(resolveColumnsFromCell("COL " + column, value2));
                column++;
            }
            return e;
        } else if (obj instanceof Map) {
            Map<String, Object> m = new HashMap<>();
            Map<Object, Object> omap = (Map<Object, Object>) obj;
            SimpleRow e = new SimpleRow();
            for (Map.Entry<Object, Object> vv : omap.entrySet()) {
                String k = String.valueOf(vv.getKey());
                m.put(k, formatObject(vv.getValue()));
                e.cells.add(resolveColumnsFromCell(k, vv.getValue()));
            }
            return e;
        } else {
            SimpleRow e = new SimpleRow();
            e.cells.add(resolveColumnsFromCell("value", obj));
            return e;
        }
    }

    public SimpleCell resolveColumnsFromCell(String title, Object obj) {
        return new SimpleCell(title, obj == null ? null : formatObject(obj));
    }

    @Override
    public Object getValue() {
        return model;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg a;
        if ((a = cmdLine.nextFlag("--no-header").orNull()) != null) {
            boolean val = a.getBooleanValue().get();
            if (a.isUncommented()) {
                helper.setVisibleHeader(!val);
            }
            return true;
        } else if ((a = cmdLine.nextFlag("--header").orNull()) != null) {
            boolean val = a.getBooleanValue().get();
            if (a.isUncommented()) {
                helper.setVisibleHeader(val);
            }
            return true;
        } else if ((a = cmdLine.nextEntry("--border").orNull()) != null) {
            if (a.isUncommented()) {
                helper.setBorder(a.getValue().asString().orElse(""));
            }
            return true;
        } else if (cmdLine.hasNext() && cmdLine.isNextOption()) {
            int cc = getModel().getColumnsCount();

            Map<String, Integer> columns = new HashMap<>();
            for (int i = 0; i < cc; i++) {
                Object v = getModel().getHeaderValue(cc);
                if (v instanceof String) {
                    columns.put(v.toString().toLowerCase(), i);
                }
            }
            NArg a2 = null;
            for (Map.Entry<String, Integer> e : columns.entrySet()) {
                if ((a2 = cmdLine.next("--" + e.getKey()).orNull()) != null) {
                    if (a2.isUncommented()) {
                        helper.setVisibleColumn(e.getValue(), true);
                    }
                    return true;
                } else if ((a2 = cmdLine.next("--no-" + e.getKey()).orNull()) != null) {
                    if (a2.isUncommented()) {
                        helper.setVisibleColumn(e.getValue(), false);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    private NText formatObject(Object any) {
        return NTextUtils.stringValueFormatted(any, false);
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }


}
