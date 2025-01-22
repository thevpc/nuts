package test;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.processor.base.TagStreamProcessor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class DollarBracket2VarStreamProcessorTest {

    public void test(String template,String result){
        NWorkspace ws = Nuts.openWorkspace(
                "-ZySbyKk",
                "--!init-java",
                "--!init-launchers",
                "-w", "test6");
        ws.runWith(()->{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            TagStreamProcessor.DOLLAR_BARACKET2.processStream(
                    new ByteArrayInputStream(template.getBytes()),
                    out,
                    new NDocContext()
            );
            System.out.println(out);
            Assertions.assertEquals(result,out.toString());
        });
    }

    @Test
    public void test1(){
        test("aa ${{'b'}} c","aa b c");
    }
    @Test
    public void test2(){
        test("aa }${{'b'}} c","aa }b c");
    }

    @Test
    public void test3(){
        test("aa ${b} c","aa ${b} c");
    }

    @Test
    public void test4(){
        test("aa ${{} c","aa ");
    }
    @Test
    public void test5(){
        test("aa ${{}} c","aa  c");
    }
}
