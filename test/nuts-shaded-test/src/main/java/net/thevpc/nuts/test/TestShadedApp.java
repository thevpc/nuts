package net.thevpc.nuts.test;

import com.google.gson.Gson;
import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.NApplicationInfo;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;

import java.util.LinkedHashMap;


/**
 * this is precious when using obfuscators!
 */
public class TestShadedApp implements NApplication {
    public static void main(String[] args) {
        new TestShadedApp().runAndExit(args);
    }

    @Override
    public void run(NSession session) {
        NMemoryPrintStream t = NPrintStream.ofInMemory(NTerminalMode.FORMATTED, session);
//        session.out().println("##{pale:2024-03-23 21:42:32.980}##\u001E ##{pale:FINEST}##\u001E ##{option:READ   }##\u001E : resolve NConfigs                                 to  ```underlined ##{option:session}##\u001E``` net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs");
    }
}
