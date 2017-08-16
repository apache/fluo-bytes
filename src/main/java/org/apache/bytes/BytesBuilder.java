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

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * This class provides an easy, efficient, reusable mechanism for building immutable Bytes objects.
 *
 * @since 1.0.0
 */
public class BytesBuilder extends AbstractByteSequence {

  private byte[] ba;
  private int len;

  /**
   * Construct a builder with the specified initial capacity
   *
   * @param initialCapacity the initial size of the internal buffer
   */
  public BytesBuilder(int initialCapacity) {
    ba = new byte[initialCapacity];
    len = 0;
  }

  /**
   * Construct a builder with the default initial capacity (32)
   */
  public BytesBuilder() {
    this(32);
  }

  private void ensureCapacity(int min) {
    if (ba.length < min) {
      int newLen = ba.length * 2;
      if (newLen < min) {
        newLen = min;
      }

      ba = Arrays.copyOf(ba, newLen);
    }
  }

  /**
   * Converts a character sequence to bytes using UTF-8 encoding and appends the resulting bytes
   *
   * @return self
   */
  public BytesBuilder append(CharSequence cs) {
    return append(cs, StandardCharsets.UTF_8);
  }

  public BytesBuilder append(CharSequence cs, Charset charset) {
    if (cs instanceof String) {
      return append((String) cs, charset);
    }

    ByteBuffer bb = charset.encode(CharBuffer.wrap(cs));

    int length = bb.remaining();
    ensureCapacity(len + length);
    bb.get(ba, len, length);
    len += length;
    return this;
  }

  /**
   * Converts string to bytes using UTF-8 encoding and appends bytes.
   *
   * @return self
   */
  public BytesBuilder append(String s) {
    return append(s, StandardCharsets.UTF_8);
  }

  public BytesBuilder append(String s, Charset charset) {
    return append(s.getBytes(charset));
  }

  public BytesBuilder append(Bytes b) {
    ensureCapacity(len + b.length());
    // note: Bytes always uses all of its internal array, so source offset is 0 here
    System.arraycopy(b.data, 0, ba, len, b.length());
    len += b.length();
    return this;
  }

  public BytesBuilder append(byte[] bytes) {
    ensureCapacity(len + bytes.length);
    System.arraycopy(bytes, 0, ba, len, bytes.length);
    len += bytes.length;
    return this;
  }

  /**
   * Append a single byte.
   *
   * @param b take the lower 8 bits and appends it.
   * @return self
   */
  public BytesBuilder append(int b) {
    ensureCapacity(len + 1);
    ba[len] = (byte) b;
    len += 1;
    return this;
  }

  /**
   * Append a section of bytes from array
   *
   * @param bytes - bytes to be appended
   * @param offset - start of bytes to be appended
   * @param length - how many bytes from 'offset' to be appended
   * @return self
   */
  public BytesBuilder append(byte[] bytes, int offset, int length) {
    ensureCapacity(len + length);
    System.arraycopy(bytes, offset, ba, len, length);
    len += length;
    return this;
  }

  /**
   * Append a sequence of bytes from an InputStream
   *
   * @param in data source to append from
   * @param length number of bytes to read from data source
   * @return self
   */
  public BytesBuilder append(InputStream in, int length) throws IOException {
    ensureCapacity(len + length);
    new DataInputStream(in).readFully(ba, len, length);
    len += length;
    return this;
  }

  /**
   * Append data from a ByteBuffer
   *
   * @param bb data is read from the ByteBuffer in such a way that its position is not changed.
   * @return self
   */
  public BytesBuilder append(ByteBuffer bb) {
    int length = bb.remaining();
    ensureCapacity(len + length);
    bb.duplicate().get(ba, len, length);
    len += length;
    return this;
  }

  /**
   * Sets the point at which appending will start. This method can shrink or grow the ByteBuilder
   * from its current state. If it grows it will zero pad.
   */
  public void setLength(int newLen) {
    if (newLen < 0) {
      throw new IllegalArgumentException("Negative length passed : " + newLen);
    }
    if (newLen > ba.length) {
      ba = Arrays.copyOf(ba, newLen);
    }

    if (newLen > len) {
      Arrays.fill(ba, len, newLen, (byte) 0);
    }

    len = newLen;
  }

  @Override
  public int length() {
    return len;
  }

  public Bytes toBytes() {
    return Bytes.of(ba, 0, len);
  }

  @Override
  public byte byteAt(int index) {
    return ba[index];
  }

  @Override
  public ByteSequence subSequence(int start, int end) {
    return Bytes.of(ba, start, end - start);
  }

}
