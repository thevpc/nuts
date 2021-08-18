package net.thevpc.nuts.toolbox.nadmin.test;

import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsApplications;
import net.thevpc.nuts.toolbox.nadmin.NAdminMain;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.sys.win.WindowsNdi;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.ReplaceString;
import net.thevpc.nuts.toolbox.nadmin.util._IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TestAddFileLine {
    @Test
    public void testAddLineMulti(){
        NAdminMain app = new NAdminMain();
        NutsApplicationContext applicationContext = NutsApplications.createApplicationContext(app, new String[0], null);
        WindowsNdi w=new WindowsNdi(applicationContext);
        Path f = Paths.get(System.getProperty("user.home")).resolve("testAddLineMulti.txt");
        try {
            try {
                Files.deleteIfExists(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String before=_IOUtils.loadFileContentLenientString(f);
            Assertions.assertEquals("",before);
            w.addFileLine(NdiScriptInfoType.NUTS, applicationContext.getAppId(),
                    f,
                    new ReplaceString("some-comments", "some-comments"),
                    "line1\nline2\nline3",
                    new ReplaceString("<HEADER>", "<HEADER>")
            );
            String after1=_IOUtils.loadFileContentLenientString(f);
            w.addFileLine(NdiScriptInfoType.NUTS, applicationContext.getAppId(),
                    f,
                    new ReplaceString("some-comments", "some-comments"),
                    "line1\nline2\nline3",
                    new ReplaceString("<HEADER>", "<HEADER>")
            );
            String after2=_IOUtils.loadFileContentLenientString(f);
            Assertions.assertEquals(after1,after2);
            String specific=after2+System.getProperty("line.separator")+"Some Specific Text";
            Files.write(f,specific.getBytes());
            String after3=_IOUtils.loadFileContentLenientString(f);

        }catch (IOException e){
            throw new UncheckedIOException(e);
        }finally {
            try {
                Files.deleteIfExists(f);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
