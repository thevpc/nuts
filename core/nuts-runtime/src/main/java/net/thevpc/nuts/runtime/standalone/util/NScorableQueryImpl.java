package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.spi.NDefaultScorableContext;
import net.thevpc.nuts.spi.NScorable;
import net.thevpc.nuts.spi.NScorableContext;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NScorableQueryImpl<T extends NScorable> implements NScorable.Query<T> {
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

    public NScorableQueryImpl(NScorableContext context) {
        NAssert.requireNonNull(context, "context");
        this.context = context;
    }

    public NScorableQueryImpl<T> withContext(NScorableContext context) {
        this.context = context;
        return this;
    }

    @Override
    public NScorable.Query<T> fromSupplierOfStream(Stream<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.STREAM_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromStream(Stream<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.STREAM));
        }
        return this;
    }
    @Override
    public NScorable.Query<T> fromSupplierOfStream(NStream<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.NSTREAM_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromStream(NStream<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.NSTREAM));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromIterable(Iterable<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERABLE));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromIterator(Iterator<T> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERATOR));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromIterableOfSupplier(Iterable<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERABLE_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> fromIteratorOrSupplier(Iterator<Supplier<T>> source) {
        if (source != null) {
            all.add(new Src(source, SrcType.ITERATOR_SUPPLIER));
        }
        return this;
    }

    @Override
    public NScorable.Query<T> withName(NMsg source) {
        return null;
    }

    @Override
    public NScorable.Query<T> withName(Supplier<NMsg> source) {
        if (source == null) {
            this.emptyMessage = null;
        } else {
            this.emptyMessage = () -> {
                NMsg u = source.get();
                return NMsg.ofC("missing %s", u == null ? "scorable" : u);
            };
        }
        return null;
    }

    @Override
    public NScorable.Query<T> withEmptyMessage(Supplier<NMsg> source) {
        if (source == null) {
            this.emptyMessage = null;
        } else {
            this.emptyMessage = () -> {
                NMsg u = source.get();
                return u == null ? NMsg.ofC("missing scorable") : u;
            };
        }
        return null;
    }

    @Override
    public List<T> getAll() {
        return getResults().stream().map(x -> x.value()).collect(Collectors.toList());
    }

    private NScorable.Result<T> findBestFromIteratorOfSupplier(Iterator<Supplier<T>> srcOk, NScorable.Result<T> track) {
        NScorableContext context = this.context==null?NScorableContext.of():this.context;
        while (srcOk.hasNext()) {
            Supplier<T> ss = srcOk.next();
            if (ss != null) {
                T s = ss.get();
                if (s != null) {
                    int score = s.getScore(context);
                    if (score > 0) {
                        if (track == null || (score > track.score())) {
                            track = new ResultImpl<T>(s, score, context);
                        }
                    }
                }
            }
        }
        return track;
    }

    private NScorable.Result<T> findBestFromIterator(Iterator<T> srcOk, NScorable.Result<T> track) {
        NScorableContext context = this.context==null?NScorableContext.of():this.context;
        while (srcOk.hasNext()) {
            T s = srcOk.next();
            if (s != null) {
                int score = s.getScore(context);
                if (score > 0) {
                    if (track == null || (score > track.score())) {
                        track = new ResultImpl<T>(s, score, context);
                    }
                }
            }
        }
        return track;
    }

    private void fillIterator(Iterator<T> srcOk, List<NScorable.Result<T>> track) {
        NScorableContext context = this.context==null?NScorableContext.of():this.context;
        while (srcOk.hasNext()) {
            T s = srcOk.next();
            if (s != null) {
                int score = s.getScore(context);
                if (score > 0) {
                    track.add(new ResultImpl<T>(s, score, context));
                }
            }
        }
    }

    private void fillIteratorOfSupplier(Iterator<Supplier<T>> srcOk, List<NScorable.Result<T>> track) {
        NScorableContext context = this.context==null?NScorableContext.of():this.context;
        while (srcOk.hasNext()) {
            Supplier<T> ss = srcOk.next();
            if (ss != null) {
                T s = ss.get();
                if (s != null) {
                    int score = s.getScore(context);
                    if (score > 0) {
                        track.add(new ResultImpl<T>(s, score, context));
                    }
                }
            }
        }
    }

    public List<NScorable.Result<T>> getResults() {
        List<NScorable.Result<T>> track = new ArrayList<>();
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
    public NOptional<NScorable.Result<T>> getBestResult() {
        NScorable.Result<T> track = null;
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
