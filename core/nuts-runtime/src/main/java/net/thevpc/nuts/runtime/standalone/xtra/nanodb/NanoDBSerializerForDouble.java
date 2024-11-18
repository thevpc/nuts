package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

class NanoDBSerializerForDouble extends NanoDBNonNullSerializer<Double> {
    public NanoDBSerializerForDouble() {
        super(Double.class);
    }

    @Override
    public void write(Double obj, NanoDBOutputStream out) {
        out.writeDouble((double) obj);
    }

    @Override
    public Double read(NanoDBInputStream in, Class expectedType) {
        return in.readDouble();
    }
}
