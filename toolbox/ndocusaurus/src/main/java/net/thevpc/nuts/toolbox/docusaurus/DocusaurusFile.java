/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.docusaurus;


import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.lib.md.docusaurus.DocusaurusUtils;
import net.thevpc.nuts.lib.md.util.MdUtils;

/**
 *
 * @author thevpc
 */
public abstract class DocusaurusFile implements DocusaurusFileOrFolder {

    private final String longId;
    private final String shortId;
    private final String title;
    private final NutsElement config;
    private final int order;

//    public static DocusaurusFile ofContent(String id, String longId, String title, String content, int menuOrder) {
//        return new DocusaurusFile(Type.CONTENT, id, longId,title, null, content, null,menuOrder);
//    }

    //    public static DocusaurusFile ofTree(String id, String longId, String title, MdElement tree, int menuOrder) {
//        return new DocusaurusFile(Type.TREE, id, longId,title, null, null, tree,menuOrder);
//    }


    protected DocusaurusFile(String shortId, String longId, String title, int order,NutsElement config) {
        this.shortId = shortId;
        this.longId = longId;
        this.title = title;
        this.order = order;
        this.config = config;
    }

    public NutsElement getConfig() {
        return config;
    }

    public int getOrder() {
        return order;
    }

    public String getLongId() {
        return longId;
    }

    public String getShortId() {
        return shortId;
    }

    public String getTitle() {
        return title;
    }


    public abstract MdElement getContent(NutsSession session);

    @Override
    public String toString() {
        String s=longId;
        if(!s.startsWith("/")){
            s="/"+s;
        }
        return s;
    }

    public boolean isFile() {
        return true;
    }

    @Override
    public boolean isFolder() {
        return false;
    }

    public String toJSON(int indent) {
        StringBuilder sb = new StringBuilder();
        sb.append(DocusaurusUtils.indentChars(indent));
        sb.append("'").append(MdUtils.escapeString(getLongId() + "'"));
        return sb.toString();
    }
}
