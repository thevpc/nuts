/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.lib.md.convert;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusUtils;

import java.util.*;

/**
 *
 * @author thevpc
 */
public class DocusaurusTreeTransform extends MdElementTransformBase {
    private NutsSession session;
    public DocusaurusTreeTransform(NutsSession session) {
        this.session=session;
    }

    @Override
    protected MdElement transformTitle(MdElementPath<MdTitle> path) {
        MdTitle e=path.getElement();
        if (e.getDepth() < 6) {
            return new MdTitle(e.getCode(), e.getValue(), e.getDepth() + 1);
        }
        return e;
    }

    @Override
    public MdElement transformDocument(MdElement e) {
        if (e instanceof MdSequence) {
            MdSequence s = (MdSequence) e;
            MdElement[] content = s.getElements();
            if (content.length > 0 && content[0] instanceof MdLineSeparator) {
                int x = 0;
                for (int i = 1; i < content.length; i++) {
                    if (content[i] instanceof MdLineSeparator) {
                        x = i + 1;
                        break;
                    }
                }
                List<MdElement> a = new ArrayList<>();
                for (int i = x; i < content.length; i++) {
                    a.add(content[i]);
                }
                for (Iterator<MdElement> it = a.iterator(); it.hasNext();) {
                    MdElement mdElement = it.next();
                    if (mdElement instanceof MdText) {
                        String t = ((MdText) mdElement).getText();
                        if (t.length() == 0) {
                            it.remove();
                        } else if (t.startsWith("import ")) {
                            it.remove();
                        } else {
                            break;
                        }
                    }
                }
                return super.transformDocument(MdFactory.seq(a));
            }
        }
        return super.transformDocument(e);
    }

    @Override
    protected MdElement transformXml(MdElementPath<MdXml> path) {
        MdXml e=path.getElement();
        switch (e.getTag()) {
            case "Tabs": {
                String props = DocusaurusUtils.skipJsonJSXBrackets(e.getProperties().get("values"));
                NutsArrayElement rows = session.getWorkspace().elem().parse(props).asSafeArray();
                Map<String,MdElement> sub=new HashMap<>();
                for (MdElement item : MdFactory.asSeq(e.getContent()).getElements()) {
                    if (item.isXml()) {
                        MdXml tabItem = item.asXml();
                        String t = tabItem.getTag();
                        if (t.equals("TabItem")) {
                            String tt = "Unknown";
                            NutsElement v = session.getWorkspace().elem().parse(tabItem.getProperties().get("value"));
                            if (v != null) {
                                tt = v.asString();
                            }
                            MdElement u = transformXml(path.append(tabItem));
                            sub.put(tt, u);
                        }
                    } else if (item.isText()) {
                        if (item.asText().getText().trim().length() > 0) {
                            throw new IllegalArgumentException("Unexpected " + item.getElementType() + ":" + item.asText().getText());
                        }
                    } else {
                        throw new IllegalArgumentException("Unexpected " + item.getElementType() + ":");
                    }
                }
                List<MdElement> res=new ArrayList<>();
                for (NutsElement row : rows) {
                    MdElement r = sub.get(row.asSafeObject().getSafeString("value"));
                    if(r!=null){
                        res.add(r);
                    }
                }
                return MdFactory.seq(res.toArray(new MdElement[0]));
            }

            case "TabItem": {
                String tt = "Unknown";
                NutsElement v = session.getWorkspace().elem().parse(e.getProperties().get("value"));
                if (v != null) {
                    tt = v.asString();
                }
                String props = DocusaurusUtils.skipJsonJSXBrackets(path.getParentPath().getElement().asXml().getProperties().get("values"));
                for (NutsElement a : session.getWorkspace().elem().parse(props).asSafeArray()) {
                    if (tt.equals(a.asSafeObject().getSafeString("value"))) {
                        tt = a.asSafeObject().getSafeString("label");
                        break;
                    }
                }
                if (tt.equals("C#")) {
                    tt = "C Sharp";
                }
                return new MdSequence("", new MdElement[]{new MdTitle("#####", tt, 5), transformElement(path.append(e.getContent()))}, false);
            }

        }
        return e;
    }

}
