package net.thevpc.nuts.core.test.manual;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NContentType;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.web.NWebCli;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PsManualTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }
    class A{
        B b;
    }
    class B{
        C c;
    }
    class C{
        double x;
    }
    @Test
    public void test1(){
        NOut.println("Hello ##world##");
//        System.out.printf("hello %s","toto");
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
        List<NPsInfo> notepads = nPsInfos.stream().filter(x -> Objects.equals(x.getName(), "notepad.exe")).collect(Collectors.toList());
        for (NPsInfo notepad : notepads) {
            NPs.of().killProcess(notepad.getPid());
        }
        String str = NElements.ofPlainJson(nPsInfos).formatPlain();
        NElement parsed = NElements.ofPlainJson().parse(str);
        for (NPsInfo nPsInfo : nPsInfos) {
            NOut.print(nPsInfo);
        }
    }
}
