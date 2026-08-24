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

    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NDefinitionFactory of() {
        return NExtensions.of(NDefinitionFactory.class);
    }

    /**
     * By id.
     *
     * @param id id
     * @return by id result
     */
    NDefinition byId(NId id);

    /**
     * By descriptor.
     *
     * @param descriptor descriptor
     * @return by descriptor result
     */
    NDefinition byDescriptor(Supplier<NDescriptor> descriptor);

    /**
     * By id.
     *
     * @param id id
     * @param repository repository
     * @return by id result
     */
    NDefinition byId(NId id, NRepository repository);

    /**
     * By id and descriptor.
     *
     * @param id id
     * @param descriptor descriptor
     * @return by id and descriptor result
     */
    NDefinition byIdAndDescriptor(NId id, Supplier<NDescriptor> descriptor);

}
