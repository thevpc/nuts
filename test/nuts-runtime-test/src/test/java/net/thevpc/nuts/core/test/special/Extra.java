package net.thevpc.nuts.core.test.special;

import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.runtime.standalone.io.util.ZipUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassByteCode;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Extra {
    public static void main(String[] args) {
        String spath = "/data/git/nuts/core/nuts/target/nuts-0.8.3.jar";
        final Map<String, Set<String>> classes = new HashMap<>();
        try (InputStream jarStream = Files.newInputStream(Paths.get(spath))) {
            ZipUtils.visitZipStream(jarStream, (path, inputStream) -> {
                if (path.endsWith(".class")) {
                    JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
                        @Override
                        public NVisitResult visitVersion(int major, int minor) {
                            String v = JavaClassUtils.classVersionToSourceVersion(major, minor, null);
                            Set<String> r = classes.get(v);
                            if (r == null) {
                                r = new HashSet<>();
                                classes.put(v, r);
                            }
                            r.add(path.substring(0, path.length() - 6));
                            return NVisitResult.TERMINATE;
                        }
                    };
                    JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(inputStream), cl, null);

                }
                return NVisitResult.CONTINUE;
            }, null);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        for (Map.Entry<String, Set<String>> e : classes.entrySet()) {
            System.out.println(e.getKey() + " (" + e.getValue().size() + ")");
        }
        System.out.println("DETAILS:");
        for (Map.Entry<String, Set<String>> e : classes.entrySet()) {
            System.out.println(e.getKey() + " (" + e.getValue().size() + ")");
            for (String s : e.getValue()) {
                System.out.println("\t" + s);
            }
        }
    }
}
