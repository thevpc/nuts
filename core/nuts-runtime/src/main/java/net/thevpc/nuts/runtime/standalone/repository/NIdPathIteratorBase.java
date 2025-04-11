package net.thevpc.nuts.runtime.standalone.repository;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NInputStreamMonitor;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.util.logging.Level;

public abstract class NIdPathIteratorBase implements NIdPathIteratorModel {

    public NIdPathIteratorBase() {
    }

    public abstract NWorkspace getWorkspace();
    public NId validate(NId id, NDescriptor descriptor, NPath pathname, NPath rootPath, NDefinitionFilter filter, NRepository repository)  {
        if (descriptor != null) {
            if (!CoreNUtils.isEffectiveId(descriptor.getId())) {
                NDescriptor effectiveDescriptor = null;
                try {
                    effectiveDescriptor = NWorkspace.of().resolveEffectiveDescriptor(descriptor);
                } catch (Exception ex) {
                    NLogOp.of(NIdPathIteratorBase.class).level(Level.FINE).error(ex).log(
                            NMsg.ofC("error resolving effective descriptor for %s in url %s : %s", descriptor.getId(),
                                    pathname,
                                    ex));//e.printStackTrace();
                }
                if(effectiveDescriptor!=null){
                    descriptor = effectiveDescriptor;
                }
            }
            if ((filter == null || filter.acceptDefinition(NDefinitionHelper.ofDescriptorOnly(descriptor)))) {
                NId nutsId = descriptor.getId().builder().setRepository(repository.getName()).build();
//                        nutsId = nutsId.setAlternative(t.getAlternative());
                return nutsId;
            }
        }
        if (id != null) {
            if ((filter == null || filter.acceptDefinition(NDefinitionHelper.ofIdOnly(id)))) {
                return id;
            }
        }
        return null;
    }

    @Override
    public NId parseId(NPath pathname, NPath rootPath, NDefinitionFilter filter, NRepository repository)  {
        NDescriptor t = null;
        try {
            t = parseDescriptor(pathname, NInputStreamMonitor.of().setSource(pathname).create(),
                    NFetchMode.LOCAL, repository, rootPath);
        } catch (Exception ex) {
            NLogOp.of(NIdPathIteratorBase.class).level(Level.FINE).error(ex)
                    .log(NMsg.ofJ("error parsing url : {0} : {1}", pathname, toString()));//e.printStackTrace();
        }
        if (t != null) {
            return validate(null, t, pathname, rootPath, filter, repository);
        }
        return null;
    }

}
