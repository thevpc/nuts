package net.thevpc.nuts.core.test.manual;

import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementWriter;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.runtime.standalone.util.jclass.JClassVersion;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaJarUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class PsManualTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    class A {
        B b;
    }

    class B {
        C c;
    }

    class C {
        double x;
    }

    @Test
    public void test1() {
        NOut.println("Hello ##world##");
        for (JClassVersion resolveJarJavaVersion : JavaJarUtils.resolveJarJavaVersions(NPath.of("/home/vpc/.m2/repository/org/jline/jline/3.25.0/jline-3.25.0.jar").getInputStream())) {
            System.out.println(resolveJarJavaVersion);
        }

        Map<JClassVersion, List<String>> map = JavaJarUtils.resolveJarJavaVersionsWithPaths(NPath.of("/home/vpc/.m2/repository/org/jline/jline/3.25.0/jline-3.25.0.jar").getInputStream());
        for (Map.Entry<JClassVersion, List<String>> e : map.entrySet()) {
            System.out.println(e.getKey());
            for (String s : e.getValue()) {
                System.out.println("        "+s);
            }
        }
        if (true) {
            return;
        }
//        TestUtils.printf("hello %s","toto");
//        Logger.getAnonymousLogger().log(Level.INFO, "hello {0}","A");

//        NOptional<Object> o = NOptional.ofNamedEmpty(NMsg.ofV("is really missing"));
//
//        NPath.of("/toto/titi.txt").mkParentDirs().writeString("hello world");
//        NPath.of("http://toto:/titi").copyTo(NPath.of("/toto"));
//        NPath.of("ssh://hammadi:pawd@toto:/titi").copyTo(NPath.of("/toto"));
//        NPath.of("classpath:/nte/toto/toti").copyTo(NPath.of("/toto"));
//        NPath.of("resource:/com.toto:titi#1.3:/toto/titi/tata.txt").copyTo(NPath.of("/toto"));
//
//
//        NElement e = NElements.of().yaml().parse(NPath.of("/toto"));
//
//        NElements.of(e).json().print(NPath.of("/toto"));
//
//        Map a=NWebCli.of().GET("/toto").run().getContentAs(Map.class, NContentType.JSON);
//
//        Double d=NOptional.of(a).then(x->x.b).then(x->x.c).then(x->x.x).orNull();

        List<NPsInfo> nPsInfos = NPs.of().getResultList().toList();
        List<NPsInfo> notepads = nPsInfos.stream().filter(x -> Objects.equals(x.name(), "notepad.exe")).collect(Collectors.toList());
        for (NPsInfo notepad : notepads) {
            NPs.of().killProcess(notepad.pid());
        }
        String str = NElementWriter.ofJson().formatPlain(nPsInfos);
        NElement parsed = NElementReader.ofJson().read(str);
        for (NPsInfo nPsInfo : nPsInfos) {
            NOut.println(nPsInfo);
        }
    }
}
