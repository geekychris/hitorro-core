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

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Constructs the concrete {@link Sink} implementations that Sink's {@code static} and
 * {@code default} methods return. Introduced so that {@code Sink} itself has no
 * compile-time dependency on {@link BaseSink}, {@link CountingSink},
 * {@link MaxItemsPerTransactionSink}, {@link PredicatedSink}, {@link MappingSink},
 * {@link TeeSink} — the dependency lives in the factory implementation instead.
 *
 * <p>{@link DefaultSinkFactory} is the built-in implementation. Callers generally do not
 * interact with this interface; it exists to support future module boundaries.
 */
public interface SinkFactory {

    <T> Sink<T> from(Consumer<T> consumer);

    <T> CountingSink<T> counting();

    <T> Sink<T> maxPerTransaction(Sink<T> delegate, long max);

    <T> Sink<T> filter(Sink<T> delegate, Predicate<T> predicate);

    <I, T> Sink<I> map(Sink<T> delegate, Function<I, T> function);

    <T> Sink<T> tee(Sink<T> a, Sink<T> b);
}
