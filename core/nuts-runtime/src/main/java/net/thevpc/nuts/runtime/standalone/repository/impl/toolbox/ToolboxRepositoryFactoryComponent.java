/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox;

import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;

import java.util.Collections;
import java.util.List;

/**
 * Created by vpc on 1/15/17.
 */
@NComponentScope(NScopeType.WORKSPACE)
public class ToolboxRepositoryFactoryComponent implements
        NRepositorySpecDefaultResolverComponent,
        NRepositorySpecTemplateResolverComponent,
        NRepositoryFactoryComponent {


    public ToolboxRepositoryFactoryComponent() {

    }

    @Override
    public List<NRepositorySpec> getTemplateRepositoryDefinitions() {
        return Collections.singletonList(
                new NRepositorySpec().setName(ToolboxRepositoryModel.REPOSITORY_TYPE)
                        .setFailSafe(false)
                        .setOrder(NRepositorySpec.ORDER_USER_REMOTE)
                        .setSourceLocation(NRepositoryLocation.of("toolbox@toolbox"))
        );
    }

    @Override
    public List<NRepositorySpec> getDefaultRepositoryDefinitions() {
        return Collections.singletonList(
                new NRepositorySpec().setName(ToolboxRepositoryModel.REPOSITORY_TYPE)
        );
    }

    @Override
    public NRepository createRepository(NRepositoryFactoryContext context) {
        if (!ToolboxRepositoryModel.REPOSITORY_TYPE.equals(context.repositoryType())) {
            return null;
        }
        return context.createDefaultRepository(new ToolboxRepositoryModel());
    }

    @NScore
    public static int getScore(NScorableContext criteria) {
        if (criteria != null) {
            NRepositoryFactoryContext context = criteria.getCriteria(NRepositoryFactoryContext.class);
            if (context != null) {
                if (ToolboxRepositoryModel.REPOSITORY_TYPE.equals(context.repositoryType())) {
                    return NScorable.DEFAULT_SCORE + 10;
                }
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }
}
