package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

public class SshTest {
    static {
        TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test01() {
        List<NPath> list = NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/")
                .list();
        TestUtils.println(list);
    }

    @Test
    public void test02() {
        NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/toDeleteFolder")
                .delete();
    }

    @Test
    public void test03() {
        NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/toDeleteFile")
                .delete();
    }


    @Test
    public void test04() {
        NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/")
                .walk().forEach(x->{
                    TestUtils.println(x);
                });
    }

    @Test
    public void test05() {
        TestUtils.println(NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/").type());
        Assertions.assertEquals(NPathType.DIRECTORY,NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/").type());

        TestUtils.println(NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/FolderNotFound").type());
        Assertions.assertEquals(NPathType.FILE,NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/FolderNotFound").type());

        TestUtils.println(NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/Programs").type());
        TestUtils.println(NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/tmp.txt").type());
    }

    @Test
    public void test06() {
        NPath.of("ssh://Administrateur@fvm.veoni.tn/C:/Folder/To/Create").mkdirs();
    }

    @Test
    public void test07() {
        long l = NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp.txt").contentLength();
        String en = NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp.txt").contentEncoding();
        String ct = NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp.txt").getContentType();
        String cs = NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp.txt").getCharset();
    }

    @Test
    public void test08() {
        NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp.txt")
                .copyTo(NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp2.txt"))
                ;
        NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp2.txt").moveTo(
                NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp3.txt")
        );
        NPath.of("ssh://Administrateur@fvm.veoni.tn/tmp3.txt").delete();
    }




}
