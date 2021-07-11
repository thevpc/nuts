package net.thevpc.nuts.toolbox.docusaurus;

public interface NameResolver {
    boolean accept(DocusaurusFileOrFolder item,String name);
}
