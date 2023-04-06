import net.thevpc.nuts.toolbox.ndb.util.DbUrlString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoginServerPathTest {

    @Test
    public void tes1() {
        check("user:password@server:1/12", new DbUrlString().setDbUser("user").setDbPassword("password").setDbServer("server").setDbPort(1).setDbPath("12"));
        check("ssh:u:p@s:12/du:dp@ds:13", new DbUrlString()
                .setSshUser("u").setSshPassword("p").setSshServer("s").setSshPort(12)
                .setDbUser("du").setDbPassword("dp").setDbServer("ds").setDbPort(13)
        );
        check("ssh:u@s/ds", new DbUrlString()
                .setSshUser("u").setSshServer("s")
                .setDbPath("ds")
        );
    }

    private void check(String str, DbUrlString expected) {
        System.out.println("   "+str);
        DbUrlString p = DbUrlString.parse(str).get();
        System.out.println("==>"+p.toUrl());
        Assertions.assertEquals(expected, p);
        Assertions.assertEquals(str, p.toUrl());
    }
}
