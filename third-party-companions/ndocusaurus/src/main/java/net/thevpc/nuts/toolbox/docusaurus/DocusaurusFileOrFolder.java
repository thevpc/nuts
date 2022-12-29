package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.lib.md.MdElement;

public interface DocusaurusFileOrFolder {
    String getShortId();

    String getLongId();

    String getTitle();

    boolean isFile();

    int getOrder();

    boolean isFolder();

    MdElement getContent(NSession session);

    String toJSON(int indent);
}
