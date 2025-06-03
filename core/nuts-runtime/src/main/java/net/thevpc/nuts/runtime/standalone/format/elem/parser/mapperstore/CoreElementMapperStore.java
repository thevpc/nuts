package net.thevpc.nuts.runtime.standalone.format.elem.parser.mapperstore;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.format.elem.mapper.NElementMapperNLiteral;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NEnum;
import net.thevpc.nuts.util.NLiteral;

import java.util.HashMap;
import java.util.Map;

public class CoreElementMapperStore implements NElementMapperStore {
    private final Map<Class, NElementMapper> coreMappers = new HashMap<>();
    public CoreElementMapperStore() {
//        addHierarchyFactory(JsonElement.class, F_JSONELEMENT);
        setCoreMapper(NDefinition.class, DefaultElementMapperStore.F_NUTS_DEF);
        setCoreMapper(NId.class, DefaultElementMapperStore.F_NUTS_ID);
        setCoreMapper(NVersion.class, DefaultElementMapperStore.F_NUTS_VERSION);
        setCoreMapper(NDescriptor.class, DefaultElementMapperStore.F_NUTS_DESCRIPTOR);
        setCoreMapper(NDependency.class, DefaultElementMapperStore.F_NUTS_DEPENDENCY);
        setCoreMapper(NIdLocation.class, DefaultElementMapperStore.F_NUTS_ID_LOCATION);
        setCoreMapper(NArtifactCall.class, DefaultElementMapperStore.F_ARTIFACT_CALL);
        setCoreMapper(NPlatformLocation.class, DefaultElementMapperStore.F_NUTS_SDK_LOCATION);
        setCoreMapper(NEnvCondition.class, DefaultElementMapperStore.F_NUTS_ENV_CONDITION);
        setCoreMapper(NEnvConditionBuilder.class, DefaultElementMapperStore.F_NUTS_ENV_CONDITION_BUILDER);
        setCoreMapper(NDescriptorProperty.class, DefaultElementMapperStore.F_DESCRIPTOR_PROPERTY);
        setCoreMapper(NDescriptorPropertyBuilder.class, DefaultElementMapperStore.F_DESCRIPTOR_PROPERTY_BUILDER);
        setCoreMapper(NDescriptorContributor.class, DefaultElementMapperStore.F_DESCRIPTOR_CONTRIBUTOR);
        setCoreMapper(NDescriptorContributorBuilder.class, DefaultElementMapperStore.F_DESCRIPTOR_CONTRIBUTOR);
        setCoreMapper(NDescriptorLicense.class, DefaultElementMapperStore.F_DESCRIPTOR_LICENSE);
        setCoreMapper(NDescriptorLicenseBuilder.class, DefaultElementMapperStore.F_DESCRIPTOR_LICENSE);
        setCoreMapper(NDescriptorOrganization.class, DefaultElementMapperStore.F_DESCRIPTOR_ORGANIZATION);
        setCoreMapper(NDescriptorOrganizationBuilder.class, DefaultElementMapperStore.F_DESCRIPTOR_ORGANIZATION);
        setCoreMapper(NEnum.class, DefaultElementMapperStore.F_NUTS_ENUM);
        setCoreMapper(NRepositoryLocation.class, DefaultElementMapperStore.F_NUTS_REPO_LOCATION);
        setCoreMapper(NLiteral.class, new NElementMapperNLiteral());
    }


    public final void setCoreMapper(Class cls, NElementMapper instance) {
        coreMappers.put(cls, instance);
    }

    public Map<Class, NElementMapper> getCoreMappers() {
        return coreMappers;
    }
}
