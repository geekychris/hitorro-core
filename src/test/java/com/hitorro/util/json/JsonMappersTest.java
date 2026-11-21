package com.hitorro.util.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.exc.UnrecognizedPropertyException;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins the format of HiTorro's shared mappers to what HiTorro 3.x (Jackson 2) wrote and read.
 *
 * <p>{@code jackson2-golden.txt} was produced with Jackson 2.18.2 ({@code new ObjectMapper()} and
 * {@code new ObjectMapper(new SmileFactory())}) from the same objects as below; the generator is
 * kept as {@code ~/hitorro-j3/tools/Jackson2Golden.java.txt}. If one of these tests fails, the
 * JSON HiTorro persists (basedms, kvstore, files) would change: do not update the golden file,
 * fix the mapper configuration.
 */
class JsonMappersTest {

    private static final JsonNodeFactory F = JsonNodeFactory.instance;

    /** Same fields and order as the generator's Bean. */
    public static class Bean {
        public int count = 3;
        public String name = "n";
        public Date when = new Date(1700000000123L);
        public double ratio = 0.5;
        public String nothing = null;
        public List<String> tags = List.of("b", "a");
        public Map<String, Integer> scores = new LinkedHashMap<>(Map.of("z", 1));

        public long getId() {
            return 42L;
        }

        public boolean isActive() {
            return true;
        }
    }

    public static class Prim {
        public int x;
        public String b;
    }

    static ObjectNode tree() {
        ObjectNode o = F.objectNode();
        o.put("z", 1);
        o.put("a", "x");
        o.putNull("n");
        o.put("d", 1.0);
        o.put("f", 1.5f);
        o.put("bd", new BigDecimal("1.10"));
        o.put("e", 1e20);
        o.put("big", new BigInteger("123456789012345678901234567890"));
        o.put("u", "é <>&\"\\/\t");
        o.put("t", true);
        o.set("arr", F.arrayNode().add(1).add("two").addNull().add(F.objectNode()));
        o.put("bin", new byte[]{1, 2, 3});
        o.put("l", Long.MIN_VALUE);
        return o;
    }

    static Map<String, String> golden() throws Exception {
        Map<String, String> g = new LinkedHashMap<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(
                JsonMappersTest.class.getResourceAsStream("jackson2-golden.txt"), StandardCharsets.UTF_8))) {
            r.lines().forEach(l -> {
                int i = l.indexOf('=');
                g.put(l.substring(0, i), l.substring(i + 1));
            });
        }
        return g;
    }

    @Nested
    @DisplayName("writes what Jackson 2 wrote")
    class Writing {
        @Test
        @DisplayName("JsonNode trees: compact, toString and pretty printed")
        void trees() throws Exception {
            Map<String, String> g = golden();
            assertThat(JsonMappers.json().writeValueAsString(tree())).isEqualTo(g.get("tree"));
            assertThat(tree().toString()).isEqualTo(g.get("treeToString"));
            assertThat(tree().toPrettyString().replace("\n", "\\n")).isEqualTo(g.get("treePretty"));
            assertThat(JsonMappers.json().writerWithDefaultPrettyPrinter().writeValueAsString(tree()).replace("\n", "\\n"))
                    .isEqualTo(g.get("mapperPretty"));
        }

        @Test
        @DisplayName("beans: declaration order (not sorted), dates as epoch millis, nulls included")
        void beans() throws Exception {
            Map<String, String> g = golden();
            assertThat(JsonMappers.json().writeValueAsString(new Bean())).isEqualTo(g.get("bean"));
            assertThat(JsonMappers.json().valueToTree(new Bean()).toString()).isEqualTo(g.get("beanTree"));
        }

        @Test
        @DisplayName("maps: insertion order, Date as timestamp, char as string")
        void maps() throws Exception {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("b", 1);
            m.put("a", new Date(0));
            m.put("c", null);
            m.put("d", new int[]{1, 2});
            m.put("e", 'c');
            assertThat(JsonMappers.json().writeValueAsString(m)).isEqualTo(golden().get("map"));
        }

        @Test
        @DisplayName("Smile: byte for byte what Jackson 2's SmileFactory wrote")
        void smile() throws Exception {
            assertThat(HexFormat.of().formatHex(JsonMappers.smile().writeValueAsBytes(tree())))
                    .isEqualTo(golden().get("smileHex"));
        }

        @Test
        @DisplayName("an empty bean fails, as in Jackson 2 (FAIL_ON_EMPTY_BEANS)")
        void emptyBean() {
            assertThatThrownBy(() -> JsonMappers.json().writeValueAsString(new Object()))
                    .isInstanceOf(tools.jackson.databind.exc.InvalidDefinitionException.class);
        }
    }

    @Nested
    @DisplayName("reads what Jackson 2 wrote and accepted")
    class Reading {
        @Test
        @DisplayName("Smile written by Jackson 2")
        void smile() throws Exception {
            byte[] bytes = HexFormat.of().parseHex(golden().get("smileHex"));
            JsonNode n = JsonMappers.smile().readTree(bytes);
            // as with Jackson 2, the BigDecimal 1.10 comes back as the double 1.1
            assertThat(JsonMappers.json().writeValueAsString(n)).isEqualTo(golden().get("smileRead"));
        }

        @Test
        @DisplayName("JSON written by Jackson 2 reads back to an equal tree")
        void jsonRoundTrip() throws Exception {
            JsonNode n = JsonMappers.json().readTree(golden().get("tree"));
            assertThat(n.toString()).isEqualTo(golden().get("jsonRead"));
            Bean b = JsonMappers.json().readValue(golden().get("bean").replace(",\"id\":42,\"active\":true", ""), Bean.class);
            assertThat(b.when).isEqualTo(new Date(1700000000123L));
            assertThat(b.tags).containsExactly("b", "a");
        }

        @Test
        @DisplayName("number node types: int, long, BigInteger, double (not BigDecimal)")
        void numberTypes() throws Exception {
            JsonNode rn = JsonMappers.json().readTree("{\"i\":1,\"l\":12345678901,\"bi\":123456789012345678901234567890,\"d\":1.10}");
            StringJoiner sj = new StringJoiner(",", "", ",");
            rn.properties().forEach(e -> sj.add(e.getKey() + ":" + e.getValue().getClass().getSimpleName()));
            assertThat(sj.toString()).isEqualTo(golden().get("readNumberTypes"));
        }

        @Test
        @DisplayName("trailing content after the first value is ignored (FAIL_ON_TRAILING_TOKENS off)")
        void trailingTokens() throws Exception {
            assertThat(JsonMappers.json().readTree("{\"a\":1} xyz").toString()).isEqualTo("{\"a\":1}");
        }

        @Test
        @DisplayName("null for a primitive gives the default (FAIL_ON_NULL_FOR_PRIMITIVES off)")
        void nullPrimitive() throws Exception {
            assertThat(JsonMappers.json().readValue("{\"x\":null}", Prim.class).x).isZero();
        }

        @Test
        @DisplayName("unknown properties fail, as in Jackson 2 (FAIL_ON_UNKNOWN_PROPERTIES on)")
        void unknownProperty() {
            assertThatThrownBy(() -> JsonMappers.json().readValue("{\"x\":1,\"nope\":2}", Prim.class))
                    .isInstanceOf(UnrecognizedPropertyException.class);
        }

        @Test
        @DisplayName("duplicate keys: the last one wins")
        void duplicateKeys() throws Exception {
            assertThat(JsonMappers.json().readTree("{\"a\":1,\"a\":2}").get("a").intValue()).isEqualTo(2);
        }
    }

    @Test
    @DisplayName("builder() starts from Jackson 2 defaults and can be changed")
    void builder() throws Exception {
        var sorted = JsonMappers.builder()
                .enable(tools.jackson.databind.MapperFeature.SORT_PROPERTIES_ALPHABETICALLY).build();
        assertThat(sorted.writeValueAsString(new Prim())).isEqualTo("{\"b\":null,\"x\":0}");
        assertThat(JsonMappers.json().writeValueAsString(new Prim())).isEqualTo("{\"x\":0,\"b\":null}");
    }
}
