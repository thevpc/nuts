package net.thevpc.nuts.platform;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.internal.rpi.NUtilsRPI;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

import java.io.Serializable;

public interface NRuntimeDistribution extends Serializable, Cloneable {

    // ── JAVA ──
    String JAVA_PRODUCT_JDK = "jdk";
    String JAVA_PRODUCT_JRE = "jre";
    String JAVA_VENDOR_TEMURIN = "temurin";
    String JAVA_VENDOR_GRAALVM = "graalvm";
    String JAVA_VENDOR_ORACLE = "oracle";
    String JAVA_VENDOR_CORRETTO = "corretto";
    String JAVA_VENDOR_ZULU = "zulu";
    String JAVA_VARIANT_NONE = "";

    // ── PYTHON ──
    // No jdk/jre-style product split; CPython/PyPy/etc is an implementation
    // difference, so — per your correction — it belongs under vendor, same
    // axis as GraalVM. product() is left empty for this family.
    String PYTHON_PRODUCT_NONE = "";
    String PYTHON_VENDOR_CPYTHON = "cpython";
    String PYTHON_VENDOR_PYPY = "pypy";
    String PYTHON_VENDOR_GRAALPY = "graalpy";
    String PYTHON_VENDOR_JYTHON = "jython";
    String PYTHON_VENDOR_IRONPYTHON = "ironpython";
    String PYTHON_VENDOR_ANACONDA = "anaconda"; // redistributed build, not a new interpreter — borderline, see note below
    String PYTHON_VARIANT_NONE = "";
    String PYTHON_VARIANT_EMBEDDABLE = "embeddable"; // Windows embeddable dist
    String PYTHON_VARIANT_FRAMEWORK = "framework";   // macOS framework build

    // ── JAVASCRIPT ──
    // Here the analogy actually maps well: product = which runtime program
    // you're running (the "jdk/jre" equivalent — a shipped, user-facing
    // product), vendor = which engine implementation powers it underneath.
    String JAVASCRIPT_PRODUCT_NODE = "node";
    String JAVASCRIPT_PRODUCT_DENO = "deno";
    String JAVASCRIPT_PRODUCT_BUN = "bun";
    String JAVASCRIPT_PRODUCT_BROWSER = "browser";
    String JAVASCRIPT_VENDOR_V8 = "v8";                  // Node, Deno, Chrome
    String JAVASCRIPT_VENDOR_JAVASCRIPTCORE = "javascriptcore"; // Bun, Safari
    String JAVASCRIPT_VENDOR_SPIDERMONKEY = "spidermonkey";     // Firefox
    String JAVASCRIPT_VENDOR_QUICKJS = "quickjs";
    String JAVASCRIPT_VENDOR_HERMES = "hermes";          // React Native
    String JAVASCRIPT_VARIANT_NONE = "";

    // ── DOTNET ──
    // product = the runtime family (these are genuinely distinct products,
    // same way jdk/jre are), vendor = who implements it.
    String DOTNET_PRODUCT_FRAMEWORK = "netframework";
    String DOTNET_PRODUCT_CORE = "netcore";
    String DOTNET_PRODUCT_UNIFIED = "net"; // .NET 5+ ("Core" and "Framework" merged branding
    String DOTNET_VENDOR_MICROSOFT = "microsoft";
    String DOTNET_VENDOR_MONO = "mono";
    String DOTNET_VARIANT_NONE = "";
    String DOTNET_VARIANT_AOT = "aot"; // ahead-of-time compiled variant

    static NRuntimeDistribution of(NId id, String vendor, String product, String variant, String name, String path, String version, String packaging, int priority) {
        return NUtilsRPI.of().createRuntimeDistribution(id, vendor, product, variant, name, path, version, packaging, priority);
    }

    static NRuntimeDistribution of(NId id, String vendor, String product, String variant, String name, String path, String version, String packaging) {
        return NUtilsRPI.of().createRuntimeDistribution(id, vendor, product, variant, name, path, version, packaging, 0);
    }


    /**
     * java, python, etc...
     */
    @NGetter
    NRuntimeDistributionFamily family();

    /**
     * jdk, jre
     */
    @NGetter
    String product();

    /**
     * temurin, etc...
     */
    @NGetter
    String vendor();


    /**
     * Good for flags like headless, musl, javafx, debug.
     */
    @NGetter
    String variant();


    @NGetter
    int priority();

    @NSetter
    NRuntimeDistribution priority(int priority);

    @NGetter
    NId id();

    @NGetter
    String version();

    @NGetter
    String name();

    @NGetter
    String path();

    /**
     * Represents distribution format (zip, tar.gz, deb, native).
     */
    @NGetter
    String packaging();

    NRuntimeDistribution copy();
}
