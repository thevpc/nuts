package net.thevpc.nuts.lib.doc.pages;

import net.thevpc.nuts.lib.md.MdElement;

public class PageGroup {
    public String pathName;
    public String id;
    public String path;
    public String title;
    public int order;
    public int level;
    public MdElement markdown;


    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPathName() {
        return pathName;
    }

    public PageGroup setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public PageGroup setOrder(int order) {
        this.order = order;
        return this;
    }

    public MdElement getMarkdown() {
        return markdown;
    }

    public PageGroup setMarkdown(MdElement markdown) {
        this.markdown = markdown;
        return this;
    }

    public PageGroup() {
    }

    public String getId() {
        return id;
    }

    public PageGroup setId(String id) {
        this.id = id;
        return this;
    }

    public String getPath() {
        return path;
    }

    public PageGroup setPath(String path) {
        this.path = path;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public PageGroup setTitle(String title) {
        this.title = title;
        return this;
    }
}
