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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.junit.Test;

public class BytesTest {

  private static final Bytes BYTES_EMPTY = Bytes.EMPTY;
  private static final Bytes BYTES_STRING = Bytes.of("test String");
  private static final Bytes BYTES_STRING_CHARSET = Bytes.of("test String with Charset",
      StandardCharsets.US_ASCII);
  private static final Bytes BYTES_CHARSEQ = Bytes.of(new StringBuilder("test CharSequence"));
  private static final Bytes BYTES_CHARSEQ_CHARSET = Bytes.of(new StringBuilder(
      "test CharSequence with Charset"), StandardCharsets.US_ASCII);
  private static final Bytes BYTES_BB = Bytes.of(ByteBuffer.wrap("test ByteBuffer"
      .getBytes(StandardCharsets.US_ASCII)));
  private static final Bytes BYTES_ARRAY = Bytes.of("test byte[]"
      .getBytes(StandardCharsets.US_ASCII));
  private static final Bytes BYTES_ARRAY_OFFSET = Bytes.of(
      "---test byte[] with offset and length---".getBytes(StandardCharsets.US_ASCII), 3, 34);

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
  }

  @Test
  public void testEndsWith() {
    assertTrue(BYTES_EMPTY.endsWith(BYTES_EMPTY));
    assertFalse(BYTES_EMPTY.endsWith(BYTES_STRING));
    assertTrue(BYTES_STRING.endsWith(BYTES_EMPTY));
    assertTrue(BYTES_STRING.endsWith(Bytes.of("ing")));
    assertFalse(Bytes.of("ing").endsWith(BYTES_STRING));
  }

}
