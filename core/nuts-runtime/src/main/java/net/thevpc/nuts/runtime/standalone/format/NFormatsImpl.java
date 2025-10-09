package net.thevpc.nuts.runtime.standalone.format;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExecCmd;
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
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.runtime.standalone.format.impl.NChronometerNFormatSPI;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.NClassMap;
import net.thevpc.nuts.util.NOptional;

public class NFormatsImpl implements NFormats {
    private NClassMap<NFormatMapper> mapper = new NClassMap<>(NFormatMapper.class);

    public NFormatsImpl() {
        registerDefaults();
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
        register(NExecCmd.class, (o, f) -> NExecCmdFormat.of().setValue((NExecCmd) o));

        register(NVersion.class, (o, f) -> NVersionFormat.of().setVersion((NVersion) o));

        register(NId.class, (o, f) -> NIdFormat.of().setValue((NId) o));
        register(NIdBuilder.class, (o, f) -> NIdFormat.of().setValue(((NIdBuilder) o).build()));

        register(NDescriptor.class, (o, f) -> NDescriptorFormat.of().setValue((NDescriptor) o));
        register(NDescriptorBuilder.class, (o, f) -> NDescriptorFormat.of().setValue(((NDescriptorBuilder) o).build()));

        register(NDependency.class, (o, f) -> NDependencyFormat.of().setValue((NDependency) o));
        register(NDependencyBuilder.class, (o, f) -> NDependencyFormat.of().setValue(((NDependencyBuilder) o).build()));

        register(NCmdLine.class, (o, f) -> NCmdLineFormat.of().setValue((NCmdLine) o));

        register(NCompressedPath.class, (o, f) -> new NCompressedPath.MyPathFormat((NCompressedPath) o));
        register(NCompressedPathBase.class, (o, f) -> new NCompressedPathBase.MyPathFormat((NCompressedPathBase) o));
        register(NPathBase.class, (o, f) -> new NPathBase.PathFormat((NPathBase) o));

        register(NFormatSPI.class, (o, f) -> NFormat.of(((NFormatSPI) o)));
        register(NChronometer.class, (o, f) -> NFormat.of(new NChronometerNFormatSPI((NChronometer) o)));
        register(NDuration.class, (o, f) -> NFormat.of(new NDurationFormatSPI((NDuration) o)));
        register(NByteArrayPrintStream.MyAbstractMultiReadNInputSource.class, (o, f) ->
                f.ofFormat(((NByteArrayPrintStream.MyAbstractMultiReadNInputSource) o).getValue()).get());
        register(InputStreamExt.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((InputStreamExt) o, ((InputStreamExt) o).getSourceName(), "input-stream")));
        register(InputStreamTee.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((InputStreamTee) o, null, "input-stream-tee")));
        register(OutputStreamExt.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((OutputStreamExt) o, null, "output-stream")));
        register(NNonBlockingInputStreamAdapter.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((NNonBlockingInputStreamAdapter) o, ((NNonBlockingInputStreamAdapter) o).getSourceName(), "input-stream")));
        register(NInputStreamSource.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((NInputStreamSource) o, null, "input-stream")));
        register(NPrintStream.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI(((NPrintStream)o), null, "print-stream")));
        register(OutputTargetExt.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((OutputTargetExt) o,((OutputTargetExt) o).getSourceName(), "output-stream")));
        register(OutputTargetExt.class, (o, f) ->
                NFormat.of(new NContentMetadataProviderFormatSPI((OutputTargetExt) o,((OutputTargetExt) o).getSourceName(), "output-stream")));

        register(DefaultNDigest.NDescriptorInputSource.class, (o, f) -> NFormat.of(new NDescriptorInputSourceFormatSPI((DefaultNDigest.NDescriptorInputSource) o)));

        register(NPathFromSPI.class, (o, f) ->
        {
            NPathFromSPI b=(NPathFromSPI) o;
            NPathSPI base = ((NPathFromSPI) o).getBase();
            NFormatSPI fspi = null;
            if (NUseDefaultUtils.isUseDefault(base.getClass(), "formatter", NPath.class)) {
            } else {
                fspi = base.formatter(b);
            }
            if (fspi != null) {
                return new NFormatFromSPI(fspi);
            }
            return new NPathBase.PathFormat((NPathBase) o);
        });
    }

    private interface NFormatMapper {
        NFormat ofFormat(Object t, NFormats texts);
    }

    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

}
