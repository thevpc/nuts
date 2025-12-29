package net.thevpc.nuts.runtime.standalone.repository.toolbox.helpers;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.runtime.standalone.util.NCoreLogUtils;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.spi.NDefinitionFactory;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PostgresRepoHelper implements ToolboxRepoHelper {
    public static final String POSTGRESQL_SOURCE_BASE_URL = "https://ftp.postgresql.org/pub/source/v";

    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper("org.postgresql:pgsql");

    @Override
    public NIterator<NId> searchVersions(NId id, NDefinitionFilter filter, NRepository repository) {
        return search(id,filter, new NPath[]{null}, repository);
    }

    @Override
    public boolean acceptId(NId id) {
        return baseIdFilterHelper.accept(id);
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NRepository repository) {
//        if (fetchMode != NFetchMode.REMOTE) {
//            return null;
//        }
        if (!baseIdFilterHelper.accept(id)) {
            String r = getUrl(id.getVersion());
            boolean found = false;
            URL url = null;
            try {
                url = new URL(r);
            } catch (MalformedURLException e) {

            }
            if (url != null) {
                NSession session = NSession.of();
                session.getTerminal().printProgress(NMsg.ofC("peek %s", url));
                try (InputStream inputStream = url.openStream()) {
                    found = true;
                } catch (Exception ex) {
                    found = false;
                }
            }
            if (found) {
                return NDescriptorBuilder.of()
                        .setId(id.getLongId())
                        .setPackaging("tar.gz")
                        .setDescription("Postgresql Official Tar Gz Source")
                        .setProperty("dynamic-descriptor", "true")
                        .build();
            }
        }
        return null;
    }


    @Override
    public NIterator<NId> search(NId id,NDefinitionFilter filter, NPath[] basePaths, NRepository repository) {
//        if (fetchMode != NFetchMode.REMOTE) {
//            return NIterator.ofEmpty();
//        }
        //List<NutsId> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
        if (!baseIdFilterHelper.accept(id,basePaths)) {
            return null;
        }
        NIdBuilder idBuilder = NIdBuilder.of("org.postgresql", "pgsql");
        List<String> versions = NPath.of("https://www.postgresql.org/ftp/source")
                .lines()
                .map(x -> {
                    Pattern p = Pattern.compile(".*(?<version>(v([0-9][1-9]?\\.){1,2}([0-9][1-9]?)?)).*");
                    Matcher m = p.matcher(x);
                    if (m.find()) {
                        return m.group("version");
                    } else {
                        return null;
                    }
                }).filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<NId> ids = new ArrayList<>();
        for (String s : versions) {
            NId id0 = idBuilder.setVersion(NVersion.of(s)).build();
            final NDefinition dd = NDefinitionFactory.of().byId(id0,repository);
            if (filter == null || filter.acceptDefinition(dd)) {
                ids.add(id0);
            }
        }
        return NIterator.of(ids.iterator()).redescribe(NElementDescribables.ofDesc("NIterator of iterator"));
        /*return NPath.of("https://www.postgresql.org/ftp/source", session)
                .stream()
                .filter(x -> *//*x.isDirectory() &&*//* x.getName().matches("v([0-9][1-9]?\\.){1,2}([0-9][1-9]?)?"), "directory && postgresql")
                .flatMapStream(
                    NFunction.of(
                        s -> {
                        String s2 = s.getName();
                        String prefix = "postgresql-";
                        NVersion version = NVersion.of(s2.substring(1)).get(session);
                            //NVersion version = NVersion.of(s).get();
                            NId id2 = idBuilder.setVersion(version).build();
                            final NDefinition dd = NDefinitionFactory.of().byId(id2,repository);
                            if (filter == null || filter.acceptDefinition(dd)) {
                                return NStream.ofSingleton(id2, session);
                            }
                            System.out.println(version);
                            return NStream.ofEmpty(());
                        }
                        , "flatMap"))
                .iterator();*/
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String r = getUrl(id.getVersion());
        //localPath = getIdLocalFile(id.builder().setFaceContent().build(), fetchMode, repository, session);
        NPath localPath = NApp.of().getCacheFolder().resolve("postgresql-" + id.getVersion().getValue() + ".tar.gz");
        NCp.of().from(NPath.of(r)).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
        return localPath;
    }

    private String getUrl(NVersion version) {
        return POSTGRESQL_SOURCE_BASE_URL + version.get(0).flatMap(NLiteral::asString) + "/postgresql-" + version.get(0).flatMap(NLiteral::asString) + ".tar.gz";
    }


}
