package net.thevpc.nuts.runtime.standalone.repository;


import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.util.NMapWithAlias;
import net.thevpc.nuts.text.NMsg;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class DefaultNRepositoryDB  {
    private volatile AtomicBoolean valid=new AtomicBoolean(false);
    private final NMapWithAlias<String, NRepositorySpec> optionByName = new NMapWithAlias<>();
    private final NMapWithAlias<String, NRepositorySpec> optionByLocation = new NMapWithAlias<>();

    public DefaultNRepositoryDB() {

    }

    public void invalidate(){
        valid.set(false);
    }

    public void validate(){
        if(valid.compareAndSet(false,true)){
            synchronized (this) {
                optionByName.clear();
                optionByLocation.clear();
                for (NRepositorySpec templateRepositoryDefinition : NWorkspaceExt.of().getConfigModel().getTemplateRepositoryDefinitions()) {
                    reg(templateRepositoryDefinition);
                }
            }
        }
    }

    public Set<String> findAllNamesByName(String name) {
        return optionByName.keySetWithAlias(name);
    }

    public NOptional<NRepositorySpec> getDefinitionByName(String name) {
        validate();
        return NOptional.of(optionByName.get(name), () -> NMsg.ofC("repository %s", name)).map(NRepositorySpec::copy);
    }

    public NOptional<NRepositorySpec> getDefinitionByPath(String name) {
        validate();
        return NOptional.of(optionByLocation.get(name), () -> NMsg.ofC("repository %s", name)).map(NRepositorySpec::copy);
    }

    private void reg(NRepositorySpec options) {
        options.setCreate(true);
        String name = options.getName();
        String location = options.getSourceLocation().toString();
        optionByName.put(name, options);
        String[] aliases = options.getAliases();
        if (aliases != null) {
            for (String alias : aliases) {
                alias=NStringUtils.trimToNull(alias);
                if (alias != null) {
                    optionByName.alias(alias, name);
                }
            }
        }
        optionByLocation.put(location, options);
    }


}
