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
package com.hitorro.util.core.params;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Static holder for the global properties JsonNode.
 * This replaces JVSProperties for modules that cannot depend on the jsontypesystem module.
 * The properties are typically set at startup by the application layer.
 */
public class GlobalProperties {
    private static JsonNode defaultProperties = JsonNodeFactory.instance.objectNode();

    public synchronized static void setDefaultProperties(JsonNode props) {
        defaultProperties = props;
    }

    public static JsonNode getProperties() {
        return defaultProperties;
    }

    public static String resolveJsonVariable(String value) {
        return PropertiesUtil.resolveJsonVariable(value, true, null, defaultProperties);
    }

    /**
     * Get a string value from the global properties using a path key.
     */
    public static String getString(String path) {
        if (defaultProperties == null) {
            return null;
        }
        JsonNode node = defaultProperties.get(path);
        if (node == null || node.isMissingNode()) {
            return null;
        }
        return node.asText();
    }
}
