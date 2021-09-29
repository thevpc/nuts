package net.thevpc.nuts.runtime.standalone.index;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.nanodb.*;

import java.util.stream.Stream;

public class ArtifactsIndexDB {
    public static final String DEFAULT_ARTIFACT_TABLE_NAME = "Artifacts";
//    private String tableName;
//    private NanoDB db;
//    private NutsWorkspace ws;
    private NanoDBTableFile<NutsId> table;



    public static ArtifactsIndexDB of(NutsSession ws) {
        synchronized (ws.getWorkspace()) {
            ArtifactsIndexDB o = (ArtifactsIndexDB) ws.env().getProperties().get(ArtifactsIndexDB.class.getName());
            if (o == null) {
                o = new ArtifactsIndexDB(DEFAULT_ARTIFACT_TABLE_NAME, CacheDB.of(ws));
                ws.env().getProperties().put(ArtifactsIndexDB.class.getName(), o);
            }
            return o;
        }
    }


    public ArtifactsIndexDB(String tableName, NanoDB db) {
//        this.tableName = tableName;
//        this.db = db;
//        this.ws = ws;
        table = db.createTable(def(tableName, db), true);
    }

    public Stream<NutsId> findAll() {
        return table.stream();
    }

    public Stream<NutsId> findByGroupId(String groupId) {
        return table.findByIndex("groupId",groupId);
    }

    public Stream<NutsId> findByArtifactId(String artifactId) {
        return table.findByIndex("artifactId",artifactId);
    }

    private static NanoDBTableDefinition<NutsId> def(String name, NanoDB db) {
        return new NanoDBTableDefinition<NutsId>(
                name, NutsId.class, db.getSerializers().of(NutsId.class,false),
                new NanoDBDefaultIndexDefinition<>("id", String.class,false, x->x.getLongNameId()
                        .builder().setRepository(x.getRepository()).build().toString()
                        ),
                new NanoDBDefaultIndexDefinition<>("groupId", String.class,false, NutsId::getGroupId),
                new NanoDBDefaultIndexDefinition<>("artifactId", String.class,false,NutsId::getArtifactId),
                new NanoDBDefaultIndexDefinition<>("repository", String.class,false,NutsId::getRepository)
        );
    }

    public void add(NutsId id) {
        table.add(id);
    }

    public void flush() {
        table.flush();
    }


    public boolean contains(NutsId id) {
        return table.findByIndex("id",
                id.getLongNameId()
                .builder().setRepository(id.getRepository())
                .build().toDependency()
        ).findAny().orElse(null)!=null;
    }
}
