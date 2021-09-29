package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsEnum;
import net.thevpc.nuts.NutsHomeLocation;
import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsStoreLocation;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class Test03_NutsHomeLocation {
    @Test
    public void test1(){
        Assertions.assertEquals(
                NutsHomeLocation.of(null, NutsStoreLocation.APPS),
                NutsHomeLocation.parse("system-apps", null)
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsHomeLocation.parse("", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsHomeLocation.parseLenient("", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertNull(NutsHomeLocation.parseLenient("any error", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE), null));
        Assertions.assertEquals(
                NutsHomeLocation.of(null, NutsStoreLocation.APPS),
                NutsEnum.parse(NutsHomeLocation.class, "system-apps", null)
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsEnum.parse(NutsHomeLocation.class,"", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertEquals(
                NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),
                NutsEnum.parseLenient(NutsHomeLocation.class,"", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE),null )
        );
        Assertions.assertNull(NutsEnum.parseLenient(NutsHomeLocation.class,"any error", NutsHomeLocation.of(NutsOsFamily.MACOS, NutsStoreLocation.CACHE), null));
    }
}
