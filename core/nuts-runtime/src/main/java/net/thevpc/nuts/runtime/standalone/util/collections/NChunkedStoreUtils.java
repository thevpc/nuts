package net.thevpc.nuts.runtime.standalone.util.collections;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.NLiteral;

import java.util.Comparator;

public class NChunkedStoreUtils {
    public static final String CHUNK_EXT = ".chunk";
    public static final String READ_PTR_FILE_NAME = "read.ptr";
    public static final String WRITE_PTR_FILE_NAME = "write.ptr";
    public static final int DEFAULT_CHUNK_NUMBER_LAYOUT = 8;
    public static final int DEFAULT_READ_UNCOMMITTED = 10;

    public static class PointerInfo {
        public final long chunk;
        public final long index;

        public PointerInfo(long chunk, long index) {
            this.chunk = chunk;
            this.index = index;
        }

        @Override
        public String toString() {
            return "PointerInfo{" +
                    "chunk=" + chunk +
                    ", index=" + index +
                    '}';
        }
    }

    public static class FileAndNumber {
        NPath path;
        long number;

        public FileAndNumber(NPath path) {
            this.path = path;
            this.number = chunkNumber(path);
        }
    }

    public static long chunkNumber(NPath path) {
        String a = path.nameParts().getBaseName();
        int i = 0;
        while (i < a.length() && a.charAt(i) == '0') {
            i++;
        }
        if (i > 0) {
            a = a.substring(i);
            if (a.isEmpty()) {
                return 0;
            }
        }
        return NLiteral.of(a).asLong().orElse(-1L);
    }

    public static String chunkFileName(long chunkNumber, int chunkNumberLayout) {
        StringBuilder s = new StringBuilder(chunkNumberLayout);
        s.append(chunkNumber);
        while (s.length() < chunkNumberLayout) {
            s.insert(0, '0');
        }
        s.append(CHUNK_EXT);
        return s.toString();
    }


    public static class FileAndNumberComparator implements Comparator<FileAndNumber> {
        @Override
        public int compare(FileAndNumber o1, FileAndNumber o2) {
            int i = Long.compare(o1.number, o2.number);
            if (i != 0) {
                return i;
            }
            return o1.path.getName().compareTo(o2.path.getName());
        }
    }

    public static void writePointerInfo(PointerInfo ptr, NPath folder, String file) {
        if (ptr != null) {
            NPath ptrPath = folder.resolve(file);
            NPath tmp = ptrPath.resolveSibling(ptrPath.getName() + ".tmp");

            tmp.writeString(ptr.chunk + "," + ptr.index);
            tmp.moveTo(ptrPath, NPathOption.ATOMIC, NPathOption.REPLACE_EXISTING);
        }
    }

    public static PointerInfo readPointerInfo(NPath folder, String file) {
        NPath ptr = folder.resolve(file);
        if (ptr.exists()) {
            String s = ptr.readString();
            String[] a = s.split(",");
            if (a.length >= 2) {
                Long c = NLiteral.of(a[0]).asLong().orNull();
                Long i = NLiteral.of(a[1]).asLong().orNull();
                if (c != null && i != null) {
                    return new PointerInfo(c, i);
                }
            }
        }
        return null;
    }
}
