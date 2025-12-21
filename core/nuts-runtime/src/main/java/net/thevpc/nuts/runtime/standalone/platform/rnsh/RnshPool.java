package net.thevpc.nuts.runtime.standalone.platform.rnsh;

import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

public class RnshPool {
    public static RnshPool of() {
        return NWorkspace.of().getOrComputeProperty(RnshPool.class, RnshPool::new);
    }

    private Map<NConnectionString, RnshHttpClient> clients = new HashMap<>();

    public RnshHttpClient get(NConnectionString cnx) {
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
}
