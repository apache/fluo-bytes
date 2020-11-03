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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

/**
 * Represents bytes in Fluo. Bytes is an immutable wrapper around a byte array. Bytes always copies
 * on creation and never lets its internal byte array escape. Its modeled after Java's String which
 * is an immutable wrapper around a char array. It was created because there is nothing in Java like
 * it at the moment. Its very nice having this immutable type, it avoids having to do defensive
 * copies to ensure correctness. Maybe one day Java will have equivalents of String, StringBuilder,
 * and Charsequence for bytes.
 *
 * <p>
 * The reason Fluo did not use ByteBuffer is because its not immutable, even a read only ByteBuffer
 * has a mutable position. This makes ByteBuffer unsuitable for place where an immutable data type
 * is desirable, like a key for a map.
 *
 * <p>
 * Bytes.EMPTY is used to represent a Bytes object with no data.
 *
 * @since 1.0.0
 */
public final class Bytes extends AbstractByteSequence implements Comparable<Bytes>, Serializable {

  private static final long serialVersionUID = 1L;

  private final byte[] data;

  private transient WeakReference<String> utf8String;

  public static final Bytes EMPTY = new Bytes(new byte[0]);

  private int hashCode = 0;

  public Bytes() {
    data = EMPTY.data;
  }

  private Bytes(byte[] data) {
    this.data = data;
  }

  private Bytes(byte[] data, String utf8String) {
    this.data = data;
    this.utf8String = new WeakReference<>(utf8String);
  }

  /**
   * Gets a byte within this sequence of bytes
   *
   * @param i index into sequence
   * @return byte
   * @throws IllegalArgumentException if i is out of range
   */
  @Override
  public byte byteAt(int i) {
    checkBounds(i);
    return data[i];
  }

  /**
   * Gets the length of bytes
   */
  @Override
  public int length() {
    return data.length;
  }

  /**
   * Returns a portion of the Bytes object
   *
   * @param begin index of subsequence begin (inclusive)
   * @param end index of subsequence end (exclusive)
   */
  @Override
  public Bytes subSequence(int begin, int end) {
    checkBounds(begin, end);
    return Bytes.of(data, begin, end - begin);
  }

  /**
   * Returns a byte array containing a copy of the bytes
   */
  public byte[] toArray() {
    byte[] copy = new byte[length()];
    System.arraycopy(data, 0, copy, 0, length());
    return copy;
  }

  /**
   * Provides a String representation, decoding these bytes with the provided charset
   *
   * @param charset the character set to decode these bytes
   */
  public String toString(Charset charset) {
    if (charset == StandardCharsets.UTF_8) {
      // cache the utf8string if that's the charset provided
      return toString();
    }
    return new String(data, charset);
  }

  /**
   * Provides a String representation, decoding the bytes as UTF-8
   */
  @Override
  public String toString() {
    if (utf8String != null) {
      String s = utf8String.get();
      if (s != null) {
        return s;
      }
    }

    String s = new String(data, StandardCharsets.UTF_8);
    utf8String = new WeakReference<>(s);
    return s;
  }

  /**
   * @return A read only byte buffer thats backed by the internal byte array.
   */
  public ByteBuffer toByteBuffer() {
    return ByteBuffer.wrap(data).asReadOnlyBuffer();
  }

  /**
   * @return An input stream thats backed by the internal byte array
   */
  public InputStream toInputStream() {
    return new ByteArrayInputStream(data);
  }

  public void writeTo(OutputStream out) throws IOException {
    // since Bytes is immutable, its important that we do not let the internal byte array escape
    if (length() <= 32) {
      int end = length();
      for (int i = 0; i < end; i++) {
        out.write(data[i]);
      }
    } else {
      out.write(toArray());
    }
  }

  /**
   * Compares this to the passed bytes, byte by byte, returning a negative, zero, or positive result
   * if the first sequence is less than, equal to, or greater than the second. The comparison is
   * performed starting with the first byte of each sequence, and proceeds until a pair of bytes
   * differs, or one sequence runs out of byte (is shorter). A shorter sequence is considered less
   * than a longer one.
   *
   * @return comparison result
   */
  @Override
  public final int compareTo(Bytes other) {
    if (this == other) {
      return 0;
    } else {
      return compareTo(other.data);
    }
  }

  @Override
  public int compareTo(byte[] bytes) {
    int minLen = Math.min(length(), bytes.length);
    for (int i = 0, j = 0; i < minLen; i++, j++) {
      int a = (this.data[i] & 0xff);
      int b = (bytes[j] & 0xff);

      if (a != b) {
        return a - b;
      }
    }
    return length() - bytes.length;
  }

  /**
   * Returns true if, and only if, this Bytes object contains the same byte sequence as another
   * Bytes object
   */
  @Override
  public final boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other instanceof Bytes) {
      Bytes otherBytes = (Bytes) other;
      if (hashCode != 0 && otherBytes.hashCode != 0 && hashCode != otherBytes.hashCode) {
        // if both hashCodes have been pre-computed (by calling hashCode(), and fail to match, then
        // they can't be equal
        return false;
      }
      return contentEquals(otherBytes.data);
    }
    return false;
  }

  @Override
  public boolean contentEquals(byte[] bytes) {
    if (data.length != bytes.length) {
      // can't be equal if they differ in length; this is checked again in Arrays.equals, but we
      // check here because it's a prerequisite for the last byte
      // comparison optimization below
      return false;
    }
    if (data.length == 0 && bytes.length == 0) {
      // both are empty
      return true;
    }
    int lastByte = data.length - 1;
    if (data[lastByte] != bytes[lastByte]) {
      // at this point, both byte arrays are non-zero and the same length; quickly compare last byte
      // before checking the full array; this is particularly
      // helpful for sorted data which have long prefixes in common
      return false;
    }
    return Arrays.equals(data, bytes);
  }

  @Override
  public final int hashCode() {
    return hashCode == 0 ? (hashCode = Arrays.hashCode(data)) : hashCode;
  }

  /**
   * Creates a Bytes object by copying the data of the given byte array
   */
  public static final Bytes of(byte[] array) {
    Objects.requireNonNull(array);
    if (array.length == 0) {
      return EMPTY;
    }
    byte[] copy = new byte[array.length];
    System.arraycopy(array, 0, copy, 0, array.length);
    return new Bytes(copy);
  }

  /**
   * Creates a Bytes object by copying the data of a subsequence of the given byte array
   *
   * @param data Byte data
   * @param offset Starting offset in byte array (inclusive)
   * @param length Number of bytes to include
   */
  public static final Bytes of(byte[] data, int offset, int length) {
    Objects.requireNonNull(data);
    if (length == 0) {
      return EMPTY;
    }
    byte[] copy = new byte[length];
    System.arraycopy(data, offset, copy, 0, length);
    return new Bytes(copy);
  }

  /**
   * Creates a Bytes object by copying the data of the given ByteBuffer.
   *
   * @param bb Data will be read from this ByteBuffer in such a way that its position is not
   *        changed.
   */
  public static final Bytes of(ByteBuffer bb) {
    Objects.requireNonNull(bb);
    if (bb.remaining() == 0) {
      return EMPTY;
    }
    byte[] data;
    if (bb.hasArray()) {
      data =
          Arrays.copyOfRange(bb.array(), bb.position() + bb.arrayOffset(),
              bb.limit() + bb.arrayOffset());
    } else {
      data = new byte[bb.remaining()];
      // duplicate so that it does not change position
      bb.duplicate().get(data);
    }
    return new Bytes(data);
  }

  public static final Bytes of(CharSequence cs, Charset charset) {
    if (cs instanceof String) {
      return of((String) cs, charset);
    }

    Objects.requireNonNull(cs);
    Objects.requireNonNull(charset);

    if (cs instanceof String) {
      return of((String) cs);
    }

    Objects.requireNonNull(cs);
    if (cs.length() == 0) {
      return EMPTY;
    }

    ByteBuffer bb = charset.encode(CharBuffer.wrap(cs));

    // this byte buffer has never escaped so can use its byte array directly
    if (bb.hasArray()) {
      return Bytes.of(bb.array(), bb.position() + bb.arrayOffset(), bb.limit());
    } else {
      byte[] data = new byte[bb.remaining()];
      bb.get(data);
      return new Bytes(data);
    }
  }

  /**
   * Creates a Bytes object by copying the data of the CharSequence and encoding it using UTF-8.
   */
  public static final Bytes of(CharSequence cs) {
    return of(cs, StandardCharsets.UTF_8);
  }

  /**
   * Creates a Bytes object by copying the value of the given String
   */
  public static final Bytes of(String s) {
    Objects.requireNonNull(s);
    if (s.length() == 0) {
      return EMPTY;
    }
    byte[] data = s.getBytes(StandardCharsets.UTF_8);
    return new Bytes(data, s);
  }

  /**
   * Creates a Bytes object by copying the value of the given String with a given charset
   */
  public static final Bytes of(String s, Charset c) {
    if (c == StandardCharsets.UTF_8) {
      return of(s);
    }
    Objects.requireNonNull(s);
    Objects.requireNonNull(c);
    if (s.length() == 0) {
      return EMPTY;
    }
    byte[] data = s.getBytes(c);
    return new Bytes(data);
  }

  /**
   * Checks if this has the passed prefix
   *
   * @param prefix is a Bytes object to compare to this
   * @return true or false
   */
  public boolean beginsWith(Bytes prefix) {
    Objects.requireNonNull(prefix, "beginsWith(Bytes prefix) cannot have null parameter");

    if (prefix.length() > this.length()) {
      return false;
    } else {
      int end = prefix.length();
      for (int i = 0, j = 0; i < end; i++, j++) {
        if (this.data[i] != prefix.data[j]) {
          return false;
        }
      }
    }
    return true;
  }

  /**
   * Check if this has the provided suffix.
   *
   * @param suffix is a Bytes object to compare to this
   * @return true or false
   */
  public boolean endsWith(Bytes suffix) {
    Objects.requireNonNull(suffix, "endsWith(Bytes suffix) cannot have null parameter");
    int suffixLen = suffix.length();
    int len = length();
    if (suffixLen > len) {
      return false;
    }

    // comparing from the back; TODO see if comparing forwards is faster
    for (int i = suffixLen - 1, j = len - 1; i >= 0; i--, j--) {
      if (suffix.data[i] != data[j]) {
        return false;
      }
    }
    return true;
  }

  /**
   * Copy this entire Bytes object into the destination byte array, <code>dest</code>, at position
   * <code>destPos</code>.
   *
   * @param dest destination array into which bytes are copied
   * @param destPos the position in the destination array where the subsequence will be copied
   * @exception IndexOutOfBoundsException if copying would cause access of data outside array
   *            bounds.
   * @exception NullPointerException if either <code>src</code> or <code>dest</code> is
   *            <code>null</code>.
   */
  public void copyTo(byte[] dest, int destPos) {
    copyTo(0, dest, destPos, length());
  }

  /**
   * Copy <code>length</code> bytes from this Bytes object, starting at the <code>begin</code>
   * position into the destination byte array, <code>dest</code>, at position <code>destPos</code>.
   * All bytes between <code>begin</code> and <code>begin+length-1</code>, inclusive, are copied.
   * The destination array must be large enough.
   *
   * @param begin index of the beginning of the subsequence to copy (inclusive)
   * @param dest destination array into which bytes are copied
   * @param destPos the position in the destination array where the subsequence will be copied
   * @param length the length of the sequence to copy
   * @exception IndexOutOfBoundsException if copying would cause access of data outside array
   *            bounds.
   * @exception NullPointerException if either <code>src</code> or <code>dest</code> is
   *            <code>null</code>.
   */
  public void copyTo(int begin, byte[] dest, int destPos, int length) {
    // since dest is byte[], we can't get the ArrayStoreException
    System.arraycopy(data, begin, dest, destPos, length);
  }

}
