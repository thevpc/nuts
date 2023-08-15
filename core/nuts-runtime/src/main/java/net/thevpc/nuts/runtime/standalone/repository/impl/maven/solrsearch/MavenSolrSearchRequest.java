package net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch;

import net.thevpc.nuts.util.NBlankable;

import java.util.HashMap;
import java.util.Map;

public class MavenSolrSearchRequest {
    String g;
    String a;
    int rows = 5000;

    public MavenSolrSearchRequest(String g, String a) {
        this.g = g;
        this.a = a;
    }

    public Map<String, String> toQueryMap() {
        Map<String, String> m = new HashMap<>();
        StringBuilder q = new StringBuilder();
        if (!NBlankable.isBlank(g) && !g.equals("*")) {
            q.append("g:\"").append(g).append("\"");
        }
        if (!NBlankable.isBlank(a) && !a.equals("*")) {
            if (q.length() > 0) {
                q.append(" AND ");
            }
            q.append("a:\"").append(a).append("\"");
        }
        if (q.length() > 0) {
            m.put("q", q.toString());
            m.put("rows", String.valueOf(rows));
            m.put("core", "gav");
            m.put("wt","json");
            return m;
        } else {
            return null;
        }
    }
}
