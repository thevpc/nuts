package net.thevpc.nuts.core.test.manual;

import net.thevpc.nuts.NOut;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLines;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.io.NPsInfo;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PsManualTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }
    @Test
    public void test1(){
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
