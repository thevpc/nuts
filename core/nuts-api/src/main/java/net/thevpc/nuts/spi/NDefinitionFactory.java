/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.spi;

import java.util.function.Supplier;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDescriptor;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.ext.NExtensions;

/**
 *
 * @author vpc
 */
public interface NDefinitionFactory extends NComponent {

    static NDefinitionFactory of() {
        return NExtensions.of(NDefinitionFactory.class);
    }

    NDefinition byId(NId id);

    NDefinition byDescriptor(Supplier<NDescriptor> descriptor);

    NDefinition byId(NId id, NRepository repository);

    NDefinition byIdAndDescriptor(NId id, Supplier<NDescriptor> descriptor);

}
