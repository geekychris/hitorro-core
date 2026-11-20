/*
 * Copyright (c) 2006-2026 Chris Collins
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
package com.hitorro.util.json;

import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.smile.SmileMapper;

/**
 * The Jackson 3 mappers HiTorro uses.
 *
 * <p>Jackson 3 changed many defaults (properties sorted alphabetically, dates written as
 * ISO-8601 strings instead of timestamps, unknown properties ignored, nulls for primitives
 * rejected, trailing tokens rejected, empty beans written as {@code {}}). HiTorro persists
 * JSON that other systems read, so every mapper made here behaves like a Jackson 2
 * {@code new ObjectMapper()} did in HiTorro 3.x: same output, same accepted input. Code that
 * used to write {@code new ObjectMapper()} uses {@link #json()} (or {@link #builder()} when it
 * changes settings); {@code new ObjectMapper(new SmileFactory())} becomes {@link #smile()}.
 *
 * <p>Jackson 3 mappers are immutable and thread safe; to change a setting, start from
 * {@link #builder()} (or {@code json().rebuild()}) and build a new mapper.
 */
public final class JsonMappers {

    private static final JsonMapper JSON = builder().build();
    private static final SmileMapper SMILE = smileBuilder().build();

    private JsonMappers() {
    }

    /**
     * A builder for a JSON mapper with Jackson 2 defaults
     * ({@link JsonMapper#builderWithJackson2Defaults()}): what {@code new ObjectMapper()} was.
     */
    public static JsonMapper.Builder builder() {
        return JsonMapper.builderWithJackson2Defaults();
    }

    /**
     * The shared JSON mapper, with Jackson 2 defaults. Use it instead of {@code new ObjectMapper()}.
     */
    public static JsonMapper json() {
        return JSON;
    }

    /**
     * A builder for a Smile (binary JSON) mapper with Jackson 2 defaults.
     */
    public static SmileMapper.Builder smileBuilder() {
        return SmileMapper.builder().configureForJackson2();
    }

    /**
     * The shared Smile mapper, with Jackson 2 defaults. Replaces {@code new ObjectMapper(new SmileFactory())}.
     */
    public static SmileMapper smile() {
        return SMILE;
    }
}
