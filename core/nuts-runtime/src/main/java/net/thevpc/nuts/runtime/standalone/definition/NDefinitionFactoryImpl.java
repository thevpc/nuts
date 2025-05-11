/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting
 * a large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the GNU LESSER GENERAL PUBLIC
 * LICENSE Version 3 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * https://www.gnu.org/licenses/lgpl-3.0.en.html Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.definition;

import java.util.function.Supplier;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NRepository;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NDefinitionFactory;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NSupportLevelContext;

/**
 *
 * @author vpc
 */
@NComponentScope(NScopeType.WORKSPACE)
public class NDefinitionFactoryImpl implements NDefinitionFactory {

    @Override
    public NDefinition byId(NId id) {
        return NDefinitionHelper.ofDefinition(id);
    }

    @Override
    public NDefinition byId(NId id, NRepository repository) {
        return NDefinitionHelper.ofIdOnlyFromRepo(id, repository, "byIdAndRepo");
    }

    @Override
    public NDefinition byDescriptor(Supplier<NDescriptor> descriptor) {
        return NDefinitionHelper.ofDescriptorOnly(descriptor.get());
    }

    @Override
    public NDefinition byIdAndDescriptor(NId id, Supplier<NDescriptor> descriptor) {
        return NDefinitionHelper.ofDescriptorOnly(id, descriptor.get());
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
