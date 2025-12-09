package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NOut;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.runtime.standalone.xtra.ps.LinuxPsParser;
import net.thevpc.nuts.runtime.standalone.xtra.ps.UnixPsParser;
import net.thevpc.nuts.runtime.standalone.xtra.ps.WindowsPs1Parser;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
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
                NOut.println(nPsInfo);
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
                NOut.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test03() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/windows-ps-result.txt"), Charset.forName("windows-1252"))) {
            WindowsPs1Parser p = new WindowsPs1Parser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                NOut.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test05() {
        try (Reader r = new InputStreamReader(PsTest.class.getClassLoader().getResourceAsStream("net/thevpc/nuts/core/test/windows-ps-result-10.txt"), Charset.forName("windows-1252"))) {
            WindowsPs1Parser p = new WindowsPs1Parser();
            List<NPsInfo> parsed = p.parse(r).toList();
            for (NPsInfo nPsInfo : parsed) {
                NOut.println(nPsInfo);
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException(ex);
        }
    }

    @Test
    public void test06() {
        for (NPsInfo nPsInfo : NPs.of().getResultList()) {
            NOut.println(nPsInfo);
        }
    }

    @Test
    public void test07() {
        for (NPsInfo nPsInfo : NPs.of().at("ssh://vpc@thevpc.net").getResultList()) {
            NOut.println(nPsInfo);
        }
    }

    @Test
    public void test08() {
        byte[] bytes = NPath.of("/home/meryem/aaa.bin").readBytes();
        try (StringReader br = new StringReader(new String(bytes, Charset.forName("utf-8")))) {
            for (NPsInfo nPsInfo : new WindowsPs1Parser().parse(br).toList()) {
                System.out.println(nPsInfo);
            }
        }

//        for (NPsInfo nPsInfo : NPs.of().at("ssh://Administrateur@fvm.veoni.tn").getResultList()) {
//            NOut.println(nPsInfo);
//        }
    }

}
