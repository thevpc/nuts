/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.lib.md.util.MdUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author thevpc
 */
public class MdElementTransformBase {

    protected MdElement[][] transformArray(MdElement[][] e, MdElementPath parentPath) {
        List<MdElement[]> all = new ArrayList<>();
        for (MdElement[] mdElement : e) {
            MdElement[] u = transformArray(mdElement, parentPath);
            if (u != null) {
                all.add(u);
            }
        }
        if (all.size() == 0) {
            return null;
        }
        return all.toArray(new MdElement[0][]);
    }

    protected MdElement[] transformArray(MdElement[] e, MdElementPath parentPath) {
        List<MdElement> all = new ArrayList<>();
        for (MdElement mdElement : e) {
            MdElement u = transformElement(parentPath.append(mdElement));
            if (u != null) {
                all.add(u);
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        return all.toArray(new MdElement[0]);
    }

    protected MdColumn[] transformColumns(MdColumn[] e, MdElementPath parentPath) {
        List<MdColumn> all = new ArrayList<>();
        for (MdElement mdElement : e) {
            MdElement c = transformElement(parentPath.append(mdElement));
            if (c != null) {
                MdColumn u = (MdColumn) c;
                all.add(u);
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        return all.toArray(new MdColumn[0]);
    }

    protected MdRow[] transformRows(MdRow[] e, MdElementPath parentPath) {
        List<MdRow> all = new ArrayList<>();
        for (MdElement mdElement : e) {
            MdElement c = transformElement(parentPath.append(mdElement));
            if (c != null) {
                MdRow u = (MdRow) c;
                all.add(u);
            }
        }
        if (all.isEmpty()) {
            return null;
        }
        return all.toArray(new MdRow[0]);
    }

    public MdElement transformDocument(MdElement e) {
        return transformElement(MdElementPath.ROOT.append(e));
    }

    public MdElement transformElement(MdElementPath path) {
        MdElement e = path.getElement();
        if (e == null) {
            return null;
        }
        switch (e.type().group()) {
            case XML: {
                return transformXml((MdElementPath<MdXml>) path);
            }
            case TITLE: {
                return transformTitle((MdElementPath<MdTitle>) path);
            }
            case NUMBERED_ITEM: {
                return transformNumberedItem((MdElementPath<MdNumberedItem>) path);
            }
            case UNNUMBERED_ITEM: {
                return transformUnNumberedItem((MdElementPath<MdUnNumberedItem>) path);
            }
            case ADMONITION: {
                return transformAdmonition((MdElementPath<MdAdmonition>) path);
            }
            case TEXT: {
                return transformText((MdElementPath<MdText>) path);
            }
            case CODE: {
                return transformCode((MdElementPath<MdCode>) path);
            }
            case HORIZONTAL_RULE: {
                return transformHorizontalRule((MdElementPath<MdHr>) path);
            }
            case TABLE: {
                return transformTable((MdElementPath<MdTable>) path);
            }
            case BOLD: {
                return transformBold((MdElementPath<MdBold>) path);
            }
            case ITALIC: {
                return transformItalic((MdElementPath<MdItalic>) path);
            }
            case IMAGE: {
                return transformImage((MdElementPath<MdImage>) path);
            }
            case LINK: {
                return transformURL((MdElementPath<MdLink>) path);
            }
            case COLUMN: {
                return transformColumn((MdElementPath<MdColumn>) path);
            }
            case ROW: {
                return transformRow((MdElementPath<MdRow>) path);
            }
            case CODE_LINK:{
                return transformLink((MdElementPath<MdRow>) path);
            }
            case LINE_BREAK:{
                return transformLineBreak((MdElementPath<MdBr>) path);
            }
            case BODY:{
                return transformBody((MdElementPath<MdBody>) path);
            }
            case PHRASE:{
                return transformPhrase((MdElementPath<MdPhrase>) path);
            }
            case NUMBERED_LIST:{
                return transformNumberedList((MdElementPath<MdNumberedList>) path);
            }
            case UNNUMBERED_LIST:{
                return transformUnNumberedList((MdElementPath<MdUnNumberedList>) path);
            }
        }
        return e;
    }

    protected MdElement transformUnNumberedList(MdElementPath<MdUnNumberedList> path) {
        return path.getElement();
    }

    protected MdElement transformNumberedList(MdElementPath<MdNumberedList> path) {
        return path.getElement();
    }

    protected MdElement transformPhrase(MdElementPath<MdPhrase> path) {
        return path.getElement();
    }

    protected MdElement transformBody(MdElementPath<MdBody> path) {
        return path.getElement();
    }

    protected MdElement transformLineBreak(MdElementPath<MdBr> path) {
        return path.getElement();
    }

    protected MdElement transformLink(MdElementPath<MdRow> path) {
        return path.getElement();
    }

    protected MdTable transformTable(MdElementPath<MdTable> path) {
        MdColumn[] h = transformColumns(path.getElement().getColumns(), path);
        MdRow[] r = transformRows(path.getElement().getRows(), path);
        return new MdTable(h, r);
    }

    protected MdElement transformHorizontalRule(MdElementPath<MdHr> path) {
        return path.getElement();
    }

    protected MdElement transformCode(MdElementPath<MdCode> path) {
        return path.getElement();
    }

    protected MdElement transformText(MdElementPath<MdText> path) {
        return path.getElement();
    }

    protected MdElement transformAdmonition(MdElementPath<MdAdmonition> path) {
        MdAdmonition e = path.getElement();
        return new MdAdmonition(e.getCode(), e.getType(), transformElement(path.append(e.getContent())));
    }

    protected MdElement transformNumberedItem(MdElementPath<MdNumberedItem> path) {
        MdNumberedItem e = path.getElement();
        return new MdNumberedItem(e.getNumber(), e.type().depth(), e.getSep(), transformElement(path.append(e.getValue())), new MdElement[0]);
    }

    protected MdElement transformUnNumberedItem(MdElementPath<MdUnNumberedItem> path) {
        MdUnNumberedItem e = path.getElement();
        return new MdUnNumberedItem(e.getType(), e.type().depth(), transformElement(path.append(e.getValue())), new MdElement[0]);
    }

    protected MdElement transformTitle(MdElementPath<MdTitle> path) {
        MdTitle e = path.getElement();
        return new MdTitle(e.getCode(), transformElement(path.append(e.getValue())),e.type().depth(),transformArray(e.getChildren(),path));
    }

    protected MdElement transformXml(MdElementPath<MdXml> path) {
        MdElement r = transformElement(path.append(path.getElement().getContent()));
        return new MdXml(MdXml.XmlTagType.OPEN, path.getElement().getTag(), path.getElement().getProperties(), r);
    }

//    protected MdElement transformSequence(MdElementPath<MdSequence> path) {
//        List<MdElement> a = new ArrayList<MdElement>();
//        MdSequence e = path.getElement();
//        for (MdElement mdElement : e.getElements()) {
//            MdElement v = transformElement(path.append(mdElement));
//            if (v != null) {
//                a.add(v);
//            }
//        }
//        if (a.isEmpty()) {
//            return null;
//        }
//        if (a.size() == 1) {
//            return a.get(0);
//        }
//        return MdSequence.of(a.toArray(new MdElement[0]));
//    }

    public MdElement transformBold(MdElementPath<MdBold> path) {
        MdBold e = path.getElement();
        return new MdBold(e.getType(), transformElement(path.append(e.getContent())));
    }

    public MdElement transformItalic(MdElementPath<MdItalic> path) {
        MdItalic e = path.getElement();
        return new MdItalic(e.getType(), transformElement(path.append(e.getContent())));
    }

    protected MdElement transformURL(MdElementPath<MdLink> path) {
        return path.getElement();
    }

    protected MdElement transformImage(MdElementPath<MdImage> path) {
        MdImage element = path.getElement();
        return element;
    }

    protected MdElement transformRow(MdElementPath<MdRow> path) {
        MdRow e = path.getElement();
        return new MdRow(transformArray(e.getCells(), path), e.isHeader());
    }

    private MdElement transformColumn(MdElementPath<MdColumn> path) {
        return new MdColumn(
                transformElement(path.append(path.getElement().getName())),
                path.getElement().getHorizontalAlign()
        );
    }

}
