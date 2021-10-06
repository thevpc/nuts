package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBSerializerForString extends NanoDBNonNullSerializer<String>{
    public NanoDBSerializerForString() {
        super(String.class);
    }

    @Override
    public void write(String obj, NanoDBOutputStream out) {
        out.writeUTF(obj);
    }

    @Override
    public String read(NanoDBInputStream in) {
        return in.readUTF();
    }
}
