/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusUtils;
import net.thevpc.nuts.lib.md.util.MdUtils;

import java.nio.file.Paths;
import java.util.*;

/**
 *
 * @author thevpc
 */
public class DocusaurusTreeTransform extends MdElementTransformBase {
    private int minDepth;
    private String fromPath;
    private String toPath;
    public DocusaurusTreeTransform(int minDepth, String fromPath, String toPath) {
        this.minDepth=minDepth;
        this.fromPath=fromPath;
        this.toPath=toPath;
    }

    @Override
    protected MdElement transformTitle(MdElementPath<MdTitle> path) {
        MdTitle e=path.getElement();
        int newDepth = e.type().depth() + this.minDepth-1;
        if(newDepth>6){
            newDepth=6;
        }
        return new MdTitle(e.getCode(), transformElement(path.append(e.getValue())), newDepth,transformArray(e.getChildren(),path));
    }

    @Override
    protected MdElement transformBody(MdElementPath<MdBody> path) {
        MdBody e = path.getElement();
        return new MdBody(transformArray(e.getChildren(),path));
    }

    @Override
    protected MdElement transformPhrase(MdElementPath<MdPhrase> path) {
        MdPhrase e = path.getElement();
        return new MdPhrase(transformArray(e.getChildren(),path));
    }

    @Override
    protected MdElement transformUnNumberedList(MdElementPath<MdUnNumberedList> path) {
        MdUnNumberedList e = path.getElement();
        return new MdUnNumberedList(e.type().depth(),
                Arrays.asList(transformArray(e.getChildren(),path)).toArray(new MdUnNumberedItem[0])
        );
    }

    @Override
    protected MdElement transformNumberedList(MdElementPath<MdNumberedList> path) {
        MdNumberedList e = path.getElement();
        return new MdNumberedList(e.type().depth(),
                Arrays.asList(transformArray(e.getChildren(),path)).toArray(new MdNumberedItem[0])
        );
    }
    @Override
    protected MdElement transformUnNumberedItem(MdElementPath<MdUnNumberedItem> path) {
        MdUnNumberedItem e = path.getElement();
        return new MdUnNumberedItem(e.getType(),e.type().depth(),transformElement(path.append(e.getValue()))
                ,transformArray(e.getChildren(),path)
        );
    }

    @Override
    protected MdElement transformNumberedItem(MdElementPath<MdNumberedItem> path) {
        MdNumberedItem e = path.getElement();
        return new MdNumberedItem(e.getNumber(),e.type().depth(),e.getSep(),transformElement(path.append(e.getValue())),
                transformArray(e.getChildren(),path)
                );
    }

    @Override
    public MdElement transformDocument(MdElement e) {
        if (e instanceof MdBody) {
            MdBody s = (MdBody) e;
            MdElement[] content = s.getChildren();
            if (content.length > 0 && content[0] instanceof MdHr) {
                int x = 0;
                for (int i = 1; i < content.length; i++) {
                    if (content[i] instanceof MdHr) {
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
                        } else if (t.trim().startsWith("import ")) {
                            it.remove();
                        } else {
                            break;
                        }
                    }else{
                        break;
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
                NArrayElement rows = NElements.of().parse(props).asArray().orElse(NArrayElement.ofEmpty());
                Map<String,MdElement> sub=new HashMap<>();
                for (MdElement item : MdFactory.asBody(e.getContent()).getChildren()) {
                    if (item.isXml()) {
                        MdXml tabItem = item.asXml();
                        String t = tabItem.getTag();
                        if (t.equals("TabItem")) {
                            String tt = "Unknown";
                            NElement v = NElements.of().parse(tabItem.getProperties().get("value"));
                            if (v != null) {
                                tt = v.asString().get();
                            }
                            MdElement u = transformXml(path.append(tabItem));
                            sub.put(tt, u);
                        }
                    } else if (item.isText()) {
                        if (item.asText().getText().trim().length() > 0) {
                            throw new IllegalArgumentException("unexpected xml content: " + item.type() + ":" + item.asText().getText());
                        }
                    } else {
                        throw new IllegalArgumentException("unexpected xml content: " + item.type() + ":");
                    }
                }
                List<MdElement> res=new ArrayList<>();
                for (NElement row : rows) {
                    MdElement r = sub.get(row.asObject().orElse(NObjectElement.ofEmpty()).getString("value").orElse(""));
                    if(r!=null){
                        res.add(r);
                    }
                }
                return MdFactory.seq(res.toArray(new MdElement[0]));
            }

            case "TabItem": {
                String tt = "Unknown";
                NElement v = NElements.of().parse(e.getProperties().get("value"));
                if (v != null) {
                    tt = v.asString().get();
                }
                String props = DocusaurusUtils.skipJsonJSXBrackets(path.getParentPath().getElement().asXml().getProperties().get("values"));
                for (NElement a : NElements.of().parse(props).asArray().orElse(NArrayElement.ofEmpty())) {
                    if (tt.equals(a.asObject().orElse(NObjectElement.ofEmpty()).getString("value").orNull())) {
                        tt = a.asObject().orElse(NObjectElement.ofEmpty()).getString("label").orNull();
                        break;
                    }
                }
                if (tt.equals("C#")) {
                    tt = "C Sharp";
                }
                return new MdBody( new MdElement[]{new MdTitle("#####", MdText.phrase(tt), 5,new MdElement[0]), transformElement(path.append(e.getContent()))});
            }

        }
        return e;
    }

    protected MdElement transformImage(MdElementPath<MdImage> path) {
        MdImage element = path.getElement();
        if(element.getImageFormat()== MdImage.ImageFormat.PATH){
            String url = element.getImageUrl();
            if(url.length()>0 && MdUtils.isRelativePath(url)){
                return new MdImage(
                        element.getType(),
                        element.getImageFormat(),
                        element.getImageTitle(),
                        MdUtils.toRelativePath(
                                Paths.get(fromPath).resolve(url).toString(),
                                toPath
                        )

                );
            }
        }
        return element;
    }
}
