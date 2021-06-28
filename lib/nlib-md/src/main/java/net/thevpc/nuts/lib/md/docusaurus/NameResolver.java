package net.thevpc.nuts.lib.md.docusaurus;

public interface NameResolver {
    boolean accept(DocusaurusFileOrFolder item,String name);
}
