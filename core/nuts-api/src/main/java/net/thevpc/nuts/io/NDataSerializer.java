package net.thevpc.nuts.io;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Interface for serializing and deserializing objects of type {@code T} to/from binary streams.
 *
 * @param <T> the type of objects managed by this serializer
 * @since 0.8.4
 */
public interface NDataSerializer<T> {

    /**
     * Serializes the given object into the binary stream.
     *
     * @param obj the object to serialize
     * @param dos the destination binary output stream
     * @throws IOException if an I/O error occurs
     */
    void serialize(T obj, DataOutputStream dos) throws IOException;

    /**
     * Deserializes an object from the binary stream.
     *
     * @param dis the source binary input stream
     * @return the deserialized object
     * @throws IOException if an I/O error occurs
     */
    T deserialize(DataInputStream dis) throws IOException;
}
