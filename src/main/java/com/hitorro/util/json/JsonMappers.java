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

import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.smile.SmileFactory;
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
 * <p>The stream limits are Jackson 2.18's too ({@link #JACKSON2_READ_CONSTRAINTS},
 * {@link #JACKSON2_WRITE_CONSTRAINTS}): Jackson 3 halved the nesting depth to 500 (reading and
 * writing) and raised the string length to 100 MB, and {@code builderWithJackson2Defaults()}
 * restores neither.
 *
 * <p>JDK types that Jackson 3 writes or reads differently (Locale, Month, {@code byte[]} trees) are
 * restored by {@link Jackson2JdkModule}, which every builder here registers.
 *
 * <p>Jackson 3 mappers are immutable and thread safe; to change a setting, start from
 * {@link #builder()} (or {@code json().rebuild()}) and build a new mapper.
 */
public final class JsonMappers {

    /**
     * Jackson 2.18's default read limits (measured from 2.18.12; 2.21 is the same): nesting
     * depth 1000 (Jackson 3: 500), string length 20,000,000 chars (Jackson 3: 100,000,000),
     * number length 1000, property name length 50,000, document length and token count
     * unlimited.
     */
    public static final StreamReadConstraints JACKSON2_READ_CONSTRAINTS = StreamReadConstraints.builder()
            .maxNestingDepth(1000)
            .maxStringLength(20_000_000)
            .maxNumberLength(1000)
            .maxNameLength(50_000)
            .maxDocumentLength(-1L)
            .maxTokenCount(-1L)
            .build();

    /** Jackson 2.18's default write limits: nesting depth 1000 (Jackson 3: 500). */
    public static final StreamWriteConstraints JACKSON2_WRITE_CONSTRAINTS = StreamWriteConstraints.builder()
            .maxNestingDepth(1000)
            .build();

    private static final JsonMapper JSON = builder().build();
    private static final SmileMapper SMILE = smileBuilder().build();

    private JsonMappers() {
    }

    /**
     * A builder for a JSON mapper with Jackson 2 defaults: what {@code new ObjectMapper()} was.
     * It is {@link JsonMapper#builderWithJackson2Defaults()} plus Jackson 2's stream limits.
     */
    public static JsonMapper.Builder builder() {
        return JsonMapper.builder(jsonFactory()).configureForJackson2()
                .addModule(new Jackson2JdkModule());
    }

    /** The JSON token stream factory under {@link #builder()}: Jackson 2 defaults and limits. */
    public static JsonFactory jsonFactory() {
        return JsonFactory.builderWithJackson2Defaults()
                .streamReadConstraints(JACKSON2_READ_CONSTRAINTS)
                .streamWriteConstraints(JACKSON2_WRITE_CONSTRAINTS)
                .build();
    }

    /**
     * The shared JSON mapper, with Jackson 2 defaults. Use it instead of {@code new ObjectMapper()}.
     */
    public static JsonMapper json() {
        return JSON;
    }

    /**
     * A builder for a Smile (binary JSON) mapper with Jackson 2 defaults and limits.
     * ({@code SmileMapper.builder().configureForJackson2()} alone sets only the mapper's
     * features, not the factory's.)
     */
    public static SmileMapper.Builder smileBuilder() {
        SmileFactory factory = SmileFactory.builder()
                .configureForJackson2()
                .streamReadConstraints(JACKSON2_READ_CONSTRAINTS)
                .streamWriteConstraints(JACKSON2_WRITE_CONSTRAINTS)
                .build();
        return SmileMapper.builder(factory).configureForJackson2()
                .addModule(new Jackson2JdkModule());
    }

    /**
     * The shared Smile mapper, with Jackson 2 defaults. Replaces {@code new ObjectMapper(new SmileFactory())}.
     */
    public static SmileMapper smile() {
        return SMILE;
    }
}
