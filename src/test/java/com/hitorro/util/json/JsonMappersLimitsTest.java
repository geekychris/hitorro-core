package com.hitorro.util.json;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JacksonException;
import tools.jackson.core.StreamReadConstraints;
import tools.jackson.core.StreamWriteConstraints;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.dataformat.smile.SmileFactory;
import tools.jackson.dataformat.smile.SmileMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Pins the shared mappers' stream limits to Jackson 2.18's defaults.
 *
 * <p>Measured with Jackson 2.18.12 ({@code new ObjectMapper()} and
 * {@code new ObjectMapper(new SmileFactory())}), each case below was accepted or rejected exactly
 * as asserted here. Jackson 3.1.8's defaults differ in three places, and
 * {@code builderWithJackson2Defaults()} restores none of them:
 * <ul>
 *   <li>read nesting depth: 2.18 = 1000, 3.1.8 = 500</li>
 *   <li>write nesting depth: 2.18 = 1000, 3.1.8 = 500</li>
 *   <li>read string length: 2.18 = 20,000,000, 3.1.8 = 100,000,000</li>
 * </ul>
 * Number length (1000), name length (50,000), document length and token count (unlimited) are
 * the same in both versions; they are pinned too.
 */
class JsonMappersLimitsTest {

    private static String nested(int n) {
        return "[".repeat(n) + "]".repeat(n);
    }

    private static JsonNode tree(int n) {
        ArrayNode root = JsonNodeFactory.instance.arrayNode();
        ArrayNode cur = root;
        for (int i = 1; i < n; i++) {
            cur = cur.addArray();
        }
        return root;
    }

    /** Writes Smile deeper than the shared mapper may, to test reading it. */
    private static final SmileMapper DEEP_SMILE_WRITER = SmileMapper.builder(SmileFactory.builder()
            .streamWriteConstraints(StreamWriteConstraints.builder().maxNestingDepth(5000).build()).build()).build();

    @Test
    @DisplayName("the constants hold Jackson 2.18's default limits")
    void constants() {
        StreamReadConstraints r = JsonMappers.JACKSON2_READ_CONSTRAINTS;
        assertThat(r.getMaxNestingDepth()).isEqualTo(1000);
        assertThat(r.getMaxStringLength()).isEqualTo(20_000_000);
        assertThat(r.getMaxNumberLength()).isEqualTo(1000);
        assertThat(r.getMaxNameLength()).isEqualTo(50_000);
        assertThat(r.getMaxDocumentLength()).isEqualTo(-1L);
        assertThat(r.getMaxTokenCount()).isEqualTo(-1L);
        assertThat(JsonMappers.JACKSON2_WRITE_CONSTRAINTS.getMaxNestingDepth()).isEqualTo(1000);
        for (ObjectMapper m : new ObjectMapper[]{JsonMappers.json(), JsonMappers.builder().build(),
                JsonMappers.json().rebuild().build(), JsonMappers.smile(), JsonMappers.smileBuilder().build()}) {
            assertThat(m.tokenStreamFactory().streamReadConstraints().getMaxNestingDepth()).isEqualTo(1000);
            assertThat(m.tokenStreamFactory().streamReadConstraints().getMaxStringLength()).isEqualTo(20_000_000);
            assertThat(m.tokenStreamFactory().streamWriteConstraints().getMaxNestingDepth()).isEqualTo(1000);
        }
    }

    @Test
    @DisplayName("JSON nesting: 1000 levels read and written, 1001 rejected (Jackson 3 default: 500)")
    void jsonNesting() {
        ObjectMapper json = JsonMappers.json();
        assertThatCode(() -> json.readTree(nested(501))).doesNotThrowAnyException();
        assertThatCode(() -> json.readTree(nested(1000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> json.readTree(nested(1001))).isInstanceOf(JacksonException.class)
                .hasMessageContaining("1000");
        assertThatCode(() -> json.writeValueAsString(tree(501))).doesNotThrowAnyException();
        assertThatCode(() -> json.writeValueAsString(tree(1000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> json.writeValueAsString(tree(1001))).isInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("Smile nesting: 1000 levels read and written, 1001 rejected")
    void smileNesting() {
        ObjectMapper smile = JsonMappers.smile();
        byte[] d1000 = DEEP_SMILE_WRITER.writeValueAsBytes(tree(1000));
        byte[] d1001 = DEEP_SMILE_WRITER.writeValueAsBytes(tree(1001));
        assertThatCode(() -> smile.readTree(d1000)).doesNotThrowAnyException();
        assertThatThrownBy(() -> smile.readTree(d1001)).isInstanceOf(JacksonException.class);
        assertThatCode(() -> smile.writeValueAsBytes(tree(1000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> smile.writeValueAsBytes(tree(1001))).isInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("string length: 20,000,000 chars read, 20,000,001 rejected, JSON and Smile (Jackson 3 default: 100,000,000)")
    void stringLength() {
        String ok = "a".repeat(20_000_000);
        String tooLong = ok + "a";
        assertThat(JsonMappers.json().readTree("\"" + ok + "\"").stringValue()).hasSize(20_000_000);
        assertThatThrownBy(() -> JsonMappers.json().readTree("\"" + tooLong + "\""))
                .isInstanceOf(JacksonException.class);
        byte[] okSmile = DEEP_SMILE_WRITER.writeValueAsBytes(JsonNodeFactory.instance.stringNode(ok));
        byte[] longSmile = DEEP_SMILE_WRITER.writeValueAsBytes(JsonNodeFactory.instance.stringNode(tooLong));
        assertThatCode(() -> JsonMappers.smile().readTree(okSmile)).doesNotThrowAnyException();
        assertThatThrownBy(() -> JsonMappers.smile().readTree(longSmile)).isInstanceOf(JacksonException.class);
    }

    @Test
    @DisplayName("number length 1000 and name length 50,000: unchanged, pinned")
    void numberAndNameLength() {
        assertThatCode(() -> JsonMappers.json().readTree("1".repeat(1000))).doesNotThrowAnyException();
        assertThatThrownBy(() -> JsonMappers.json().readTree("1".repeat(1001))).isInstanceOf(JacksonException.class);
        assertThatCode(() -> JsonMappers.json().readTree("{\"" + "k".repeat(50_000) + "\":1}")).doesNotThrowAnyException();
        assertThatThrownBy(() -> JsonMappers.json().readTree("{\"" + "k".repeat(50_001) + "\":1}"))
                .isInstanceOf(JacksonException.class);
    }
}
