/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.DefaultNutsArgument;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class Test14_CommandLineTest {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test1() throws Exception {
        NutsArgument[] cmd = NutsCommandLine.parseDefault("-ad+ +ad--").get(session).toArgumentArray();
        Set<String> set = Arrays.stream(cmd).map(x -> x.toString()).collect(Collectors.toSet());
        Set<String> expectedSet = new HashSet<>(Arrays.asList(
                "-a", "-d+", "+a","+d--"
        ));
        Assertions.assertEquals(set,expectedSet);
    }



    @Test
    public void testArgument01() {
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument(null),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument(""),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("hello"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("!hello"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("//!hello"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("/!hello=me"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--!hello=me"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//!hello=me"),
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
        NutsElements elems = NutsElements.of(session);
        checkDefaultNutsArgument(
                new DefaultNutsArgument("--//="),
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
        String line0="start -Djava.util.logging.config.file=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/conf/logging.properties -Djava.util.logging.manager=org.apache.juli.ClassLoaderLogManager -Dnuts-config-name=default -Djdk.tls.ephemeralDHKeySize=2048 -Djava.protocol.handler.pkgs=org.apache.catalina.webresources -Dorg.apache.catalina.security.SecurityListener.UMASK=0027 -Dignore.endorsed.dirs= -Dcatalina.base=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default -Dcatalina.home=/home/vpc/.local/share/nuts/apps/default-workspace/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 -Djava.io.tmpdir=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp";
        String line="-Dcatalina.base=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default -Dcatalina.home=/home/vpc/.local/share/nuts/apps/default-workspace/id/org/apache/catalina/apache-tomcat/10.0.0-M1/apache-tomcat-10.0.0-M1 -Djava.io.tmpdir=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default/temp ";
        NutsCommandLine cmdline = NutsCommandLine.parseDefault(line).get(session).setExpandSimpleOptions(false);
        NutsArgument a=null;
        int x=0;
        while(cmdline.hasNext()){
            if((a=cmdline.nextString("-Dcatalina.home").orNull())!=null) {
                NutsPath.of(a.getStringValue().get(session),session);
                x++;
            }else if((a=cmdline.nextString("-Dcatalina.base").orNull())!=null){
                a.getStringValue().get(session);
                x++;
            }else{
                cmdline.skip();
            }
        }
        Assertions.assertEquals(2,x);
    }
    @Test
    public void testArgument12(){
        String s="-Dcatalina.base=/home/vpc/.config/nuts/default-workspace/config/id/net/thevpc/nuts/toolbox/ntomcat/SHARED/catalina-base-10.0/default";
        DefaultNutsArgument a=new DefaultNutsArgument(s);
        Assertions.assertEquals("-Dcatalina.base",a.getStringKey().get());
    }

    private static void checkDefaultNutsArgument(NutsArgument a, boolean active, boolean option, boolean keyValue, boolean negated
            , String key
            , String value
            , String optionName
            , String optionPrefix
            , String eq
    ){
        String s = a.asString().orNull();
        Assertions.assertEquals(option,a.isOption(),"Option:"+ s);
        Assertions.assertEquals(active,a.isActive(),"Enabled:"+ s);
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
