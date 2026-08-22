/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.cmdline.DefaultNCmdLine;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NEmptyOptionalException;
import org.junit.jupiter.api.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
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
        checkDefaultNArgument(
                new DefaultNArg(null,null),
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
        checkDefaultNArgument(
                new DefaultNArg("",null),
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
        NCmdLine cmdline = NCmdLine.parseDefault(line).get().expandSimpleOptions(false);
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
        NCmdLine c=new DefaultNCmdLine(new String[]{"-1=15"}, NShellFamily.BASH).expandSimpleOptions(true);
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
        Assertions.assertEquals(active,a.isUncommented(),"Enabled:"+ s);
        Assertions.assertEquals(keyValue,a.isKeyValue(),"KeyValue:"+ s);
        Assertions.assertEquals(negated,a.isNegated(),"Negated:"+ s);
        Assertions.assertEquals(key,a.getKey().asString().orNull(),"StringKey:"+ s);
        Assertions.assertEquals(value,a.getStringValue().orNull(),"StringValue:"+ s);
        Assertions.assertEquals(optionName,a.getOptionName().asString().orNull(),"StringOptionName:"+ s);
        Assertions.assertEquals(optionPrefix,a.getOptionPrefix().asString().orNull(),"StringOptionPrefix:"+ s);
        Assertions.assertEquals(eq,a.getSeparator(),"KeyValueSeparator:"+ s);
        TestUtils.println("OK : "+ s);
    }

    // Simple holder mimicking a typical options object populated by a command parser.
    static class Opts {
        boolean full;
        boolean verbose;
        String license;
        String example;
        List<String> paths = new ArrayList<>();
        List<String> positionals = new ArrayList<>();
    }

    private NCmdLine cmd(String... args) {
        return new DefaultNCmdLine(java.util.Arrays.asList(args));
    }

    // ---------------------------------------------------------------
    // 1. FLAG: boolean toggle, never consumes a following token
    // ---------------------------------------------------------------
    @Test
    void testFlag() {
        Opts o = new Opts();
        NCmdLine c = cmd("--full", "positional");

        c.matcher()
                .when("--full", "-f").asFlag(a -> o.full = a.booleanValue())
                .whenNonOption().asArg(a -> o.positionals.add(a.image()))
                .requireAll();

        Assertions.assertTrue(o.full);
        Assertions.assertEquals(Arrays.asList("positional"), o.positionals);
    }

    // ---------------------------------------------------------------
    // 2. FLAG negation: --!k / --~k
    // ---------------------------------------------------------------
    @Test
    void testFlagNegation() {
        Opts o = new Opts();
        o.full = true; // pretend it defaulted to true
        NCmdLine c = cmd("--!full");

        c.matcher()
                .when("--full", "-f").asFlag(a -> o.full = a.booleanValue())
                .requireAll();

        Assertions.assertFalse(o.full);
    }

    // ---------------------------------------------------------------
    // 3. asTrueFlag: only fires the consumer when the flag resolves true
    //    (negated / false flags are consumed but the consumer is skipped)
    // ---------------------------------------------------------------
    @Test
    void testTrueFlagOnlyFiresWhenTrue() {
        AtomicBoolean fired = new AtomicBoolean(false);
        NCmdLine c = cmd("--!verbose");

        c.matcher()
                .when("--verbose", "-v").asTrueFlag(a -> fired.set(true))
                .requireAll();

        Assertions.assertFalse(fired.get(), "negated flag must not trigger asTrueFlag consumer");
    }

    // ---------------------------------------------------------------
    // 4. ENTRY (default/common case): attached '=' form
    // ---------------------------------------------------------------
    @Test
    void testEntryAttached() {
        Opts o = new Opts();
        NCmdLine c = cmd("--license=MIT");

        c.matcher()
                .when("--license").asEntry(a -> o.license = a.stringValue())
                .requireAll();

        Assertions.assertEquals("MIT", o.license);
    }

    // ---------------------------------------------------------------
    // 5. ENTRY: separate-token form, but only when the next token
    //    does not itself look like an option (the "guessing" contract)
    // ---------------------------------------------------------------
    @Test
    void testEntrySeparateToken() {
        Opts o = new Opts();
        NCmdLine c = cmd("--license", "MIT");

        c.matcher()
                .when("--license").asEntry(a -> o.license = a.stringValue())
                .requireAll();

        Assertions.assertEquals("MIT", o.license);
    }

    @Test
    void testEntryDoesNotStealFollowingOption() {
        Opts o = new Opts();
        NCmdLine c = cmd("--license", "--full");

        // --license has no attached value, and the next token looks like an
        // option, so ENTRY must NOT consume it as the value.
        Assertions.assertThrows(NEmptyOptionalException.class,()->{ //NEmptyOptionalException because thrown in a.stringValue()
            c.matcher()
                    .when("--license").asEntry(a -> o.license = a.stringValue())
                    .when("--full").asFlag(a -> o.full = a.booleanValue())
                    .requireAll();
        });

        Assertions.assertNull(o.license);
        Assertions.assertFalse(o.full);// parser is stopped at license
    }

    // ---------------------------------------------------------------
    // 6. ATTACHED_ENTRY: value only via '=', never grabs a following token
    // ---------------------------------------------------------------
    @Test
    void testAttachedEntryIgnoresFollowingToken() {
        AtomicReference<String> value = new AtomicReference<>();
        NCmdLine c = cmd("--color", "always"); // "always" must NOT be consumed

        NCmdLineMatcher m = c.matcher()
                .when("--color").asAttachedEntry(a -> value.set(a.stringValue()))
                .whenNonOption().asArg(a -> { /* "always" lands here instead */ });

        Assertions.assertThrows(NEmptyOptionalException.class, () -> {
            m.requireAll();
        });
    }

    @Test
    void testAttachedEntryWithEquals() {
        AtomicReference<String> value = new AtomicReference<>();
        NCmdLine c = cmd("--color=always");

        c.matcher()
                .when("--color").asAttachedEntry(a -> value.set(a.stringValue()))
                .requireAll();

        Assertions.assertEquals("always", value.get());
    }

    // ---------------------------------------------------------------
    // 7. REQUIRED_ENTRY: value via '=' or unconditional next-token grab
    //    (even if it looks like an option); errors if truly absent.
    // ---------------------------------------------------------------
    @Test
    void testRequiredEntryGrabsOptionLikeValue() {
        AtomicReference<String> javaOpts = new AtomicReference<>();
        // Simulates: nuts -J -Xmx512m  (value legitimately starts with '-')
        NCmdLine c = cmd("-J", "-Xmx512m");

        c.matcher()
                .when("-J", "--java-options").asRequiredEntry(a -> javaOpts.set(a.stringValue()))
                .requireAll();

        Assertions.assertEquals("-Xmx512m", javaOpts.get());
    }

    @Test
    void testRequiredEntryErrorsWhenAbsent() {
        NCmdLine c = cmd("-J"); // nothing follows at all

        Assertions.assertThrows(NCmdLineException.class, () ->
                c.matcher()
                        .when("-J", "--java-options").asRequiredEntry(a -> { /* never reached */ })
                        .requireAll()
        );
    }

    // ---------------------------------------------------------------
    // 8. asRaw: full control over the NCmdLine itself (e.g. --help style
    //    unconditional termination, or conditional termination based on
    //    a runtime check).
    // ---------------------------------------------------------------
    @Test
    void testAsRawUnconditionalTermination() {
        AtomicBoolean helpShown = new AtomicBoolean(false);
        NCmdLine c = cmd("--help", "install", "somepackage");

        c.matcher()
                .when("--help").asRaw(a -> {
                    helpShown.set(true);
                    a.skipAll(); // everything after --help is discarded
                })
                .whenNonOption().asArg(a -> Assertions.fail("should never reach positional args after --help"))
                .requireAll();

        Assertions.assertTrue(helpShown.get());
        Assertions.assertTrue(c.isEmpty());
    }

    @Test
    void testAsRawConditionalTermination() {
        AtomicBoolean fellThrough = new AtomicBoolean(false);
        AtomicBoolean helpShown = new AtomicBoolean(false);
        NCmdLine c = cmd("--unknown-option");

        c.matcher()
                .whenOption().asRaw(a -> {
                    boolean configured = false; // simulate NSession.configureFirst(a) failing
                    if (!configured) {
                        helpShown.set(true);
                        a.skipAll();
                    } else {
                        fellThrough.set(true);
                    }
                })
                .requireAll();

        Assertions.assertTrue(helpShown.get());
        Assertions.assertFalse(fellThrough.get());
    }

    // ---------------------------------------------------------------
    // 9. whenNonOption + asAny: collect bare positional arguments
    // ---------------------------------------------------------------
    @Test
    void testWhenNonOptionCollectsPositionals() {
        Opts o = new Opts();
        NCmdLine c = cmd("install", "somepackage", "--full");

        c.matcher()
                .when("--full").asFlag(a -> o.full = a.booleanValue())
                .whenNonOption().asArg(a -> o.positionals.add(a.image()))
                .requireAll();

        Assertions.assertEquals(Arrays.asList("install", "somepackage"), o.positionals);
        Assertions.assertTrue(o.full);
    }

    // ---------------------------------------------------------------
    // 10. whenArg: single-token predicate, general-purpose shape check
    //     (path-like non-option detection, without manual peek() unwrap)
    // ---------------------------------------------------------------
    @Test
    void testWhenArgPathDetection() {
        List<String> paths = new ArrayList<>();
        NCmdLine c = cmd("./local", "..", "notapath", "/abs/path");

        c.matcher()
                .whenArg(u -> !u.isOption() && u.isNonOption()
                        && (u.image().equals(".") || u.image().equals("..")
                        || u.image().contains("/") || u.image().contains("\\")))
                .asArg(a -> paths.add(a.image()))
                .whenNonOption().asArg(a -> { /* swallow the rest, e.g. "notapath" */ })
                .requireAll();

        Assertions.assertEquals(Arrays.asList("./local", "..", "/abs/path"), paths);
    }

    // ---------------------------------------------------------------
    // 11. whenRaw: full multi-token lookahead predicate (the escape
    //     hatch for conditions that can't be expressed as a single-arg
    //     shape check). Must guard against an empty cmdline itself.
    // ---------------------------------------------------------------
    @Test
    void testWhenRawMultiTokenLookahead() {
        AtomicBoolean matched = new AtomicBoolean(false);
        // Condition: next two tokens are exactly "run" followed by "now"
        NCmdLine c = cmd("run", "now", "extra");

        c.matcher()
                .whenRaw(cl -> cl.hasNext()
                        && "run".equals(cl.get(0).map(NArg::image).orElse(null))
                        && "now".equals(cl.get(1).map(NArg::image).orElse(null)))
                .asRaw(cl -> {
                    matched.set(true);
                    cl.skip(2); // consume "run" and "now"
                })
                .whenNonOption().asArg(a -> { /* consumes "extra" */ })
                .requireAll();

        Assertions.assertTrue(matched.get());
    }

    @Test
    void testWhenRawSafeOnEmptyCmdLine() {
        // Regression test: whenRaw predicates must not throw on an
        // exhausted cmdline (no unguarded peek().get()).
        NCmdLine c = cmd(); // empty

        Assertions.assertDoesNotThrow(() -> {
            boolean any = c.matcher()
                    .whenRaw(cl -> cl.hasNext() && cl.peek().get().isOption())
                    .asRaw(cl -> Assertions.fail("must not match on empty input"))
                    .anyMatch();
            Assertions.assertFalse(any);
        });
    }

    // ---------------------------------------------------------------
    // 12. with(NCmdLineProcessor): mutually-exclusive top-level command
    //     dispatch, first processor to return true wins.
    // ---------------------------------------------------------------
    @Test
    void testWithProcessorDispatch() {
        StringBuilder log = new StringBuilder();
        NCmdLine c = cmd("install", "somepackage");

        boolean handled = c.matcher()
                .with(cl -> doVersion(cl, log))
                .with(cl -> doInstall(cl, log))
                .with(cl -> doUninstall(cl, log))
                .anyMatch();

        Assertions.assertTrue(handled);
        Assertions.assertEquals("install:somepackage", log.toString());
    }

    private boolean doVersion(NCmdLine cl, StringBuilder log) {
        if (!cl.next("version").isPresent()) return false;
        log.append("version");
        return true;
    }

    private boolean doInstall(NCmdLine cl, StringBuilder log) {
        if (!cl.next("install").isPresent()) return false;
        String pkg = cl.next().map(NArg::image).orElse("");
        log.append("install:").append(pkg);
        return true;
    }

    private boolean doUninstall(NCmdLine cl, StringBuilder log) {
        if (!cl.next("uninstall").isPresent()) return false;
        log.append("uninstall");
        return true;
    }

    @Test
    void testWithProcessorDispatchNoMatchFallsThrough() {
        NCmdLine c = cmd("bogus-command");
        StringBuilder log = new StringBuilder();

        boolean handled = c.matcher()
                .with(cl -> doVersion(cl, log))
                .with(cl -> doInstall(cl, log))
                .noMatch();

        Assertions.assertTrue(handled); // noMatch() == true means nothing matched
        Assertions.assertEquals(0, log.length());
    }

    // ---------------------------------------------------------------
    // 13. require() vs requireAll(): throwing behavior
    // ---------------------------------------------------------------
    @Test
    void testRequireThrowsOnUnrecognizedToken() {
        NCmdLine c = cmd("--totally-unknown");

        Assertions.assertThrows(RuntimeException.class, () ->
                c.matcher()
                        .when("--full").asFlag(a -> { })
                        .require() // only tries to match ONE token, then throws
        );
    }

    @Test
    void testRequireAllConsumesEveryToken() {
        Opts o = new Opts();
        NCmdLine c = cmd("--full", "--license=MIT", "positional");

        c.matcher()
                .when("--full").asFlag(a -> o.full = a.booleanValue())
                .when("--license").asEntry(a -> o.license = a.stringValue())
                .whenNonOption().asArg(a -> o.positionals.add(a.image()))
                .requireAll(); // loops require() until cmdline is empty

        Assertions.assertTrue(c.isEmpty());
        Assertions.assertTrue(o.full);
        Assertions.assertEquals("MIT", o.license);
        Assertions.assertEquals(Arrays.asList("positional"), o.positionals);
    }

    // ---------------------------------------------------------------
    // 14. withDefaults(): delegates unmatched tokens to session-level
    //     default configuration (e.g. global options shared across
    //     every nuts command).
    // ---------------------------------------------------------------
    @Test
    void testWithDefaultsAsFallback() {
        NCmdLine c = cmd("--full");
        Opts o = new Opts();

        c.matcher()
                .when("--full").asFlag(a -> o.full = a.booleanValue())
                .withDefaults() // would delegate anything unmatched to NSession.configureFirst
                .requireAll();

        Assertions.assertTrue(o.full);
    }

    // ---------------------------------------------------------------
    // 15. Full realistic composition, mirroring an actual nuts command
    //     parser built from every primitive above.
    // ---------------------------------------------------------------
    @Test
    void testFullRealisticComposition() {
        Opts o = new Opts();
        NCmdLine c = cmd("--full", "-e", "demo1", "--license=MIT", "./src", "leftover");

        c.matcher()
                .when("--full", "-f").asFlag(a -> o.full = a.booleanValue())
                .when("--example", "-e").asEntry(a -> o.example = a.stringValue())
                .when("--license").asAttachedEntry(a -> o.license = a.stringValue())
                .whenArg(u -> !u.isOption() && u.isNonOption() && u.image().startsWith("."))
                .asArg(a -> o.paths.add(a.image()))
                .whenNonOption().asArg(a -> o.positionals.add(a.image()))
                .requireAll();

        Assertions.assertTrue(o.full);
        Assertions.assertEquals("demo1", o.example);
        Assertions.assertEquals("MIT", o.license);
        Assertions.assertEquals(Arrays.asList("./src"), o.paths);
        Assertions.assertEquals(Arrays.asList("leftover"), o.positionals);
        Assertions.assertTrue(c.isEmpty());
    }

}
