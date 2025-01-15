package net.thevpc.nuts.lib.doc.pages;

import net.thevpc.nuts.lib.md.MdElement;

public class MdPage {
    public String pathName;
    public String id;
    public String path;
    public String title;
    public int order;
    public int level;
    public MdElement markdown;
    public boolean sortAsc=true;

    public boolean isSortAsc() {
        return sortAsc;
    }

    public void setSortAsc(boolean sortAsc) {
        this.sortAsc = sortAsc;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getPathName() {
        return pathName;
    }

    public MdPage setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public MdPage setOrder(int order) {
        this.order = order;
        return this;
    }

    public MdElement getMarkdown() {
        return markdown;
    }

    public MdPage setMarkdown(MdElement markdown) {
        this.markdown = markdown;
        return this;
    }

    public MdPage() {
    }

    public String getId() {
        return id;
    }

    public MdPage setId(String id) {
        this.id = id;
        return this;
    }

    public String getPath() {
        return path;
    }

    public MdPage setPath(String path) {
        this.path = path;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MdPage setTitle(String title) {
        this.title = title;
        return this;
    }

    @Override
    public String toString() {
        return "MdPage{" +
                "title='" + title + '\'' +
                ", path='" + path + '\'' +
                '}';
    }
}
