package net.thevpc.nuts.runtime.standalone.xtra.rnsh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class RnshExecCmdExtension implements NExecCmdExtension {
    private Map<NConnexionString, RnshHttpClient> clients = new HashMap<>();

    @Override
    public int exec(NExecCmdExtensionContext context) {
        String[] command = context.getCommand();
        if (command.length == 0) {
            throw new NIllegalArgumentException(NMsg.ofC("missing arguments"));
        }
        InputStream in = NSession.of().in();
        //load available input
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            while (true) {
                int av = in.available();
                if (av > 0) {
                    byte[] b = new byte[av];
                    int a = in.read(b);
                    out.write(b, 0, a);
                } else {
                    break;
                }
            }
        } catch (IOException e) {
            throw new NIOException(e);
        }
        RnshHttpClient.ExecResult is = resolveRnshHttpClient(context.getTarget())
                .ensureConnected()
                .exec(command, NInputSource.of(new ByteArrayInputStream(out.toByteArray()),
                        new DefaultNContentMetadata().setName("in").setContentType("octet-stream")
                )
                );

        try (InputStream ins = is.getOut().getInputStream()) {
            NIOUtils.copy(ins, NSession.of().out().asPrintStream());
        } catch (IOException e) {
            throw new NIOException(e);
        }
        try (InputStream ins = is.getErr().getInputStream()) {
            NIOUtils.copy(ins, NSession.of().err().asPrintStream());
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return is.getCode();
    }

    private RnshHttpClient resolveRnshHttpClient(String cnx) {
        NConnexionString cc = NConnexionString.of(cnx).get();
        NConnexionString c0 = cc.copy();
        String v = NStringUtils.trimToNull(cc.getPath());
        Map<String, List<String>> qm = c0.getQueryMap().orElse(new HashMap<>());
        String context = NOptional.ofFirst(qm.get("context")).orElse(null);
        if (NBlankable.isBlank(context)) {
            Map<String, List<String>> qm2 = new HashMap<>(qm);
            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(v, "/"))));
            c0.setQueryMap(qm2);
            c0.setPath("/");
        } else {
            Map<String, List<String>> qm2 = new HashMap<>(qm);
            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(context, v))));
            c0.setQueryMap(qm2);
            c0.setPath("/");
        }
        RnshHttpClient client = clients.get(c0);
        if (client == null) {
            client = new RnshHttpClient().setConnexionString(c0);
            clients.put(c0, client);
        }
        return client;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        Object c = context.getConstraints();
        if (c instanceof String) {
            NConnexionString z = NConnexionString.of((String) c).orNull();
            if (z != null && isSupportedProtocol(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        if (c instanceof NConnexionString) {
            NConnexionString z = (NConnexionString) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return NConstants.Support.DEFAULT_SUPPORT;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    private boolean isSupportedProtocol(String protocol) {
        return ("rnsh".equals(protocol)
                || "rnsh-http".equals(protocol)
                || "rnsh-https".equals(protocol)
                || "rnshs".equals(protocol)
        );
    }
}
