/**
 * ====================================================================
 *            thevpc-common-md : Simple Markdown Manipulation Library
 * <br>
 *
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

import java.util.Map;

/**
 *
 * @author thevpc
 */
public class MdXml extends MdAbstractElement {

    private String tag;
    private String propertiesString;
    private MdElement content;

    public MdXml(String tag, String properties, MdElement content) {
        this.tag = tag;
        this.propertiesString = properties;
        this.content = content;
    }

    @Override
    public MdElementType getElementType() {
        return MdElementType.XML;
    }

    public String getTag() {
        return tag;
    }

    public Map<String,String> getProperties() {
        return new PropertiesParser(propertiesString).parseMap();
    }
    public String getPropertiesString() {
        return propertiesString;
    }

    public MdElement getContent() {
        return content;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("<").append(tag);
        if (propertiesString != null) {
            sb.append(" ").append(propertiesString);
        }
        sb.append(">");
        sb.append(content);
        sb.append("</").append(tag);
        sb.append(">");
        return sb.toString();
    }

}
