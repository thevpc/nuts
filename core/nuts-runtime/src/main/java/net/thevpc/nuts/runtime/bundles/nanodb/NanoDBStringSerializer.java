package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBStringSerializer {
    public static class Null extends NanoDBNullSerializer<String>{
        public Null() {
            super(String.class);
        }

        @Override
        public void writeNonNull(String obj, NanoDBOutputStream out) {
            out.writeUTF(obj);
        }

        @Override
        public String readNonNull(NanoDBInputStream in) {
            return in.readUTF();
        }
    }

    static class NonNull extends NanoDBNonNullSerializer<String> {
        public NonNull() {
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
}
