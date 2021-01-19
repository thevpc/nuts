package net.thevpc.nuts.runtime.bundles.nanodb;

class NanoDBIntegerSerializer {
    static class Null extends NanoDBNullSerializer<Integer> {
        public Null() {
            super(Integer.class);
        }

        @Override
        public void writeNonNull(Integer obj, NanoDBOutputStream out) {
            out.writeInt(obj);
        }

        @Override
        public Integer readNonNull(NanoDBInputStream in) {
            return in.readInt();
        }
    }

    static class NonNull extends NanoDBNonNullSerializer<Integer> {
        public NonNull() {
            super(int.class);
        }

        @Override
        public void write(Integer obj, NanoDBOutputStream out) {
            out.writeInt((int) obj);
        }

        @Override
        public Integer read(NanoDBInputStream in) {
            return in.readInt();
        }
    }
}
