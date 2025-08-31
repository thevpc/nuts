package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.runtime.standalone.xtra.ps.LinuxPsParser;
import net.thevpc.nuts.runtime.standalone.xtra.ps.UnixPsParser;
import net.thevpc.nuts.runtime.standalone.xtra.ps.WindowsPsParser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.util.List;

public class PsTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/linux-ps-result.txt"))) {
            LinuxPsParser p = new LinuxPsParser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                System.out.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test02() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/unix-ps-result.txt"))) {
            UnixPsParser p = new UnixPsParser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                System.out.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test03() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/windows-ps-result.txt"), Charset.forName("windows-1252"))) {
            WindowsPsParser p = new WindowsPsParser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                System.out.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test05() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/windows-ps-result-10.txt"), Charset.forName("windows-1252"))) {
            WindowsPsParser p = new WindowsPsParser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                System.out.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test06() {
        for (NPsInfo nPsInfo : NPs.of().getResultList()) {
            System.out.println(nPsInfo);
        }
    }

}
