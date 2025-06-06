/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class CmdLineTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test1() throws Exception {
        NArg[] cmd = NCmdLine.parseDefault("-ad+ +ad--").get().toArgumentArray();
        Set<String> set = Arrays.stream(cmd).map(Object::toString).collect(Collectors.toSet());
        Set<String> expectedSet = new HashSet<>(Arrays.asList(
                "-a", "-d+", "+a","+d--"
        ));
        Assertions.assertEquals(expectedSet,set);
    }

    @Test
    public void test2() throws Exception {
        NCmdLine cmd = new DefaultNCmdLine().registerSpecialSimpleOption("-version");
        Assertions.assertEquals(true,cmd.isSpecialSimpleOption("-//version"));
    }



    @Test
    public void testArgument01() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg(null),
                true,
                false,
                false,
                false,
                null,
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument02() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg(""),
                true,
                false,
                false,
                false,
                "",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument03() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("hello"),
                true,
                false,
                false,
                false,
                "hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument04() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("!hello"),
                true,
                false,
                false,
                false,
                "!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument05() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("//!hello"),
                true,
                false,
                false,
                false,
                "//!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument06() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("/!hello"),
                true,
                false,
                false,
                false,
                "/!hello",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument07() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("/!hello=me"),
                true,
                false,
                false,
                false,
                "/!hello=me",
                null,
                null,
                null,
                "="
        );
    }

    @Test
    public void testArgument08() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("--!hello=me"),
                true,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
    }

    @Test
    public void testArgument09() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("--//!hello=me"),
                false,
                true,
                true,
                true,
                "--hello",
                "me",
                "hello",
                "--",
                "="
        );
    }


    @Test
    public void testArgument10() {
        NElements elems = NElements.of();
        checkDefaultNArgument(
                new DefaultNArg("--//="),
                false,
                true,
                true,
                false,
                "--",
                "",
                "",
                "--",
                "="
        );
    }
    @Test
    public void testArgument11() {
        String line0="start -Djava.util.logging.config.file=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Dnuts-config-name=default -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 -Dignore.endorsed.dirs= -Dcatalina.base=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default -Dcatalina.home=/home/me/.local/share/nuts/apps/default-workspace/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 -Djava.io.tmpdir=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp";
        String line="-Dcatalina.base=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default -Dcatalina.home=/home/me/.local/share/nuts/apps/default-workspace/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 -Djava.io.tmpdir=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp ";
        NCmdLine cmdline = NCmdLine.parseDefault(line).get().setExpandSimpleOptions(false);
        NArg a=null;
        int x=0;
        while(cmdline.hasNext()){
            if((a=cmdline.nextEntry("-Dcatalina.home").orNull())!=null) {
                NPath.of(a.getStringValue().get());
                x++;
            }else if((a=cmdline.nextEntry("-Dcatalina.base").orNull())!=null){
                a.getStringValue().get();
                x++;
            }else{
                cmdline.skip();
            }
        }
        Assertions.assertEquals(2,x);
    }

    @Test
    public void testArgument12(){
        String s="-Dcatalina.base=/home/me/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default";
        DefaultNArg a=new DefaultNArg(s);
        Assertions.assertEquals("-Dcatalina.base",a.getStringKey().get());
    }

    @Test
    public void testArgument13(){
        NCmdLine c=new DefaultNCmdLine(new String[]{"-1=15"}, NShellFamily.BASH).setExpandSimpleOptions(true);
        NArg a = c.next().get();
        Assertions.assertEquals("-1",a.getStringKey().get());
    }

    private static void checkDefaultNArgument(NArg a, boolean active, boolean option, boolean keyValue, boolean negated
            , String key
            , String value
            , String optionName
            , String optionPrefix
            , String eq
    ){
        String s = a.asString().orNull();
        Assertions.assertEquals(option,a.isOption(),"Option:"+ s);
        Assertions.assertEquals(active,a.isNonCommented(),"Enabled:"+ s);
        Assertions.assertEquals(keyValue,a.isKeyValue(),"KeyValue:"+ s);
        Assertions.assertEquals(negated,a.isNegated(),"Negated:"+ s);
        Assertions.assertEquals(key,a.getKey().asString().orNull(),"StringKey:"+ s);
        Assertions.assertEquals(value,a.getStringValue().orNull(),"StringValue:"+ s);
        Assertions.assertEquals(optionName,a.getOptionName().asString().orNull(),"StringOptionName:"+ s);
        Assertions.assertEquals(optionPrefix,a.getOptionPrefix().asString().orNull(),"StringOptionPrefix:"+ s);
        Assertions.assertEquals(eq,a.getSeparator(),"KeyValueSeparator:"+ s);
        TestUtils.println("OK : "+ s);
    }
}
