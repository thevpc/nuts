package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBSerializerForBoolean extends NanoDBNonNullSerializer<Boolean> {
    public NanoDBSerializerForBoolean() {
        super(Boolean.class);
    }

    @Override
    public void write(Boolean obj, NanoDBOutputStream out) {
        out.writeBoolean((Boolean) obj);
    }

    @Override
    public Boolean read(NanoDBInputStream in) {
        return in.readBoolean();
    }
}
