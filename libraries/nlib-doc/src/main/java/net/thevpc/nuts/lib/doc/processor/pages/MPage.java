package net.thevpc.nuts.lib.doc.processor.pages;

import net.thevpc.nuts.lib.md.MdElement;
import net.thevpc.nuts.text.NText;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;

public class MPage {
    public MPageType type;
    public String pathName;
    public String id;
    public String path;
    public String title;
    public String category;
    public String hmi;
    public String website;
    public String subTitle;
    public String installCommand;
    public String exampleCommand;
    public String menuTitle;
    public int order;
    public int level;
    public MdElement markdownContent;
    public NText ntfContent;
    public boolean sortAsc = true;

    private String author;
    private String authorTitle;
    private String authorURL;
    private String authorImageUrl;
    private Instant publishDate;
    private Instant lastModified;
    private String[] tags;
    private Map<String, Object> typeInfo;


    public String getInstallCommand() {
        return installCommand;
    }

    public void setInstallCommand(String installCommand) {
        this.installCommand = installCommand;
    }

    public String getExampleCommand() {
        return exampleCommand;
    }

    public void setExampleCommand(String exampleCommand) {
        this.exampleCommand = exampleCommand;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getHmi() {
        return hmi;
    }

    public void setHmi(String hmi) {
        this.hmi = hmi;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public MPage(MPageType type) {
        this.type = type;
    }

    public MPageType getType() {
        return type;
    }

    public NText getNtfContent() {
        return ntfContent;
    }

    public void setNtfContent(NText ntfContent) {
        this.ntfContent = ntfContent;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public Map<String, Object> getTypeInfo() {
        return typeInfo;
    }

    public void setTypeInfo(Map<String, Object> typeInfo) {
        this.typeInfo = typeInfo;
    }

    public String getMenuTitle() {
        return menuTitle;
    }

    public void setMenuTitle(String menuTitle) {
        this.menuTitle = menuTitle;
    }

    public Instant getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Instant publishDate) {
        this.publishDate = publishDate;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

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

    public MPage setPathName(String pathName) {
        this.pathName = pathName;
        return this;
    }

    public int getOrder() {
        return order;
    }

    public MPage setOrder(int order) {
        this.order = order;
        return this;
    }

    public MdElement getMarkdownContent() {
        return markdownContent;
    }

    public MPage setMarkdownContent(MdElement markdownContent) {
        this.markdownContent = markdownContent;
        return this;
    }

    public MPage() {
    }

    public String getId() {
        return id;
    }

    public MPage setId(String id) {
        this.id = id;
        return this;
    }

    public String getPath() {
        return path;
    }

    public MPage setPath(String path) {
        this.path = path;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public MPage setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorTitle() {
        return authorTitle;
    }

    public void setAuthorTitle(String authorTitle) {
        this.authorTitle = authorTitle;
    }

    public String getAuthorURL() {
        return authorURL;
    }

    public void setAuthorURL(String authorURL) {
        this.authorURL = authorURL;
    }

    public String getAuthorImageUrl() {
        return authorImageUrl;
    }

    public void setAuthorImageUrl(String authorImageUrl) {
        this.authorImageUrl = authorImageUrl;
    }

    public String[] getTags() {
        return tags;
    }

    public void setTags(String[] tags) {
        this.tags = tags;
    }

    @Override
    public String toString() {
        return "MdPage{" +
                "pathName='" + pathName + '\'' +
                ", id='" + id + '\'' +
                ", path='" + path + '\'' +
                ", title='" + title + '\'' +
                ", menuTitle='" + menuTitle + '\'' +
                ", order=" + order +
                ", level=" + level +
                ", markdown=" + markdownContent +
                ", sortAsc=" + sortAsc +
                ", author='" + author + '\'' +
                ", authorTitle='" + authorTitle + '\'' +
                ", authorURL='" + authorURL + '\'' +
                ", authorImageUrl='" + authorImageUrl + '\'' +
                ", publishDate=" + publishDate +
                ", lastModified=" + lastModified +
                ", tags=" + Arrays.toString(tags) +
                ", typeInfo=" + typeInfo +
                '}';
    }
}
