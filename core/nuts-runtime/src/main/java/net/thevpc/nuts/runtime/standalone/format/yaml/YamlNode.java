package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NPairElement;

class YamlNode {

    NodeType type;
    Object value;

    YamlNode(NodeType type, Object value) {
        this.type = type;
        this.value = value;
    }

    static YamlNode forLiteral(NElement value) {
        return new YamlNode(NodeType.LITERAL, value);
    }

    static YamlNode forArrayElement(NElement value) {
        return new YamlNode(NodeType.ARRAY_ELEMENT, value);
    }

    static YamlNode forObjectElement(NPairElement value) {
        return new YamlNode(NodeType.OBJECT_ELEMENT, value);
    }

    public NodeType getType() {
        return type;
    }

    public NElement getElement() {
        return (NElement) value;
    }

    public NPairElement getEntry() {
        return (NPairElement) value;
    }

    public Object getValue() {
        return value;
    }

}
