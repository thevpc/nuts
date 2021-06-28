package net.thevpc.nuts.lib.md.docusaurus;

public interface DocusaurusFileOrFolder {
    String getShortId();

    String getLongId();

    String getTitle();

    boolean isFile();

    int getOrder();

    boolean isFolder();

    String toJSON(int indent);
}
