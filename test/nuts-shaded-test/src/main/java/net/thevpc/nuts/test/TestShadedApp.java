package net.thevpc.nuts.test;

import net.thevpc.nuts.NApplication;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;


/**
 * this is precious when using obfuscators!
 */
public class TestShadedApp implements NApplication {
    public static void main(String[] args) {
        new TestShadedApp().run(NAppRunOptions.ofExit(args));
    }

    @Override
    public void run() {
        NMemoryPrintStream t = NPrintStream.ofMem(NTerminalMode.FORMATTED);
//        session.out().println("##{pale:2024-03-23 21:42:32.980}##\u001E ##{pale:FINEST}##\u001E ##{option:READ   }##\u001E : resolve NConfigs                                 to  ```underlined ##{option:session}##\u001E``` net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs");
    }
}
