package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

class NanoDBSerializerForBoolean extends NanoDBNonNullSerializer<Boolean> {
    public NanoDBSerializerForBoolean() {
        super(Boolean.class);
    }

    @Override
    public void write(Boolean obj, NanoDBOutputStream out) {
        out.writeBoolean((Boolean) obj);
    }

    @Override
    public Boolean read(NanoDBInputStream in, Class expectedType) {
        return in.readBoolean();
    }
}
