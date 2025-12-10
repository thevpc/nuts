package net.thevpc.nuts.core.test;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathType;
import net.thevpc.nuts.util.NHex;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

public class SshTest {
    static {
        TestUtils.openNewTestWorkspace();
    }

    // disable test before commit
    //@Test
    public void test01() {
        List<NPath> list = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/")
                .list();
        TestUtils.println(list);
    }

    // disable test before commit
    //@Test
    public void test02() {
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test.txt")
                .delete();
    }

    @Test
    public void test03() {
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/toDeleteFile")
                .delete();
    }


    // disable test before commit
    //@Test
    public void test04() {
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/");
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/")
                .walk().forEach(x -> {
                    TestUtils.println(x);
                });
    }

    // disable test before commit
    //@Test
    public void test05() {
        TestUtils.println(NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/").type());
        Assertions.assertEquals(NPathType.DIRECTORY, NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/").type());

        TestUtils.println(NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/FolderNotFound").type());
        Assertions.assertEquals(NPathType.FILE, NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/FolderNotFound").type());

        TestUtils.println(NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Programs").type());
        TestUtils.println(NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/tmp.txt").type());
    }

    // disable test before commit
    //@Test
    public void test06() {
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Folder/To/Create").mkdir();
    }

    // disable test before commit
    //@Test
    public void test07() {
        long l = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/test.txt").getContentLength();
//        String en = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/tmp.txt").contentEncoding();
//        String ct = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/tmp.txt").getContentType();
//        String cs = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/tmp.txt").getCharset();
        System.out.println(l);
    }

    // disable test before commit
    //@Test
    public void test08() {
//        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test")
//                .copyTo(NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/meriem/"))
//        ;
        NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test10").moveTo(
               NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test11")
       );
        //NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/tmp3.txt").delete();
    }

    // disable test before commit
    //@Test
    public void test09() {
        NPathType result = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test.txt")
                .type();
        TestUtils.println(result);
    }

    // disable test before commit
    //@Test
    public void test10() {
        NPath remotePath = NPath.of("ssh://"+ TestUtils.WINDOWS_TEST_SERVER +"/C:/Users/Administrateur/test.txt");
        byte[] digest = remotePath.getDigest("SHA-256");
        TestUtils.println(NHex.fromBytes(digest));
    }
}
