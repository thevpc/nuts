package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.lib.md.MdElement;

public interface DocusaurusFileOrFolder {
    String getShortId();

    String getLongId();

    String getTitle();

    boolean isFile();

    int getOrder();

    boolean isFolder();

    MdElement getContent();

    String toJSON(int indent);
}
