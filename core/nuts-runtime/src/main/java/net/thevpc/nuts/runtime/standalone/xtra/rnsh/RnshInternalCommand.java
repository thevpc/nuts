//package net.thevpc.nuts.runtime.standalone.xtra.rnsh;
//
//import net.thevpc.nuts.*;
//import net.thevpc.nuts.io.NIOException;
//import net.thevpc.nuts.io.NIOUtils;
//import net.thevpc.nuts.io.NInputSource;
//import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.NInternalCommand;
//import net.thevpc.nuts.spi.NSupportLevelContext;
//import net.thevpc.nuts.util.*;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.*;
//
//public class RnshInternalCommand implements NInternalCommand {
//    //TODO : should it be by session?
//    private Map<NConnexionString, RnshHttpClient> clients = new HashMap<>();
//
//    @Override
//    public String getName() {
//        return "rnsh";
//    }
//
//    private RnshHttpClient resolveRnshHttpClient(String cnx) {
//        NConnexionString cc = NConnexionString.of(cnx).get();
//        NConnexionString c0 = cc.copy();
//        String v = NStringUtils.trimToNull(cc.getPath());
//        Map<String, List<String>> qm = c0.getQueryMap().orElse(new HashMap<>());
//        String context = NOptional.ofFirst(qm.get("context")).orElse(null);
//        if (NBlankable.isBlank(context)) {
//            Map<String, List<String>> qm2 = new HashMap<>(qm);
//            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(v, "/"))));
//            c0.setQueryMap(qm2);
//            c0.setPath("/");
//        } else {
//            Map<String, List<String>> qm2 = new HashMap<>(qm);
//            qm2.put("context", new ArrayList<>(Arrays.asList(NStringUtils.firstNonBlank(context, v))));
//            c0.setQueryMap(qm2);
//            c0.setPath("/");
//        }
//        RnshHttpClient client = clients.get(c0);
//        if (client == null) {
//            client = new RnshHttpClient().setConnexionString(c0);
//            clients.put(c0, client);
//        }
//        return client;
//    }
//
//    @Override
//    public int execute(String[] args, NExecCmd execCommand) {
//        if (args.length < 2) {
//            throw new NIllegalArgumentException(NMsg.ofC("missing arguments %s", args));
//        }
//        RnshHttpClient.ExecResult is = resolveRnshHttpClient(args[0])
//                .exec(
//                        Arrays.copyOfRange(args, 1, args.length)
//                        , NInputSource.of(NSession.of().in())
//                );
//
//        try (InputStream ins = is.getOut().getInputStream()) {
//            NIOUtils.copy(ins, NSession.of().out().asPrintStream());
//        } catch (IOException e) {
//            throw new NIOException(e);
//        }
//        try (InputStream ins = is.getErr().getInputStream()) {
//            NIOUtils.copy(ins, NSession.of().err().asPrintStream());
//        } catch (IOException e) {
//            throw new NIOException(e);
//        }
//        return is.getCode();
//    }
//
//    @Override
//    public int getSupportLevel(NSupportLevelContext context) {
//        return NConstants.Support.DEFAULT_SUPPORT;
//    }
//}
