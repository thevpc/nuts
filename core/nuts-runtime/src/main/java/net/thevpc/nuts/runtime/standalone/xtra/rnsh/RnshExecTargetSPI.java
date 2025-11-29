package net.thevpc.nuts.runtime.standalone.xtra.rnsh;

import net.thevpc.nuts.command.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.spi.NExecTargetInfoContext;
import net.thevpc.nuts.spi.NExecTargetInfoRunner;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.*;

public class RnshExecTargetSPI implements NExecTargetSPI {
    private Map<NConnectionString, RnshHttpClient> clients = new HashMap<>();

    @Override
    public NExecTargetInfo getTargetInfo(NExecTargetInfoContext context) {
        return context.createDefaultTargetInfo(new NExecTargetInfoRunner() {
            @Override
            public String run(String command, NConnectionString connectionString) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ByteArrayOutputStream err = new ByteArrayOutputStream();
                int x = exec(connectionString, new String[]{command}, true, NullInputStream.INSTANCE, out,err);
                return out.toString();
            }
        });
    }

    @Override
    public int exec(NExecTargetCommandContext context) {
        return exec(context.getConnectionString(), context.getCommand(), context.isRawCommand(), context.in(), context.out(), context.err());
    }

    public int exec(NConnectionString connectionString, String[] command, boolean rawCommand, InputStream in, OutputStream out0, OutputStream err) {
        if (command.length == 0) {
            throw new NIllegalArgumentException(NMsg.ofC("missing arguments"));
        }
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
        RnshHttpClient.ExecResult is = resolveRnshHttpClient(connectionString)
                .ensureConnected()
                .exec(command, NInputSource.of(new ByteArrayInputStream(out.toByteArray()),
                                new DefaultNContentMetadata().setName("in").setContentType("octet-stream")
                        )
                );

        try (InputStream ins = is.getOut().getInputStream()) {
            NIOUtils.copy(ins, out);
        } catch (IOException e) {
            throw new NIOException(e);
        }
        try (InputStream ins = is.getErr().getInputStream()) {
            NIOUtils.copy(ins, err);
        } catch (IOException e) {
            throw new NIOException(e);
        }
        return is.getCode();
    }

    private RnshHttpClient resolveRnshHttpClient(NConnectionString cnx) {
        NConnectionStringBuilder cb = cnx.builder();
        String v = NStringUtils.trimToNull(cb.getPath());
        Map<String, List<String>> qm = cb.getQueryMap().orElse(new HashMap<>());
        String context = NOptional.ofFirst(qm.get("context")).orElse(null);
        if (NBlankable.isBlank(context)) {
            Map<String, List<String>> qm2 = new HashMap<>(qm);
            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(v, "/"))));
            cb.setQueryMap(qm2);
            cb.setPath("/");
        } else {
            Map<String, List<String>> qm2 = new HashMap<>(qm);
            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(context, v))));
            cb.setQueryMap(qm2);
            cb.setPath("/");
        }
        NConnectionString c00 = cb.build();
        RnshHttpClient client = clients.get(c00);
        if (client == null) {
            client = new RnshHttpClient().setConnectionString(c00);
            clients.put(c00, client);
        }
        return client;
    }

    @Override
    public int getScore(NScorableContext context) {
        Object c = context.getCriteria();

        if (c instanceof String) {
            NConnectionStringBuilder z = DefaultNConnectionStringBuilder.of((String) c).orNull();
            if (z != null && isSupportedProtocol(z.getProtocol())) {
                return DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionStringBuilder) {
            NConnectionStringBuilder z = (NConnectionStringBuilder) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return DEFAULT_SCORE;
            }
        }
        if (c instanceof NConnectionString) {
            NConnectionString z = (NConnectionString) c;
            if (isSupportedProtocol(z.getProtocol())) {
                return DEFAULT_SCORE;
            }
        }
        return UNSUPPORTED_SCORE;
    }

    private boolean isSupportedProtocol(String protocol) {
        return ("rnsh".equals(protocol)
                || "rnsh-http".equals(protocol)
                || "rnsh-https".equals(protocol)
                || "rnshs".equals(protocol)
        );
    }
}
