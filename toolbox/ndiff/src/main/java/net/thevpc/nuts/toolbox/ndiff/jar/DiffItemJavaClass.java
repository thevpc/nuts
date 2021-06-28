/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.ndiff.jar;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class DiffItemJavaClass extends AbstractDiffItem {

    public DiffItemJavaClass(String name, DiffStatus kind, String desc, List<DiffItem> details) {
        super("java-class", name, kind,desc,details);
    }

    public String getClassName() {
        String r = getName().replace('/', '.');
        return r.substring(0, r.length() - ".class".length());
    }

    @Override
    public String toString() {
        DiffStatus kind = getStatus();
        String c = kind == DiffStatus.ADDED ? "+ " : kind == DiffStatus.REMOVED ? "- " : kind == DiffStatus.CHANGED ? "~ " : "? ";
        return c + getKind()+" : " + getClassName()+(getDescription() == null ? "" : (" (" + getDescription()+")"));
    }
}
