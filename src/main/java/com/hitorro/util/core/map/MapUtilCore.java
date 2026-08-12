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
package com.hitorro.util.core.map;

import com.hitorro.util.core.string.StringUtilCore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * JDK-only subset of {@link MapUtil}. Contains the property-tree navigation helpers
 * (getChildKeys, getSubMap, getPropertySubProperties, extractPropertySubMap) used by
 * config/property machinery — no dependency on CSVReader/CSVConsumer or other iterator
 * infrastructure. Anything more elaborate (CSV loading, KeyMap tree building) lives on
 * the full MapUtil in hitorro-streams.
 */
public final class MapUtilCore {
    private MapUtilCore() {}

    /** Empty {@link java.util.HashMap} factory. */
    public static Map map() { return new java.util.HashMap(); }

    /**
     * Add a value to a list under a key; create the list on first use. Behaves like the
     * original {@code MapUtil.add(Map<L, List<K>>, L, K)}.
     */
    public static <L, K> void add(Map<L, List<K>> map, L key, K elem) {
        List<K> list = map.get(key);
        if (list == null) { list = new ArrayList<>(); map.put(key, list); }
        list.add(elem);
    }

    /** Trove overload for {@link gnu.trove.map.hash.TLongObjectHashMap} keyed lists. */
    public static <K> void add(gnu.trove.map.hash.TLongObjectHashMap<List<K>> map, long key, K elem) {
        List<K> list = map.get(key);
        if (list == null) { list = new ArrayList<>(); map.put(key, list); }
        list.add(elem);
    }

    /** Simple {@link Map} builder from a flat array of alternating key/value pairs. */
    public static Map<Object, Object> createMapFromArray(Object[] array, int incrementor, int keyIndex, int valueIndex) {
        int mod = array.length % incrementor;
        if (mod > 0) return null;
        java.util.HashMap<Object, Object> m = new java.util.HashMap<>();
        for (int i = 0; i + incrementor <= array.length; i += incrementor) {
            m.put(array[i + keyIndex], array[i + valueIndex]);
        }
        return m;
    }

    public static Map<Object, Object> createMapFromArray(Object[] array) {
        return createMapFromArray(array, 2, 0, 1);
    }

    public static <E> void extractPropertySubMap(TreeMap<String, E> props, String prefix, Map result) {
        if (prefix == null) return;
        prefix = prefix.toLowerCase();

        String fromKey = StringUtilCore.strcat(prefix, ".");
        String toKey = StringUtilCore.strcat(prefix, "/");
        if (props == null) return;

        SortedMap range = props.subMap(fromKey, toKey);
        Iterator keyI = range.keySet().iterator();
        while (keyI.hasNext()) {
            String key = (String) keyI.next();
            E value = props.get(key);
            String stripped = key.substring(prefix.length() + 1);
            result.put(stripped, value);
        }
    }

    public static TreeMap<String, String> getSubMap(TreeMap<String, String> props, String prefix) {
        TreeMap<String, String> result = new TreeMap<>();
        extractPropertySubMap(props, prefix, result);
        return result;
    }

    public static List<String> getChildKeys(String prefix, TreeMap<String, String> map) {
        if (prefix == null) return null;
        String fromKey = StringUtilCore.strcat(prefix, ".");
        String toKey = StringUtilCore.strcat(prefix, "/");
        SortedMap<String, String> subMap = map.subMap(fromKey, toKey);

        List<String> childKeys = new ArrayList<>();
        String lastKey = null;
        int prefixLen = prefix.length() + 1;
        for (String key : subMap.keySet()) {
            int dot = key.indexOf('.', prefixLen);
            String childKey = (dot == -1) ? key.substring(prefixLen) : key.substring(prefixLen, dot);
            if (!childKey.equals(lastKey)) {
                childKeys.add(childKey);
                lastKey = childKey;
            }
        }
        return childKeys;
    }

    public static Properties getPropertySubProperties(TreeMap<String, String> props, String prefix) {
        Properties result = new Properties();
        extractPropertySubMap(props, prefix, result);
        return result;
    }

    public static Map<String, String> createStringIdentityMap(java.util.List list) {
        Map<String, String> map = new HashMap<>();
        for (Object a : list) {
            String s = a.toString();
            map.put(s, s);
        }
        return map;
    }

    public static Map<String, String> createStringIdentityMap(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (String a : args) {
            map.put(a, a);
        }
        return map;
    }
}
