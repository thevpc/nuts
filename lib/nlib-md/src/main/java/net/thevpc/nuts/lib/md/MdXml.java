/**
 * ====================================================================
 * thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.lib.md;

import net.thevpc.nuts.lib.md.util.MdUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author thevpc
 */
public class MdXml extends MdAbstractElement {

    private String tag;
    private Map<String, String> properties = new HashMap<>();
    private MdElement content;
    private XmlTagType tagType;

    public MdXml(XmlTagType tagType, String tag, String properties, MdElement content) {
        this.tagType = tagType;
        this.tag = tag;
        this.properties = new PropertiesParser(properties).parseMap();
        this.content = content;
    }

    public MdXml(XmlTagType tagType, String tag, Map<String, String> properties, MdElement content) {
        this.tagType = tagType;
        this.tag = tag;
        this.properties = properties;
        this.content = content;
    }

    public XmlTagType getTagType() {
        return tagType;
    }

    @Override
    public MdElementType type() {
        return MdElementType.XML;
    }

    @Override
    public boolean isInline() {
        return true;
    }

    public String getTag() {
        return tag;
    }

    public Map<String, String> getProperties() {
        return properties;
    }

    public MdElement getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag);
        if (properties != null && properties.size() > 0) {
            for (Map.Entry<String, String> e : properties.entrySet()) {
                sb.append(" ").append(e.getKey());
                if (e.getValue() != null) {
                    sb.append("=").append('"').append(MdUtils.escapeString(e.getValue())).append('"');
                }
            }
        }
        sb.append(">");
        sb.append(content);
        sb.append("</").append(tag);
        sb.append(">");
        return sb.toString();
    }

    public enum XmlTagType {
        OPEN,
        CLOSE,
        AUTO_CLOSE,
    }

    @Override
    public boolean isEndWithNewline() {
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MdXml mdXml = (MdXml) o;
        return Objects.equals(tag, mdXml.tag) && Objects.equals(properties, mdXml.properties) && Objects.equals(content, mdXml.content) && tagType == mdXml.tagType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag, properties, content, tagType);
    }
}
