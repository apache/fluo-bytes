/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.bytes;

import java.nio.ByteBuffer;

import org.junit.Test;

import static java.nio.charset.StandardCharsets.ISO_8859_1;
import static java.nio.charset.StandardCharsets.US_ASCII;
import static java.nio.charset.StandardCharsets.UTF_16;
import static java.nio.charset.StandardCharsets.UTF_16BE;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BytesTest {

  private static final Bytes BYTES_EMPTY = Bytes.EMPTY;
  private static final Bytes BYTES_STRING = Bytes.of("test String");
  private static final Bytes BYTES_STRING_CHARSET = Bytes.of("test String with Charset", US_ASCII);
  private static final Bytes BYTES_CHARSEQ = Bytes.of(new StringBuilder("test CharSequence"));
  private static final Bytes BYTES_CHARSEQ_CHARSET =
      Bytes.of(new StringBuilder("test CharSequence with Charset"), US_ASCII);
  private static final Bytes BYTES_BB =
      Bytes.of(ByteBuffer.wrap("test ByteBuffer".getBytes(US_ASCII)));
  private static final Bytes BYTES_ARRAY = Bytes.of("test byte[]".getBytes(US_ASCII));
  private static final Bytes BYTES_ARRAY_OFFSET =
      Bytes.of("---test byte[] with offset and length---".getBytes(US_ASCII), 3, 34);

  @Test
  public void testToString() {
    assertEquals("", BYTES_EMPTY.toString());
    assertEquals("test String", BYTES_STRING.toString());
    assertEquals("test String with Charset", BYTES_STRING_CHARSET.toString());
    assertEquals("test CharSequence", BYTES_CHARSEQ.toString());
    assertEquals("test CharSequence with Charset", BYTES_CHARSEQ_CHARSET.toString());
    assertEquals("test ByteBuffer", BYTES_BB.toString());
    assertEquals("test byte[]", BYTES_ARRAY.toString());
    assertEquals("test byte[] with offset and length", BYTES_ARRAY_OFFSET.toString());
  }

  @Test
  public void testBeginsWith() {
    assertTrue(BYTES_EMPTY.beginsWith(BYTES_EMPTY));
    assertFalse(BYTES_EMPTY.beginsWith(BYTES_STRING));
    assertTrue(BYTES_STRING.beginsWith(BYTES_EMPTY));
    assertTrue(BYTES_STRING_CHARSET.beginsWith(BYTES_STRING));
    assertFalse(BYTES_STRING.beginsWith(BYTES_STRING_CHARSET));
    assertFalse(BYTES_CHARSEQ.beginsWith(BYTES_STRING));
    assertFalse(Bytes.of("abcdef").beginsWith(Bytes.of("Abcd")));
    assertFalse(Bytes.of("abcdef").beginsWith(Bytes.of("abcD")));
    assertFalse(Bytes.of("abcdef").beginsWith(Bytes.of("abCd")));
  }

  @Test
  public void testEndsWith() {
    assertTrue(BYTES_EMPTY.endsWith(BYTES_EMPTY));
    assertFalse(BYTES_EMPTY.endsWith(BYTES_STRING));
    assertTrue(BYTES_STRING.endsWith(BYTES_EMPTY));
    assertTrue(BYTES_STRING.endsWith(Bytes.of("ing")));
    assertFalse(Bytes.of("ing").endsWith(BYTES_STRING));
    assertFalse(BYTES_CHARSEQ.endsWith(BYTES_STRING));
    assertFalse(Bytes.of("abcdef").endsWith(Bytes.of("Cdef")));
    assertFalse(Bytes.of("abcdef").endsWith(Bytes.of("cdeF")));
    assertFalse(Bytes.of("abcdef").endsWith(Bytes.of("cdEf")));
  }

  @Test
  public void testToArray() {
    assertArrayEquals("".getBytes(US_ASCII), BYTES_EMPTY.toArray());
    assertArrayEquals("test String".getBytes(UTF_8), BYTES_STRING.toArray());
    assertArrayEquals("test String with Charset".getBytes(UTF_8), BYTES_STRING_CHARSET.toArray());
    assertArrayEquals("test CharSequence".getBytes(UTF_8), BYTES_CHARSEQ.toArray());
    assertArrayEquals("test CharSequence with Charset".getBytes(UTF_8),
        BYTES_CHARSEQ_CHARSET.toArray());
    assertArrayEquals("test ByteBuffer".getBytes(UTF_8), BYTES_BB.toArray());
    assertArrayEquals("test byte[]".getBytes(UTF_8), BYTES_ARRAY.toArray());
    assertArrayEquals("test byte[] with offset and length".getBytes(UTF_8),
        BYTES_ARRAY_OFFSET.toArray());
    // test array with custom charset for String
    assertArrayEquals("test utf16".getBytes(UTF_16), Bytes.of("test utf16", UTF_16).toArray());
    // test array with custom charset for CharSequence
    assertArrayEquals("test ISO_8859_1".getBytes(ISO_8859_1),
        Bytes.of(new StringBuilder("test ISO_8859_1"), ISO_8859_1).toArray());
  }

  @Test
  public void testByteAt() {
    String s = "1234";
    Bytes b = Bytes.of(s, UTF_16BE);
    assertEquals(s.length() * 2, b.length()); // no BOM with UTF_16BE
    // for each char in string, check that its corresponding bytes exist in the correct position
    for (int i = 0; i < s.length(); ++i) {
      int codePoint = s.codePointAt(i);
      assertEquals(codePoint >> Byte.SIZE, b.byteAt(2 * i)); // check most significant bits
      assertEquals(codePoint & 0xFF, b.byteAt(2 * i + 1)); // check least significant bits
    }

    try {
      int a = b.byteAt(b.length());
      fail("Previous line should have failed; byte: " + a);
    } catch (IndexOutOfBoundsException e) {
      // this is expected
    }

    try {
      int a = b.byteAt(-1);
      fail("Previous line should have failed; byte: " + a);
    } catch (IndexOutOfBoundsException e) {
      // this is expected
    }
  }

  @Test
  public void testLength() {
    assertEquals(0, BYTES_EMPTY.length());
    // string length should be equal to array length, because all these use 7-bit ASCII chars with
    // US_ASCII or UTF_8 encoding
    assertEquals("test String".length(), BYTES_STRING.length());
    assertEquals("test String with Charset".length(), BYTES_STRING_CHARSET.length());
    assertEquals("test CharSequence".length(), BYTES_CHARSEQ.length());
    assertEquals("test CharSequence with Charset".length(), BYTES_CHARSEQ_CHARSET.length());
    assertEquals("test ByteBuffer".length(), BYTES_BB.length());
    assertEquals("test byte[]".length(), BYTES_ARRAY.length());
    assertEquals("test byte[] with offset and length".length(), BYTES_ARRAY_OFFSET.length());

    // UTF_16 uses 2 bytes per char
    assertEquals("test UTF_16BE".length() * 2, Bytes.of("test UTF_16BE", UTF_16BE).length());
  }

}
