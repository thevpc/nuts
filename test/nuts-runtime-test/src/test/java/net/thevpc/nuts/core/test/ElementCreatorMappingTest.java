package net.thevpc.nuts.core.test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.artifact.NDependency;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NCreator;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NMapBy;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.elem.NProperty;
import net.thevpc.nuts.elem.NToElement;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NNameFormat;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ElementCreatorMappingTest {

    @BeforeAll
    static void setUpWorkspace() {
        Nuts.require();
    }

    private static NElement tson(String s) {
        return NElementReader.ofTson().read(s);
    }

    private static <T> T from(String source, Class<T> type) {
        return type.cast(NElement.defaultFromElement(tson(source), type).get());
    }

    private static String messageOf(Throwable t) {
        List<String> messages = new ArrayList<>();
        for (Throwable c = t; c != null; c = c.getCause()) {
            if (c.getMessage() != null) {
                messages.add(c.getMessage());
            }
            if (c.getCause() == c) {
                break;
            }
        }
        return String.join(" ; ", messages) + " ; " + t.getClass().getName();
    }

    @Test
    void chronometerRoundTripUsesNCreatorAndNProperty() {
        NChronometer original = NChronometer.of("task").start();
        NElement element = NElement.defaultToElement(original);
        NChronometer copy = NElement.defaultFromElement(element, NChronometer.class).get();
        assertNotNull(copy);
        assertEquals(original.name(), copy.name());
        assertEquals(original.startClock(), copy.startClock());
        assertEquals(element.toString(), NElement.defaultToElement(copy).toString());
    }

    @Test
    void mapClassMarkerIsDormantAndDoesNotAffectMapping() {
        DormantMapper o = from("{a:7}", DormantMapper.class);
        assertEquals(7, o.a);
    }

    @Test
    void elementParameterShapes() {
        Shape1 s1 = from("{a:1}", Shape1.class);
        assertEquals("element-shape1", s1.mark);

        Shape2 s2 = from("{a:1}", Shape2.class);
        assertEquals("element-shape2", s2.mark);

        Shape3 s3 = from("{a:1}", Shape3.class);
        assertEquals("element-shape3", s3.mark);
    }

    @Test
    void propertyBoundShapes() {
        Shape4 s4 = from("{a:1,b:\"x\"}", Shape4.class);
        assertEquals(1, s4.a);
        assertEquals("x", s4.b);

        Shape5 s5 = from("{a:2}", Shape5.class);
        assertEquals(2, s5.a);

        Shape6 s6 = from("{a:3}", Shape6.class);
        assertEquals(3, s6.a);
    }

    @Test
    void elementSubtypeOverloadsEachReceiveTheRightKind() {
        Overloads object = from("{a:1}", Overloads.class);
        assertEquals("object", object.mark);

        Overloads array = from("[1,2]", Overloads.class);
        assertEquals("array", array.mark);
    }

    @Test
    void elementParameterAcceptsObjectArrayAndPrimitiveElements() {
        assertEquals("OBJECT", from("{a:1}", AnyElement.class).mark);
        assertEquals("ARRAY", from("[1,2]", AnyElement.class).mark);
        assertEquals("INT", from("12", AnyElement.class).mark);
    }

    @Test
    void elementTypeMismatchReportsExpectedAndActualKinds() {
        Throwable ex = assertThrows(RuntimeException.class, () -> from("[1,2]", ObjectOnly.class));
        String message = messageOf(ex);
        assertTrue(message.contains("cannot map ARRAY element"), message);
        assertTrue(message.contains("NObjectElement"), message);
        assertTrue(message.contains(ObjectOnly.class.getName()), message);
    }

    @Test
    void emptyOptionalDeclinesAndFallsBackToTheNextStrategy() {
        Declining o = from("{x:7}", Declining.class);
        assertEquals(0, o.createdBy);
        assertEquals(7, o.x);
    }

    @Test
    void presentOptionalIsUsedAndCreatorFailureIsPropagated() {
        Declining o = from("{x:1,accept:true}", Declining.class);
        assertEquals(1, o.createdBy);
        assertEquals(1, o.x);

        Throwable ex = assertThrows(RuntimeException.class, () -> from("{x:1}", Boom.class));
        String message = messageOf(ex);
        assertTrue(message.contains("boom"), message);
        assertTrue(message.contains("creator"), message);
    }

    @Test
    void duplicatedCreatorsOfTheSameShapeAreAmbiguous() {
        Throwable ex = assertThrows(RuntimeException.class, () -> from("{a:1}", DuplicateProperties.class));
        String message = messageOf(ex);
        assertTrue(message.contains("share the same parameter shape"), message);
        assertTrue(message.contains(DuplicateProperties.class.getName()), message);

        Throwable ex2 = assertThrows(RuntimeException.class, () -> from("{a:1}", DuplicateElements.class));
        String message2 = messageOf(ex2);
        assertTrue(message2.contains("share the same parameter shape"), message2);
        assertTrue(message2.contains("NObjectElement"), message2);
    }

    @Test
    void propertyNamesMayDifferFromJavaParameterNames() {
        Renamed o = from("{x_coord:3,y_coord:4}", Renamed.class);
        assertEquals(3, o.x);
        assertEquals(4, o.y);
    }

    @Test
    void elementCreatorWinsOverPropertyBoundCreator() {
        BothKinds o = from("{a:5}", BothKinds.class);
        assertEquals("element", o.mark);
        assertEquals(0, o.a);
    }

    @Test
    void decliningElementCreatorFallsThroughToThePropertyBoundCreator() {
        DecliningElementThenProperty o = from("{a:9}", DecliningElementThenProperty.class);
        assertEquals("property", o.mark);
        assertEquals(9, o.a);
    }

    @Test
    void noCreatorAcceptedTheElementWhenNoDefaultMappingIsPossible() {
        Throwable ex = assertThrows(RuntimeException.class, () -> from("\"hello\"", DecliningNoFallback.class));
        String message = messageOf(ex);
        assertTrue(message.contains("no creator accepted"), message);
        assertTrue(message.contains(DecliningNoFallback.class.getName()), message);
    }

    @Test
    void migratedLibraryTypesStillRoundTrip() {
        NClock clock = NClock.now();
        NElement clockElement = NElement.defaultToElement(clock);
        assertEquals(clock, NElement.defaultFromElement(clockElement, NClock.class).get());

        NDuration duration = NDuration.ofMillis(1500);
        assertEquals(duration, NElement.defaultFromElement(
                NElement.defaultToElement(duration), NDuration.class).get());

        NElement format = NElement.defaultToElement(NNameFormat.LOWER_SNAKE_CASE);
        NNameFormat copy = NElement.defaultFromElement(format, NNameFormat.class).get();
        assertEquals(format.toString(), NElement.defaultToElement(copy).toString());
    }

    @NMapBy(mapClass = Object.class)
    public static class DormantMapper {
        int a;

        @NCreator
        public DormantMapper(@NProperty("a") int a) {
            this.a = a;
        }
    }

    public static class Shape1 {
        final String mark;

        @NCreator
        public Shape1(NElement e) {
            mark = e.asObject().get().get("a").isPresent() ? "element-shape1" : "empty";
        }
    }

    public static class Shape2 {
        String mark;

        public static Shape2 of(NElement e) {
            Shape2 o = new Shape2();
            o.mark = "element-shape2";
            return o;
        }
    }

    public static class Shape3 {
        String mark;

        public static NOptional<Shape3> of(NElement e) {
            Shape3 o = new Shape3();
            o.mark = "element-shape3";
            return NOptional.of(o);
        }
    }

    public static class Shape4 {
        final int a;
        final String b;

        @NCreator
        public Shape4(@NProperty("a") int a, @NProperty("b") String b) {
            this.a = a;
            this.b = b;
        }
    }

    public static class Shape5 {
        int a;

        public Shape5() {
        }

        @NCreator
        public static Shape5 of(@NProperty("a") int a) {
            Shape5 o = new Shape5();
            o.a = a;
            return o;
        }
    }

    public static class Shape6 {
        int a;

        public Shape6() {
        }

        @NCreator
        public static NOptional<Shape6> of(@NProperty("a") int a) {
            Shape6 o = new Shape6();
            o.a = a;
            return NOptional.of(o);
        }
    }

    public static class Overloads {
        String mark;

        public Overloads() {
        }

        public static Overloads of(NObjectElement e) {
            Overloads o = new Overloads();
            o.mark = "object";
            return o;
        }

        public static Overloads of(NArrayElement e) {
            Overloads o = new Overloads();
            o.mark = "array";
            return o;
        }
    }

    public static class AnyElement {
        String mark;

        public static AnyElement of(NElement e) {
            AnyElement o = new AnyElement();
            o.mark = e.type().name();
            return o;
        }
    }

    public static class ObjectOnly {
        String mark;

        public static ObjectOnly of(NObjectElement e) {
            ObjectOnly o = new ObjectOnly();
            o.mark = "object";
            return o;
        }
    }

    public static class Declining {
        int x;
        int createdBy;

        public Declining() {
        }

        @NCreator
        public static NOptional<Declining> of(@NProperty("x") int x, @NProperty("accept") boolean accept) {
            if (!accept) {
                return NOptional.ofEmpty();
            }
            Declining o = new Declining();
            o.x = x;
            o.createdBy = 1;
            return NOptional.of(o);
        }
    }

    public static class DecliningNoFallback {
        final int x;

        private DecliningNoFallback(int x) {
            this.x = x;
        }

        @NCreator
        public static NOptional<DecliningNoFallback> of(NElement e) {
            return NOptional.ofEmpty();
        }
    }

    public static class Boom {
        @NCreator
        public static Boom of(NElement e) {
            throw new IllegalStateException("boom");
        }
    }

    public static class DuplicateProperties {
        @NCreator
        public DuplicateProperties(@NProperty("a") int a) {
        }

        @NCreator
        public DuplicateProperties(@NProperty("b") String b) {
        }
    }

    public static class DuplicateElements {
        @NCreator
        public DuplicateElements(NObjectElement e) {
        }

        @NCreator
        public static DuplicateElements of(NObjectElement e) {
            return new DuplicateElements(e);
        }
    }

    public static class Renamed {
        final int x;
        final int y;

        @NCreator
        public Renamed(@NProperty("x_coord") int x, @NProperty("y_coord") int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static class BothKinds {
        final String mark;
        final int a;

        @NCreator
        public BothKinds(@NProperty("a") int a) {
            this.mark = "property";
            this.a = a;
        }

        @NCreator
        public static BothKinds of(NObjectElement e) {
            return new BothKinds("element", 0);
        }

        private BothKinds(String mark, int a) {
            this.mark = mark;
            this.a = a;
        }
    }

    public static class DecliningElementThenProperty {
        final String mark;
        final int a;

        @NCreator
        public DecliningElementThenProperty(@NProperty("a") int a) {
            this.mark = "property";
            this.a = a;
        }

        @NCreator
        public static NOptional<DecliningElementThenProperty> of(NElement e) {
            return NOptional.ofEmpty();
        }
    }

    @Test
    void builtinMappersStillHonorNToElementOnTheDefaultPath() {
        // NElementMapperNDependency serializes through the default path with an NText; a plain NText is
        // an NToElement and must stay a string, not a reflective dump of the text (this corrupted
        // eff-nuts.cache and dropped every dependency on reload)
        NDependency dependency = NDependency.get("net.thevpc.nuts:nuts#1.1.0").get();
        NElement element = NElement.of(dependency);
        assertTrue(element.type().isAnyString(), String.valueOf(element));
        assertEquals(dependency, NDependency.get(element.asStringValue().get()).get());
    }

    @Test
    void defaultToNamedElementInsideToElementDoesNotRecurse() {
        SelfMapped original = new SelfMapped(3, "x");
        NElement element = original.toElement();
        NObjectElement object = element.asObject().get();
        assertEquals("SelfMapped", object.name().orNull());
        assertEquals(3, object.getIntValue("a").get());
        assertEquals("x", object.getStringValue("b").get());
        assertEquals(element.toString(), NElement.of(original).toString());
        SelfMapped copy = NElement.defaultFromElement(element, SelfMapped.class).get();
        assertEquals(3, copy.a);
        assertEquals("x", copy.b);
    }

    public static class SelfMapped implements NToElement {
        final int a;
        final String b;

        @NCreator
        public SelfMapped(@NProperty("a") int a, @NProperty("b") String b) {
            this.a = a;
            this.b = b;
        }

        public int getA() {
            return a;
        }

        public String getB() {
            return b;
        }

        @Override
        public NElement toElement() {
            return NElement.defaultToNamedElement(this);
        }
    }

}
