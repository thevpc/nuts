import net.thevpc.nuts.toolbox.ndb.util.DbUrlString;
import net.thevpc.nuts.util.NConnexionString;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoginServerPathTest {

    @Test
    public void tes1() {
        check("user:password@server:1/12", new DbUrlString()
                .setDb(new NConnexionString().setUser("user").setPassword("password").setHost("server").setPort(String.valueOf(1)).setPath("12"))
        );
        check("ssh://u:p@s:12/du:dp@ds:13", new DbUrlString()
                .setSsh(new NConnexionString().setProtocol("ssh").setUser("u").setPassword("p").setHost("s").setPort("12"))
                .setDb(new NConnexionString().setUser("du").setPassword("dp").setHost("ds").setPort("13"))
        );
        check("ssh://u@s/ds", new DbUrlString()
                .setSsh(new NConnexionString().setProtocol("ssh").setUser("u").setHost("s"))
                .setDb(new NConnexionString().setPath("ds"))
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
