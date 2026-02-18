package net.thevpc.nuts.core.test;

import net.thevpc.nuts.runtime.standalone.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConnectionStringTest {

    @Test
    public void test01() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("test").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setPath("/test")
                ,
                test
        );
        Assertions.assertEquals(
                "/test", test.toString()
        );
    }

    @Test
    public void test02() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://user:password@server:1234/folder/file").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setUserName("user")
                        .setPassword("password")
                        .setHost("server")
                        .setPort("1234")
                        .setPath("/folder/file")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://user:password@server:1234/folder/file", test.toString()
        );
    }

    @Test
    public void test03() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://user@server:1234/folder/file").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setUserName("user")
                        .setHost("server")
                        .setPort("1234")
                        .setPath("/folder/file")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://user@server:1234/folder/file", test.toString()
        );
    }

    @Test
    public void test04() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://user@server/folder/file").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setUserName("user")
                        .setHost("server")
                        .setPath("/folder/file")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://user@server/folder/file", test.toString()
        );
    }

    @Test
    public void test05() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://server/folder/file").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setHost("server")
                        .setPath("/folder/file")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://server/folder/file", test.toString()
        );
    }

    @Test
    public void test08() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://me:ok@192.168.1.89").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setUserName("me")
                        .setPassword("ok")
                        .setHost("192.168.1.89")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://me:ok@192.168.1.89", test.toString()
        );
    }

    @Test
    public void test06() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("ssh://server").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("ssh")
                        .setHost("server")
                ,
                test
        );
        Assertions.assertEquals(
                "ssh://server", test.toString()
        );
    }

    @Test
    public void test07() {
        NConnectionStringBuilder test = DefaultNConnectionStringBuilder.of("file:/folder/file").get();
        Assertions.assertEquals(
                new DefaultNConnectionStringBuilder()
                        .setProtocol("file")
                        .setPath("/folder/file")
                ,
                test
        );
        Assertions.assertEquals(
                "file:/folder/file", test.toString()
        );
    }



}
