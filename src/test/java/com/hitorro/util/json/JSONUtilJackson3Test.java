package com.hitorro.util.json;

import com.hitorro.util.json.keys.mappers.JsonNodeToBoolean;
import com.hitorro.util.json.keys.mappers.JsonNodeToDouble;
import com.hitorro.util.json.keys.mappers.JsonNodeToInteger;
import com.hitorro.util.json.keys.mappers.JsonNodeToLong;
import com.hitorro.util.json.keys.mappers.JsonNodeToString;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Behaviour of hitorro-core's JSON helpers after the move to Jackson 3: loosely typed input
 * gives the same results as with Jackson 2 instead of throwing.
 */
class JSONUtilJackson3Test {

    private static final JsonNodeFactory F = JsonNodeFactory.instance;

    @Test
    @DisplayName("getString/getLong/getDouble on the wrong node type give null/default/0, not an exception")
    void lenientGetters() {
        assertThat(JSONUtil.getString(F.numberNode(5))).isNull();
        assertThat(JSONUtil.getString(F.stringNode("s"))).isEqualTo("s");
        assertThat(JSONUtil.getString(null)).isNull();
        assertThat(JSONUtil.getLong(F.stringNode("12"), 9)).isZero();
        assertThat(JSONUtil.getLong(F.numberNode(3.9), 9)).isEqualTo(3);
        assertThat(JSONUtil.getLong(null, 9)).isEqualTo(9);
        assertThat(JSONUtil.getDouble(F.nullNode(), 1.5)).isZero();
        assertThat(JSONUtil.getDouble(F.numberNode(2), 1.5)).isEqualTo(2.0);
    }

    @Test
    @DisplayName("arrays of mixed values: non-strings become null, non-numbers 0")
    void arrays() {
        ArrayNode a = F.arrayNode().add("a").add(1).addNull().add(2.7);
        assertThat(JSONUtil.getStringList(a)).containsExactly("a", null, null, null);
        assertThat(JSONUtil.getLongArray(a)).containsExactly(0, 1, 0, 2);
        assertThat(JSONUtil.getStringList(F.objectNode())).isNull();
    }

    @Test
    @DisplayName("object helpers keep insertion order")
    void objects() {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("z", "1");
        m.put("a", "2");
        ObjectNode on = JSONUtil.map2json(m);
        assertThat(on.toString()).isEqualTo("{\"z\":\"1\",\"a\":\"2\"}");
        String[] keys = new String[2];
        JsonNode[] vals = new JsonNode[2];
        JSONUtil.populateKeyValue(on, keys, vals);
        assertThat(keys).containsExactly("z", "a");
        assertThat(JSONUtil.mergeValues(on, "|")).isEqualTo("1|2");
        ObjectNode root = F.objectNode();
        root.set("kids", on);
        assertThat(JSONUtil.getSubMaps(root, "kids")).hasSize(2);
        assertThat(JSONUtil.getSubMaps(root, "none")).isEmpty();
    }

    @Test
    @DisplayName("ensureJsonNode makes string nodes (StringNode, Jackson 3's TextNode)")
    void ensureJsonNode() {
        assertThat(JSONUtil.ensureJsonNode("x")).isInstanceOf(StringNode.class);
        assertThat(JSONUtil.ensureJsonNode(3L).longValue()).isEqualTo(3L);
        assertThat(JSONUtil.ensureJsonNode(new Object())).isNull();
        assertThat(JSONUtil.nullOrEmpty(F.arrayNode())).isTrue();
        assertThat(JSONUtil.nullOrEmpty(F.stringNode(""))).isFalse();
    }

    @Test
    @DisplayName("key mappers convert numbers given as numbers or text")
    void keyMappers() {
        assertThat(JsonNodeToInteger.instance.apply(F.numberNode(4))).isEqualTo(4);
        assertThat(JsonNodeToInteger.instance.apply(F.stringNode("4"))).isEqualTo(4);
        assertThat(new JsonNodeToLong().apply(F.stringNode("40"))).isEqualTo(40L);
        assertThat(new JsonNodeToLong().apply(F.numberNode(40L))).isEqualTo(40L);
        assertThat(new JsonNodeToDouble().apply(F.stringNode("1.5"))).isEqualTo(1.5);
        assertThat(JsonNodeToBoolean.instance.apply(F.booleanNode(true))).isTrue();
        assertThat(JsonNodeToBoolean.instance.apply(F.stringNode("true"))).isTrue();
        assertThat(new JsonNodeToString().apply(F.numberNode(7))).isNull(); // as in Jackson 2: textValue of a number
        assertThat(new JsonNodeToString().apply(F.stringNode("s"))).isEqualTo("s");
        assertThat(new JsonNodeTransEnum().string2Json(F.numberNode(7))).isNull();
    }
}
