package net.thevpc.nuts.runtime.standalone.xtra.nanodb;

import net.thevpc.nuts.io.NDataSerializer;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NanoDBNDataSerializer<T> implements NDataSerializer<T> {
    private final NanoDBSerializer<T> delegate;
    private final Class<T> type;

    public NanoDBNDataSerializer(Class<T> type, NanoDBSerializer<T> delegate) {
        this.type = type;
        this.delegate = delegate;
    }

    @Override
    public void serialize(T obj, DataOutputStream dos) throws IOException {
        delegate.write(obj, new NanoDBDefaultOutputStream(dos));
    }

    @Override
    public T deserialize(DataInputStream dis) throws IOException {
        return delegate.read(new NanoDBDefaultInputStream(dis), type);
    }
}
