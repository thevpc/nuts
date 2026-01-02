package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.io.NContentMetadataProviderFormatSPI;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.io.path.*;
import net.thevpc.nuts.runtime.standalone.io.printstream.NByteArrayPrintStream;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.printstream.OutputTargetExt;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamExt;
import net.thevpc.nuts.runtime.standalone.io.util.InputStreamTee;
import net.thevpc.nuts.runtime.standalone.io.util.NInputStreamSource;
import net.thevpc.nuts.runtime.standalone.io.util.NNonBlockingInputStreamAdapter;
import net.thevpc.nuts.runtime.standalone.reflect.NUseDefaultUtils;
import net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest;
import net.thevpc.nuts.spi.NFormatSPI;
import net.thevpc.nuts.spi.NPathSPI;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.runtime.standalone.format.impl.NChronometerNFormatSPI;
import net.thevpc.nuts.time.NDuration;

import java.util.function.Function;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class NFormatsImpl implements NFormats {
    private NClassMap<NFormatMapper> mapper = new NClassMap<>(NFormatMapper.class);

    public NFormatsImpl() {
        registerDefaults();
    }

    private void registerConverter(Class clz, Function<Object, Object> mapper) {
        register(clz, new NFormatMapperBridge() {
            @Override
            Object convert(Object o) {
                return mapper.apply(o);
            }
        });
    }

    private void registerToFormatSPI(Class clz, Function<Object, NFormatSPI> mapper) {
        register(clz, new NFormatMapperFromSPI() {
            @Override
            NFormatSPI toSpi(Object o) {
                return mapper.apply(o);
            }
        });
    }

    private void register(Class clz, NFormatMapper mapper) {
        if (mapper == null) {
            this.mapper.remove(clz);
        } else {
            this.mapper.put(clz, mapper);
        }
    }

    @Override
    public NOptional<NFormat> ofFormat(Object t) {
        if (t == null) {
            return NOptional.ofNamedEmpty("null");
        }
        if (t instanceof NText) {
            return NOptional.of((NFormat) t);
        }
        Class<?> c = t.getClass();
        NFormatMapper e = mapper.get(c);
        if (e != null) {
            NFormat n = e.ofFormat(t, this);
            if (n != null) {
                return NOptional.of(n);
            }
        }
        return NOptional.ofNamedEmpty("format for " + t.getClass().getSimpleName());
    }

    private void registerDefaults() {
        register(NExec.class, (o, f) -> NExecFormat.of());

        register(NVersion.class, (o, f) -> NVersionFormat.of());

        register(NId.class, (o, f) -> NIdFormat.of());
        registerConverter(NIdBuilder.class, o -> ((NIdBuilder) o).build());

        register(NDescriptor.class, (o, f) -> NDescriptorFormat.of());
        registerConverter(NDescriptorBuilder.class, o -> ((NDescriptorBuilder) o).build());

        register(NDependency.class, (o, f) -> NDependencyFormat.of());
        registerConverter(NDependencyBuilder.class, o -> ((NDependencyBuilder) o).build());

        register(NCmdLine.class, (o, f) -> NCmdLineFormat.of());

        register(NCompressedPath.class, (o, f) -> new NCompressedPath.MyPathFormat());
        register(NCompressedPathBase.class, (o, f) -> new NCompressedPathBase.MyPathFormat());
        register(NPathBase.class, (o, f) -> new NPathBase.PathFormat());

        register(NFormatSPI.class, (o, f) -> NFormat.of(((NFormatSPI) o)));
        registerToFormatSPI(NChronometer.class, o -> new NChronometerNFormatSPI((NChronometer) o));
        registerToFormatSPI(NDuration.class, o -> new NDurationFormatSPI((NDuration) o));
        registerConverter(NByteArrayPrintStream.MyAbstractMultiReadNInputSource.class, o -> ((NByteArrayPrintStream.MyAbstractMultiReadNInputSource) o).getValue());
        registerToFormatSPI(InputStreamExt.class, o->new NContentMetadataProviderFormatSPI((InputStreamExt) o, ((InputStreamExt) o).getSourceName(), "input-stream"));
        registerToFormatSPI(InputStreamTee.class, o ->new NContentMetadataProviderFormatSPI((InputStreamTee) o, null, "input-stream-tee"));
        registerToFormatSPI(OutputStreamExt.class, o -> new NContentMetadataProviderFormatSPI((OutputStreamExt) o, null, "output-stream"));
        registerToFormatSPI(NNonBlockingInputStreamAdapter.class, o -> new NContentMetadataProviderFormatSPI((NNonBlockingInputStreamAdapter) o, ((NNonBlockingInputStreamAdapter) o).getSourceName(), "input-stream"));
        registerToFormatSPI(NInputStreamSource.class,o -> new NContentMetadataProviderFormatSPI((NInputStreamSource) o, null, "input-stream"));
        registerToFormatSPI(NPrintStream.class, o -> new NContentMetadataProviderFormatSPI(((NPrintStream) o), null, "print-stream"));
        registerToFormatSPI(OutputTargetExt.class,o ->new NContentMetadataProviderFormatSPI((OutputTargetExt) o, ((OutputTargetExt) o).getSourceName(), "output-stream"));
        registerToFormatSPI(OutputTargetExt.class, o-> new NContentMetadataProviderFormatSPI((OutputTargetExt) o, ((OutputTargetExt) o).getSourceName(), "output-stream"));

        registerToFormatSPI(DefaultNDigest.NDescriptorInputSource.class, o -> new NDescriptorInputSourceFormatSPI((DefaultNDigest.NDescriptorInputSource) o));

        register(NPathFromSPI.class, (o, f) -> new NFormatAdapter() {
                    @Override
                    public NFormatAndValue<Object, NFormat> getBase(Object aValue) {
                        NPathFromSPI b = (NPathFromSPI) o;
                        NPathSPI base = ((NPathFromSPI) o).getBase();
                        NFormatSPI fspi = null;
                        if (NUseDefaultUtils.isUseDefault(base.getClass(), "formatter", NPath.class)) {
                        } else {
                            fspi = base.formatter(b);
                        }
                        if (fspi != null) {
                            return new NFormatAndValue<>(fspi, new NFormatFromSPI(fspi));
                        }
                        return new NFormatAndValue<>((NPathBase) o, new NPathBase.PathFormat());
                    }
                }
        );
    }

    private interface NFormatMapper {
        NFormat ofFormat(Object t, NFormats texts);
    }

    private abstract class NFormatMapperFromSPI implements NFormatMapper {
        abstract NFormatSPI toSpi(Object o);

        @Override
        public NFormat ofFormat(Object t, NFormats texts) {
            return new NFormatAdapter() {
                @Override
                public NFormatAndValue<Object, NFormat> getBase(Object aValue) {
                    NFormatSPI spi = toSpi(aValue);
                    return new NFormatAndValue<>(spi, NFormat.of(spi));
                }
            };
        }
    }

    private abstract class NFormatMapperBridge implements NFormatMapper {
        abstract Object convert(Object o);

        @Override
        public NFormat ofFormat(Object t, NFormats texts) {
            return new NFormatAdapter() {
                @Override
                public NFormatAndValue<Object, NFormat> getBase(Object aValue) {
                    Object spi = convert(aValue);
                    return new NFormatAndValue<>(spi, texts.ofFormat(spi).get());
                }
            };
        }
    }

}
