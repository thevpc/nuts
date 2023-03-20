package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.util.reflect.SimpleParametrizedType;
import net.thevpc.nuts.runtime.standalone.xtra.execentries.DefaultNExecutionEntry;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NRef;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.logging.Level;

public class JavaClassUtils {
    public static Class unwrapCGLib(Class clazz) {
        if(isCGLib(clazz)){
            return clazz.getSuperclass();
        }
        return clazz;
    }

    public static boolean isCGLib(Class clazz) {
        if(clazz.getSimpleName().contains("$$EnhancerBySpringCGLIB$$")
                ||clazz.getSimpleName().contains("$$CGLIB$$")
        ){
            return true;
        }
        return false;
    }

    /**
     * @param stream stream
     * @return main class type for the given
     */
    public static MainClassType getMainClassType(InputStream stream, NSession session) {
        final NRef<Boolean> mainClass = new NRef<>();
        final NRef<Boolean> nutsApp = new NRef<>();
        final NRef<String> nutsAppVer = new NRef<>();
        final NRef<String> className = new NRef<>();
        JavaClassByteCode.Visitor cl = new JavaClassByteCode.Visitor() {
            @Override
            public boolean visitClassDeclaration(int access, String name, String superName, String[] interfaces) {
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
                return true;
            }

            @Override
            public boolean visitMethod(int access, String name, String desc) {
                if (name.equals("main") && desc.equals("([Ljava/lang/String;)V")
                        && Modifier.isPublic(access)
                        && Modifier.isStatic(access)) {
                    mainClass.set(true);
                }
                return true;
            }
        };
        JavaClassByteCode classReader = new JavaClassByteCode(new BufferedInputStream(stream), cl, session);
        if (mainClass.isSet()) {
            return new MainClassType(className.get(), mainClass.isSet(), nutsApp.isSet());
        }
        return null;
    }

    public static NExecutionEntry parseClassExecutionEntry(InputStream classStream, String sourceName, NSession session) {
        MainClassType mainClass = null;
        try {
            mainClass = getMainClassType(classStream, session);
        } catch (Exception ex) {
            NLogOp.of(CorePlatformUtils.class, session).level(Level.FINE).error(ex)
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
    public static String classVersionToSourceVersion(String classVersion, NSession session) {
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
        return classVersionToSourceVersion(major, minor, session);
    }

    public static String classVersionToSourceVersion(int major, int minor, NSession session) {
        if (major < 45) {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid classVersion %s.%s", major,minor));
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

    public static String sourceVersionToClassVersion(String sourceVersion, NSession session) {
        NVersion v = NVersion.of(sourceVersion).get(session);
        int major = v.getNumber(0).flatMap(NLiteral::asInt).orElse(0);
        int minor = v.getNumber(1).flatMap(NLiteral::asInt).orElse(-1);
        if (major < 1) {
            throw new NIllegalArgumentException(session, NMsg.ofC("invalid sourceVersion %s", sourceVersion));
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
                    throw new NIllegalArgumentException(session, NMsg.ofC("invalid sourceVersion %s", sourceVersion));
                }
            }
        } else {
            return String.valueOf(46 - major - 2);
        }
    }
}
