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
package com.hitorro.util.urlparser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UrlCreator")
class UrlCreatorTest {

    @Nested
    @DisplayName("URL Building")
    class UrlBuilding {

        @Test
        @DisplayName("Should build URL with single parameter")
        void shouldBuildWithSingleParam() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com/search");
            creator.addArg("q", "test");
            assertThat(creator.getUrl()).isEqualTo("http://example.com/search?q=test");
        }

        @Test
        @DisplayName("Should build URL with multiple parameters")
        void shouldBuildWithMultipleParams() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com/api");
            creator.addArg("key", "abc");
            creator.addArg("format", "json");
            String url = creator.getUrl();
            assertThat(url).startsWith("http://example.com/api?");
            assertThat(url).contains("key=abc");
            assertThat(url).contains("format=json");
            assertThat(url).contains("&");
        }

        @Test
        @DisplayName("Should URL-encode parameter values")
        void shouldEncodeValues() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com/search");
            creator.addArg("q", "hello world");
            assertThat(creator.getUrl()).contains("hello+world");
        }

        @Test
        @DisplayName("Should URL-encode special characters in keys")
        void shouldEncodeKeys() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com/api");
            creator.addArg("my param", "value");
            assertThat(creator.getUrl()).contains("my+param=value");
        }

        @Test
        @DisplayName("Should cache URL until args change")
        void shouldCacheUrl() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com");
            creator.addArg("a", "1");
            String first = creator.getUrl();
            String second = creator.getUrl();
            assertThat(first).isSameAs(second);
        }

        @Test
        @DisplayName("Should invalidate cache when adding new arg")
        void shouldInvalidateCache() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://example.com");
            creator.addArg("a", "1");
            String first = creator.getUrl();
            creator.addArg("b", "2");
            String second = creator.getUrl();
            assertThat(second).isNotEqualTo(first);
            assertThat(second).contains("b=2");
        }

        @Test
        @DisplayName("Should reset on setUrl")
        void shouldResetOnSetUrl() {
            UrlCreator creator = new UrlCreator();
            creator.setUrl("http://first.com");
            creator.addArg("old", "param");
            creator.setUrl("http://second.com");
            creator.addArg("new", "param");
            assertThat(creator.getUrl()).startsWith("http://second.com");
            assertThat(creator.getUrl()).doesNotContain("old");
        }
    }
}
