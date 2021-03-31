/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nnote.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import net.thevpc.nuts.toolbox.nnote.gui.util.NNoteError;
import net.thevpc.nuts.toolbox.nnote.gui.NNoteTypes;

/**
 *
 * @author vpc
 */
public class NNote implements Cloneable {

    public static NNote newDocument(String path) {
        NNote n = new NNote();
        n.setContentType(NNoteTypes.NNOTE_DOCUMENT);
        n.setContent(path);
        return n;
    }

    private String version;
    private Instant creationTime;
    private Instant lastModified;
    private String name;
    private boolean titleBold;
    private boolean titleItalic;
    private boolean titleUnderlined;
    private String titleForeground;
    private String titleBackground;
    private String icon;
    private String folderIcon;
    private String contentType;
    private String editorType;
    private String content;
    private boolean readOnly;
    public transient NNoteError error;
    private Set<String> tags = new HashSet<String>();
    private List<NNote> children = new ArrayList<>();
    private Map<String, String> properties = new LinkedHashMap<String, String>();
    private CypherInfo cypherInfo;

    public String getFolderIcon() {
        return folderIcon;
    }

    public void setFolderIcon(String folderIcon) {
        this.folderIcon = folderIcon;
    }

    public String getContentType() {
        return contentType;
    }

    public NNote setContentType(String type) {
        this.contentType = type;
        return this;
    }

    public String getEditorType() {
        return editorType;
    }

    public void setEditorType(String editorType) {
        this.editorType = editorType;
    }

    public String getContent() {
        return content;
    }

    public NNote setContent(String content) {
        this.content = content;
        return this;
    }

    public Set<String> getTags() {
        return tags;
    }

    public NNote setTags(Set<String> tags) {
        this.tags = tags;
        return this;
    }

    public String getName() {
        return name;
    }

    public NNote setName(String name) {
        this.name = name;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public NNote setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public List<NNote> getChildren() {
        return children;
    }

    public NNote setChildren(List<NNote> children) {
        this.children = children;
        return this;
    }

    public NNoteError getError() {
        return error;
    }

    public void setError(NNoteError error) {
        this.error = error;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public Instant getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Instant creationTime) {
        this.creationTime = creationTime;
    }

    public Instant getLastModified() {
        return lastModified;
    }

    public void setLastModified(Instant lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 17 * hash + Objects.hashCode(this.version);
        hash = 17 * hash + Objects.hashCode(this.creationTime);
        hash = 17 * hash + Objects.hashCode(this.lastModified);
        hash = 17 * hash + Objects.hashCode(this.name);
        hash = 17 * hash + (this.titleBold ? 1 : 0);
        hash = 17 * hash + (this.titleItalic ? 1 : 0);
        hash = 17 * hash + (this.titleUnderlined ? 1 : 0);
        hash = 17 * hash + Objects.hashCode(this.titleForeground);
        hash = 17 * hash + Objects.hashCode(this.titleBackground);
        hash = 17 * hash + Objects.hashCode(this.icon);
        hash = 17 * hash + Objects.hashCode(this.folderIcon);
        hash = 17 * hash + Objects.hashCode(this.contentType);
        hash = 17 * hash + Objects.hashCode(this.editorType);
        hash = 17 * hash + Objects.hashCode(this.content);
        hash = 17 * hash + (this.readOnly ? 1 : 0);
        hash = 17 * hash + Objects.hashCode(this.error);
        hash = 17 * hash + Objects.hashCode(this.tags);
        hash = 17 * hash + Objects.hashCode(this.children);
        hash = 17 * hash + Objects.hashCode(this.properties);
        hash = 17 * hash + Objects.hashCode(this.cypherInfo);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NNote other = (NNote) obj;
        if (this.titleBold != other.titleBold) {
            return false;
        }
        if (this.titleItalic != other.titleItalic) {
            return false;
        }
        if (this.titleUnderlined != other.titleUnderlined) {
            return false;
        }
        if (this.readOnly != other.readOnly) {
            return false;
        }
        if (!Objects.equals(this.version, other.version)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.titleForeground, other.titleForeground)) {
            return false;
        }
        if (!Objects.equals(this.titleBackground, other.titleBackground)) {
            return false;
        }
        if (!Objects.equals(this.icon, other.icon)) {
            return false;
        }
        if (!Objects.equals(this.folderIcon, other.folderIcon)) {
            return false;
        }
        if (!Objects.equals(this.contentType, other.contentType)) {
            return false;
        }
        if (!Objects.equals(this.editorType, other.editorType)) {
            return false;
        }
        if (!Objects.equals(this.content, other.content)) {
            return false;
        }
        if (!Objects.equals(this.creationTime, other.creationTime)) {
            return false;
        }
        if (!Objects.equals(this.lastModified, other.lastModified)) {
            return false;
        }
        if (!Objects.equals(this.error, other.error)) {
            return false;
        }
        if (!Objects.equals(this.tags, other.tags)) {
            return false;
        }
        if (!Objects.equals(this.children, other.children)) {
            return false;
        }
        if (!Objects.equals(this.properties, other.properties)) {
            return false;
        }
        if (!Objects.equals(this.cypherInfo, other.cypherInfo)) {
            return false;
        }
        return true;
    }

    public CypherInfo getCypherInfo() {
        return cypherInfo;
    }

    public void setCypherInfo(CypherInfo cypherInfo) {
        this.cypherInfo = cypherInfo;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public NNote copy() {
        NNote n2;
        try {
            n2 = (NNote) clone();
        } catch (CloneNotSupportedException ex) {
            throw new IllegalArgumentException("Impossible");
        }

        n2.setTags(this.getTags() == null ? null : new HashSet<>(this.getTags()));
        n2.setCypherInfo(getCypherInfo() == null ? null : getCypherInfo().copy());
        n2.setProperties(getProperties() == null ? null : new LinkedHashMap<>(getProperties()));
        n2.setChildren(new ArrayList<>());
        if (this.getChildren() != null) {
            for (NNote c : this.getChildren()) {
                if (c != null) {
                    n2.getChildren().add(c.copy());
                }
            }
        }
        return n2;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "NNote{" + "name=" + name + ", contentType=" + contentType + '}';
    }

    public boolean isTitleBold() {
        return titleBold;
    }

    public void setTitleBold(boolean titleBold) {
        this.titleBold = titleBold;
    }

    public boolean isTitleItalic() {
        return titleItalic;
    }

    public void setTitleItalic(boolean titleItalic) {
        this.titleItalic = titleItalic;
    }

    public boolean isTitleUnderlined() {
        return titleUnderlined;
    }

    public void setTitleUnderlined(boolean titleUnderlined) {
        this.titleUnderlined = titleUnderlined;
    }

    public String getTitleForeground() {
        return titleForeground;
    }

    public void setTitleForeground(String titleForeground) {
        this.titleForeground = titleForeground;
    }

    public String getTitleBackground() {
        return titleBackground;
    }

    public void setTitleBackground(String titleBackground) {
        this.titleBackground = titleBackground;
    }

}
