package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NScorableNScorableQueryImpl<T extends NScorable> implements NScorableQuery<T> {
    List<Src> all = new ArrayList<>();
    Supplier<NMsg> emptyMessage;
    NScorableContext context;

    private class Src {
        SrcType type;
        Object value;

        public Src(Object value, SrcType type) {
            this.type = type;
            this.value = value;
        }
    }

    private enum SrcType {
        STREAM,
        STREAM_SUPPLIER,
        NSTREAM,
        NSTREAM_SUPPLIER,
        ITERATOR,
        ITERATOR_SUPPLIER,
        ITERABLE,
        ITERABLE_SUPPLIER,
    }

    public NScorableNScorableQueryImpl(NScorableContext context) {
        NAssert.requireNonNull(context, "context");
        this.context = context;
    }

    public NScorableNScorableQueryImpl<T> withContext(NScorableContext context) {
        this.context = context;
        return this;
    }

    @Override
    public NScorableQuery<T> fromStreamOfSuppliers(Stream<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.STREAM_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromStream(Stream<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.STREAM));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromSingleton(T source) {
        if (source != null) {
            fromIterable(Arrays.asList(source));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromSingletonSupplier(Supplier<T> source) {
        if (source != null) {
            fromIterableOfSuppliers(Arrays.asList(source));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromStreamOfSuppliers(NStream<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.NSTREAM_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromStream(NStream<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.NSTREAM));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromIterable(Iterable<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERABLE));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromIterator(Iterator<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERATOR));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromIterableOfSuppliers(Iterable<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERABLE_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> fromIteratorOfSuppliers(Iterator<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERATOR_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorableQuery<T> withName(NMsg source) {
        this.emptyMessage = source == null ? null : () -> source;
        return this;
    }

    @Override
    public NScorableQuery<T> withName(Supplier<NMsg> source) {
        if (source == null) {
            this.emptyMessage = null;
        } else {
            this.emptyMessage = () -> {
                NMsg u = source.get();
                return NMsg.ofC("missing %s", u == null ? "scorable" : u);
            };
        }
        return this;
    }

    @Override
    public NScorableQuery<T> withEmptyMessage(Supplier<NMsg> source) {
        if (source == null) {
            this.emptyMessage = null;
        } else {
            this.emptyMessage = () -> {
                NMsg u = source.get();
                return u == null ? NMsg.ofC("missing scorable") : u;
            };
        }
        return this;
    }

    @Override
    public List<T> getAll() {
        return getResults().stream().map(x -> x.value()).collect(Collectors.toList());
    }

    private NScorableResult<T> findBestFromIteratorOfSupplier(Iterator<Supplier<T>> srcOk, NScorableResult<T> track) {
        NScorableContext context = this.context == null ? NScorableContext.of() : this.context;
        while (srcOk.hasNext()) {
            Supplier<T> ss = srcOk.next();
            if (ss != null) {
                T s = ss.get();
                if (s != null) {
                    int score = s.getScore(context);
                    if (score > 0) {
                        if (track == null || (score > track.score())) {
                            track = new NScorableResultImpl<T>(s, score, context);
                        }
                    }
                }
            }
        }
        return track;
    }

    private NScorableResult<T> findBestFromIterator(Iterator<T> srcOk, NScorableResult<T> track) {
        NScorableContext context = this.context == null ? NScorableContext.of() : this.context;
        while (srcOk.hasNext()) {
            T s = srcOk.next();
            if (s != null) {
                int score = s.getScore(context);
                if (score > 0) {
                    if (track == null || (score > track.score())) {
                        track = new NScorableResultImpl<T>(s, score, context);
                    }
                }
            }
        }
        return track;
    }

    private void fillIterator(Iterator<T> srcOk, List<NScorableResult<T>> track) {
        NScorableContext context = this.context == null ? NScorableContext.of() : this.context;
        while (srcOk.hasNext()) {
            T s = srcOk.next();
            if (s != null) {
                int score = s.getScore(context);
                if (score > 0) {
                    track.add(new NScorableResultImpl<T>(s, score, context));
                }
            }
        }
    }

    private void fillIteratorOfSupplier(Iterator<Supplier<T>> srcOk, List<NScorableResult<T>> track) {
        NScorableContext context = this.context == null ? NScorableContext.of() : this.context;
        while (srcOk.hasNext()) {
            Supplier<T> ss = srcOk.next();
            if (ss != null) {
                T s = ss.get();
                if (s != null) {
                    int score = s.getScore(context);
                    if (score > 0) {
                        track.add(new NScorableResultImpl<T>(s, score, context));
                    }
                }
            }
        }
    }

    public List<NScorableResult<T>> getResults() {
        List<NScorableResult<T>> track = new ArrayList<>();
        for (Src source : all) {
            if (source != null) {
                switch (source.type) {
                    case STREAM: {
                        fillIterator(((Stream<T>) source.value).iterator(), track);
                        break;
                    }
                    case STREAM_SUPPLIER: {
                        fillIteratorOfSupplier(((Stream<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case NSTREAM: {
                        fillIterator(((NStream<T>) source.value).iterator(), track);
                        break;
                    }
                    case NSTREAM_SUPPLIER: {
                        fillIteratorOfSupplier(((NStream<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case ITERATOR_SUPPLIER: {
                        fillIteratorOfSupplier(((Iterator<Supplier<T>>) source.value), track);
                        break;
                    }
                    case ITERATOR: {
                        fillIterator(((Iterator<T>) source.value), track);
                        break;
                    }
                    case ITERABLE_SUPPLIER: {
                        fillIteratorOfSupplier(((Iterable<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case ITERABLE: {
                        fillIterator(((Iterable<T>) source.value).iterator(), track);
                        break;
                    }
                }
            }
        }
        if (track.size() > 1) {
            Collections.sort(track, (a, b) -> Integer.compare(b.score(), a.score()));
        }
        return track;
    }


    @Override
    public NOptional<T> getBest() {
        return getBestResult().map(x -> x.value());
    }

    @Override
    public NOptional<NScorableResult<T>> getBestResult() {
        NScorableResult<T> track = null;
        for (Src source : all) {
            if (source != null) {
                switch (source.type) {
                    case STREAM: {
                        track = findBestFromIterator(((Stream<T>) source.value).iterator(), track);
                        break;
                    }
                    case STREAM_SUPPLIER: {
                        track = findBestFromIteratorOfSupplier(((Stream<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case NSTREAM: {
                        track = findBestFromIterator(((NStream<T>) source.value).iterator(), track);
                        break;
                    }
                    case NSTREAM_SUPPLIER: {
                        track = findBestFromIteratorOfSupplier(((NStream<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case ITERATOR_SUPPLIER: {
                        track = findBestFromIteratorOfSupplier(((Iterator<Supplier<T>>) source.value), track);
                        break;
                    }
                    case ITERATOR: {
                        track = findBestFromIterator(((Iterator<T>) source.value), track);
                        break;
                    }
                    case ITERABLE_SUPPLIER: {
                        track = findBestFromIteratorOfSupplier(((Iterable<Supplier<T>>) source.value).iterator(), track);
                        break;
                    }
                    case ITERABLE: {
                        track = findBestFromIterator(((Iterable<T>) source.value).iterator(), track);
                        break;
                    }
                }
            }
        }
        return NOptional.of(track, emptyMessage == null ? () -> NMsg.ofC("missing scorable") : emptyMessage);
    }


}
