/*
 * Copyright (c) 2006-2025 Chris Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.hitorro.util.core.iterator.sinks;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.io.StoreException;
import com.hitorro.util.json.JsonInitable;

import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Place where a sync of some kind of processing queue can send its data to (if we are in some kind of vectored queue
 * mode.
 */
public interface
Sink<T> extends AutoCloseable, JsonInitable, Consumer<T> {

    /**
     * Create a simple sink from a Consumer. Useful for quick lambda-based sinks.
     */
    static <T> Sink<T> of(Consumer<T> consumer) {
        return SinkFactoryHolder.factory().from(consumer);
    }

    /**
     * Create a sink that counts items added to it. Call getCount() after processing.
     */
    static <T> CountingSink<T> counting() {
        return SinkFactoryHolder.factory().counting();
    }

    boolean init(JsonNode node);

    boolean start() throws IOException;

    default Sink<T> maxPerTransaction(long max) {
        return SinkFactoryHolder.factory().maxPerTransaction(this, max);
    }

    default Sink<T> filter(Predicate<T> predicate) {
        return SinkFactoryHolder.factory().filter(this, predicate);
    }

    default <I> Sink<I> map(Function<I, T> function) {
        return SinkFactoryHolder.factory().map(this, function);
    }

    default Sink<T> tee(Sink<T> other) {
        return SinkFactoryHolder.factory().tee(this, other);
    }

    /**
     * Make consumer friendly
     *
     * @param t
     */
    default void accept(T t) {
        try {
            add(t);
        } catch (IOException | StoreException e) {
            throw new RuntimeException("Sink.accept failed: " + e.getMessage(), e);
        }
    }

    boolean add(T o) throws IOException, StoreException;

    default boolean addAll(final Collection<T> oList) throws IOException, StoreException {
        boolean success = true;
        for (T o : oList) {
            if (!add(o)) {
                success = false;
            }
        }
        return success;
    }

    boolean stop() throws IOException;

    void close() throws IOException;

    default Sink<T> merge(Sink<T> in) {
        return in;
    }

    /**
     * Number of items this sink has accepted since {@link #start()}.
     * Default returns {@code -1} meaning "not tracked" — callers use
     * this to distinguish "sink counts nothing" from "sink has zero
     * items". Implementations that want to surface progress (UI job
     * status, log summaries, exactly-once acks) should override.
     *
     * <p>Cheap to call — must not do I/O. Callers may poll during
     * live runs (mesh pipelines' JobStatus reads this every second).</p>
     */
    default long count() {
        return -1;
    }

    /**
     * Idempotent write — sinks that support it dedupe by
     * ({@code taskId}, {@code seq}) so retried producers don't
     * double-write. Default implementation ignores the identity and
     * calls {@link #add(Object)} (at-least-once semantics; safe when
     * the caller never retries the same {@code seq}).
     *
     * <p>Sinks backed by a durable store (KV, Lucene, HTTP with an
     * idempotency key, exactly-once Kafka) should override — track the
     * highest {@code seq} seen per {@code taskId} and skip earlier ones.
     * The {@code taskId} is caller-defined — any string that identifies
     * the retryable unit of work (mesh {@code TaskDescriptor.taskId()},
     * job id, HTTP request id, ...).</p>
     *
     * <p>Not distributed-execution-specific — any pub-sub or ETL flow
     * with retries benefits.</p>
     */
    default boolean addIdempotent(String taskId, long seq, T o) throws IOException, StoreException {
        return add(o);
    }
}
