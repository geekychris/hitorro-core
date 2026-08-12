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

/**
 * Holds the mutable {@link SinkFactory} reference used by {@link Sink}'s static/default
 * methods. Interfaces cannot hold mutable static state (their static fields are implicitly
 * {@code final}), so this companion class carries the plug point.
 */
public final class SinkFactoryHolder {

    // Lazily resolved by reflection so this class can live in hitorro-core
    // even though DefaultSinkFactory (and every concrete Sink) lives in hitorro-streams.
    private static volatile SinkFactory factory = loadDefault();

    private static SinkFactory loadDefault() {
        try {
            return (SinkFactory) Class.forName("com.hitorro.util.core.iterator.sinks.DefaultSinkFactory")
                    .getDeclaredConstructor().newInstance();
        } catch (Throwable t) {
            return null; // streams not on classpath; setFactory() must be called before use
        }
    }

    private SinkFactoryHolder() {}

    public static SinkFactory factory() {
        return factory;
    }

    /** Swap in a custom factory (e.g. when concrete Sink implementations live in a separate module). */
    public static void setFactory(SinkFactory f) {
        if (f == null) throw new IllegalArgumentException("factory must not be null");
        factory = f;
    }
}
