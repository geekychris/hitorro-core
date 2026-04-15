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
package com.hitorro.util.core.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("WeakReferenceList")
class WeakReferenceListTest {

    @Nested
    @DisplayName("Basic Operations")
    class BasicOperations {

        @Test
        @DisplayName("Should add and retrieve elements")
        void shouldAddAndRetrieve() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            list.add(s);
            assertThat(list.size()).isEqualTo(1);
            assertThat(list.get(0)).isEqualTo("hello");
        }

        @Test
        @DisplayName("Should add duplicates")
        void shouldAddDuplicates() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            list.add(s);
            list.add(s);
            assertThat(list.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("addIfAbsent")
    class AddIfAbsent {

        @Test
        @DisplayName("Should add if not present")
        void shouldAddIfNotPresent() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            assertThat(list.addIfAbsent(s)).isTrue();
            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should not add if same reference present")
        void shouldNotAddIfPresent() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            list.addIfAbsent(s);
            assertThat(list.addIfAbsent(s)).isFalse();
            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should add different objects with same value (identity check)")
        void shouldAddDifferentObjectsWithSameValue() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s1 = new String("hello");
            String s2 = new String("hello");
            list.addIfAbsent(s1);
            assertThat(list.addIfAbsent(s2)).isTrue();
            assertThat(list.size()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("remove")
    class Remove {

        @Test
        @DisplayName("Should remove by reference identity")
        void shouldRemoveByIdentity() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            list.add(s);
            assertThat(list.remove(s)).isTrue();
            assertThat(list.size()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return false if not found")
        void shouldReturnFalseIfNotFound() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            assertThat(list.remove("hello")).isFalse();
        }
    }

    @Nested
    @DisplayName("removeNulls")
    class RemoveNulls {

        @Test
        @DisplayName("Should preserve insertion order")
        void shouldPreserveOrder() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String a = "a";
            String b = "b";
            String c = "c";
            list.add(a);
            list.add(b);
            list.add(c);
            // No nulls to remove, but order should be preserved
            list.removeNulls();
            assertThat(list.get(0)).isEqualTo("a");
            assertThat(list.get(1)).isEqualTo("b");
            assertThat(list.get(2)).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("getIndexOfExistingObject")
    class GetIndex {

        @Test
        @DisplayName("Should find existing object")
        void shouldFindExisting() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            String s = "hello";
            list.add(s);
            assertThat(list.getIndexOfExistingObject(s)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should return -1 for missing object")
        void shouldReturnNegativeForMissing() {
            WeakReferenceList<String> list = new WeakReferenceList<>();
            assertThat(list.getIndexOfExistingObject("missing")).isEqualTo(-1);
        }
    }
}
