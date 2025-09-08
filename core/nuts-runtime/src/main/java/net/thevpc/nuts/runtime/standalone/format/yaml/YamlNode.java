package net.thevpc.nuts.runtime.standalone.format.yaml;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NPairElement;

class YamlNode {

    int indent;
    NodeType type;
    NElement value;

    YamlNode(NodeType type, NElement value, int indent) {
        this.type = type;
        this.value = value;
    }

    static YamlNode forLiteral(NElement value, int indent) {
        return new YamlNode(NodeType.LITERAL, value, indent);
    }

    static YamlNode forArrayElement(NElement value, int indent) {
        return new YamlNode(NodeType.ARRAY_ELEMENT, value, indent);
    }

    static YamlNode forObjectElement(NPairElement value, int indent) {
        return new YamlNode(NodeType.OBJECT_ELEMENT, value, indent);
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
