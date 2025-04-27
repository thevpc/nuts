package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NEvictingCharQueue;
import net.thevpc.nuts.util.NPlatformSignature;
import net.thevpc.nuts.util.NPlatformSignatureMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CollectionsTest {

    @Test
    public void testEvictingCharQueue1(){
        NEvictingCharQueue q=new NEvictingCharQueue(1);
        TestUtils.println(q);

        q.add('a');
        Assertions.assertEquals("a",q.toString());
        q.add('b');
        Assertions.assertEquals("b",q.toString());
        q.add('c');
        Assertions.assertEquals("c",q.toString());
        q.add('d');
        Assertions.assertEquals("d",q.toString());
        q.add('e');
        Assertions.assertEquals("e",q.toString());
        q.add('f');
        Assertions.assertEquals("f",q.toString());
        q.add('g');
        Assertions.assertEquals("g",q.toString());
    }

    @Test
    public void testEvictingCharQueue2(){
        NEvictingCharQueue q=new NEvictingCharQueue(2);
        TestUtils.println(q);

        q.add('a');
        Assertions.assertEquals("a",q.toString());
        q.add('b');
        Assertions.assertEquals("ab",q.toString());
        q.add('c');
        Assertions.assertEquals("bc",q.toString());
        q.add('d');
        Assertions.assertEquals("cd",q.toString());
        q.add('e');
        Assertions.assertEquals("de",q.toString());
        q.add('f');
        Assertions.assertEquals("ef",q.toString());
        q.add('g');
        Assertions.assertEquals("fg",q.toString());
    }

    @Test
    public void testEvictingCharQueue3(){
        NEvictingCharQueue q=new NEvictingCharQueue(3);
        TestUtils.println(q);

        q.add('a');
        Assertions.assertEquals("a",q.toString());
        q.add('b');
        Assertions.assertEquals("ab",q.toString());
        q.add('c');
        Assertions.assertEquals("abc",q.toString());
        q.add('d');
        Assertions.assertEquals("bcd",q.toString());
        q.add('e');
        Assertions.assertEquals("cde",q.toString());
        q.add('f');
        Assertions.assertEquals("def",q.toString());
        q.add('g');
        Assertions.assertEquals("efg",q.toString());
    }

    @Test
    public void test01() {
        NPlatformSignatureMap<NPlatformSignature> cm=new NPlatformSignatureMap<NPlatformSignature>(NPlatformSignature.class);
        cm.put(NPlatformSignature.of(CharSequence.class,int.class), NPlatformSignature.of(CharSequence.class,int.class));
        cm.put(NPlatformSignature.of(String.class,Number.class), NPlatformSignature.of(String.class,Number.class));
        Assertions.assertEquals(
                NPlatformSignature.of(CharSequence.class,int.class),
                cm.get(NPlatformSignature.of(String.class,Integer.class)).get()
        );
    }

}
