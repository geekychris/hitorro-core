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
package com.hitorro.util.json.keys.mappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.hitorro.util.json.JsonInitable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class JsonNArrayToMapT<K, T> implements Function<JsonNode, Map<K, T>>, JsonInitable {
    private Function<JsonNode, K> keyMapper;
    private Function<JsonNode, T> mapper;

    public JsonNArrayToMapT(Function<JsonNode, K> keyMapper, Function<JsonNode, T> mapper) {
        this.keyMapper = keyMapper;
        this.mapper = mapper;
    }

    public Map<K, T> apply(JsonNode jsonNodes) {
        if (jsonNodes == null) {
            return null;
        }
        if (jsonNodes.isArray()) {
            Map<K, T> map = new HashMap<K, T>();
            int size = jsonNodes.size();
            for (int i = 0; i < size; i++) {
                JsonNode elem = jsonNodes.get(i);
                K k = keyMapper.apply(elem);
                T t = mapper.apply(elem);
                map.put(k, t);
            }
            return map;
        }
        // Also accept object shape — several config files (e.g.
        // lucene_fields.json) declare their map as {"key1": {...}, "key2": {...}}
        // rather than an array. Fall back to the JSON field name as the map key.
        if (jsonNodes.isObject()) {
            Map<K, T> map = new HashMap<K, T>();
            var it = jsonNodes.fields();
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> e = it.next();
                T t = mapper.apply(e.getValue());
                K k = keyMapper.apply(e.getValue());
                // If the value carries its own key (a "name" property etc),
                // prefer that; otherwise use the field name and cast — this
                // path only kicks in when K is String, which is the case for
                // every existing MapProperty<String, ?> declaration.
                if (k == null) {
                    @SuppressWarnings("unchecked")
                    K asString = (K) e.getKey();
                    k = asString;
                }
                map.put(k, t);
            }
            return map;
        }
        return null;
    }
}
