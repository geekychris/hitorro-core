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

import java.util.function.Supplier;

/**
 * Static holder for the global properties JsonNode.
 * This replaces JVSProperties for modules that cannot depend on the jsontypesystem module.
 * The properties are typically set at startup by the application layer.
 *
 * A property provider can be registered (e.g., by JVSProperties) to supply properties
 * when the local defaultProperties hasn't been explicitly set.
 */
public class GlobalProperties {
    private static JsonNode defaultProperties = JsonNodeFactory.instance.objectNode();
    private static volatile Supplier<JsonNode> propertyProvider;

    public synchronized static void setDefaultProperties(JsonNode props) {
        defaultProperties = props;
    }

    /**
     * Register a provider that supplies the properties JsonNode.
     * This allows JVSProperties (in jsontypesystem) to bridge its properties
     * to hitorro-core without a compile dependency.
     */
    public static void setPropertyProvider(Supplier<JsonNode> provider) {
        propertyProvider = provider;
    }

    public static JsonNode getProperties() {
        if (propertyProvider != null) {
            JsonNode provided = propertyProvider.get();
            if (provided != null) {
                return provided;
            }
        }
        return defaultProperties;
    }

    public static String resolveJsonVariable(String value) {
        return PropertiesUtil.resolveJsonVariable(value, true, null, getProperties());
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
