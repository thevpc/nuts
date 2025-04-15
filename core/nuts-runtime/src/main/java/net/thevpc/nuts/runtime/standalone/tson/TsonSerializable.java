package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonSerializable {
    default TsonElement toTsonElement() {
        return toTsonElement(Tson.serializer().context());
    }

    default TsonElement toTsonElement(TsonObjectContext context) {
        throw new UnsupportedOperationException("Unsupported toNode(TsonSerializer serializer) in " + getClass().getName());
    }
}
