package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.format.NVisitResult;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNExecutionEntry;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NRef;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;

public class JavaClassUtils {
    public static Class unwrapCGLib(Class clazz) {
        if(isCGLib(clazz)){
            return clazz.getSuperclass();
        }
        return clazz;
    }

    public static List<URL> resolveURLs(Class clazz) {
        List<URL> all=new ArrayList<>();
        try {
            final String n = clazz.getName().replace('.', '/').concat(".class");
            ClassLoader classLoader = clazz.getClassLoader();
            if(classLoader==null){
                return null;
            }
            final Enumeration<URL> r = classLoader.getResources(n);
            ArrayList<URL> list = Collections.list(r);
            for (URL url : list) {
                String s = url.toString();
                if(s.endsWith(n)){
                    String substring = s.substring(0, s.length() - n.length());
                    if(substring.startsWith("jar:") && substring.endsWith("!/")){
                        substring=substring.substring("jar:".length(),substring.length()-"!/".length());
                    }
                    all.add(CoreIOUtils.urlOf(substring));
                }
            }
        } catch (IOException | UncheckedIOException ex) {
            //
        }
        return all;
    }

    public static boolean isCGLib(Class clazz) {
        String simpleName = clazz.getSimpleName();
        if(simpleName.contains("$$EnhancerBySpringCGLIB$$")
                || simpleName.contains("$$CGLIB$$")
                || simpleName.contains("$$SpringCGLIB$$")
        ){
            return true;
        }
        return false;
    }

    /**
     * @param stream stream
     * @return main class type for the given
     */
    public static MainClassType getMainClassType(InputStream stream, NWorkspace workspace) {
        final NRef<Boolean> mainClass = new NRef<>();
        final NRef<Boolean> nutsApp = new NRef<>();
        final NRef<String> nutsAppVer = new NRef<>();
        final NRef<String> className = new NRef<>();
        JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
            @Override
            public NVisitResult visitClassDeclaration(int access, String name, String superName, String[] interfaces) {
                //v0.8.0
                //TODO remove me
                if (superName != null && superName.equals("net/thevpc/nuts/NApplication")) {
                    nutsApp.set(true);
                    nutsAppVer.set("0.8.4");
                    //TODO remove me
                }else if (superName != null && superName.equals("net/thevpc/nuts/NutsApplication")) {
                    nutsApp.set(true);
                    nutsAppVer.set("0.8.0");
                    //TODO remove me
                } else if (superName != null && superName.equals("net/vpc/app/nuts/NutsApplication")) {
                    //this is nuts version < 0.8.0
                    nutsApp.set(true);
                    nutsAppVer.set("0.7.0");
                }
                if (interfaces != null) {
                    for (String anInterface : interfaces) {
                        //v0.8.1
                        if (anInterface != null && anInterface.equals("net/thevpc/nuts/NutsApplication")) {
                            nutsApp.set(true);
                            nutsAppVer.set("0.8.1");
                        }
                    }
                }
                className.set(name.replace('/', '.'));
                return NVisitResult.CONTINUE;
            }

            @Override
            public NVisitResult visitMethod(int access, String name, String desc) {
                if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)) {
                    mainClass.set(true);
                }
                return NVisitResult.CONTINUE;
            }
        };
        JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(stream), cl);
        if (mainClass.isSet()) {
            return new MainClassType(className.get(), mainClass.isSet(), nutsApp.isSet());
        }
        return null;
    }

    public static NExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName) {
        MainClassType mainClass = null;
        try {
            mainClass = getMainClassType(classStream, NWorkspace.get().get());
        } catch (Exception ex) {
            NLogOp.of(CorePlatformUtils.class).level(Level.FINE).error(ex)
                    .log(NMsg.ofJ("invalid file format {0}", sourceName));
        }
        if (mainClass != null) {
            return new DefaultNExecutionEntry(
                    mainClass.getName(),
                    false,
                    mainClass.isApp()
            );
        }
        return null;
    }

    /**
     * "Oracle's Java Virtual Machine implementation in JDK
     * release 1.0.2 supports class file format versions 45.0 through 45.3 inclusive.
     * JDK releases 1.1.* support class file format versions in the range 45.0 through 45.65535 inclusive. For k ≥ 2, JDK release 1.k supports class file format versions in the range 45.0 through 44+k.0 inclusive."
     *
     * @param classVersion
     * @return
     */
    public static String classVersionToSourceVersion(String classVersion) {
        int major;
        int minor;
        int i = classVersion.indexOf('.');
        if (i > 0) {
            major = Integer.parseInt(classVersion.substring(0, i));
            minor = Integer.parseInt(classVersion.substring(i + 1));
        } else {
            major = Integer.parseInt(classVersion);
            minor = 0;
        }
        return classVersionToSourceVersion(major, minor);
    }

    public static String classVersionToSourceVersion(int major, int minor) {
        if (major < 45) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid classVersion %s.%s", major,minor));
        }
        if (major == 45) {
            if (minor <= 3) {
                return "1.0.2";
            } else {
                return "1.1";
            }
        } else {
            int v = major - 46 + 2;
            if (v <= 8) {
                return "1." + v;
            } else {
                return String.valueOf(v);
            }
        }
    }

    public static String sourceVersionToClassVersion(String sourceVersion) {
        NVersion v = NVersion.get(sourceVersion).get();
        int major = v.getIntegerAt(0).orElse(0);
        int minor = v.getIntegerAt(1).orElse(-1);
        if (major < 1) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid sourceVersion %s", sourceVersion));
        }
        if (major == 1) {
            switch (minor) {
                case 0:
                    return "45.0";
                case 1:
                    return "45.4";
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                case 9:
                    return String.valueOf(46 - minor - 2);
                default: {
                    throw new NIllegalArgumentException(NMsg.ofC("invalid sourceVersion %s", sourceVersion));
                }
            }
        } else {
            return String.valueOf(46 - major - 2);
        }
    }
}
