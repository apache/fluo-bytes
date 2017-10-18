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

import java.util.stream.IntStream;

/**
 * Interface representing a sequence of bytes.
 *
 * @since 1.0.0
 */
public interface ByteSequence extends Iterable<Byte> {

  /**
   * The length of the sequence this object represents. It does not necessarily reflect the size of
   * any internal data structures, such as an internal byte array.
   *
   * @return the length of the sequence
   */
  int length();

  /**
   * Retrieve a byte at the specified index. Valid indices are between <code>0</code> (for the first
   * byte) and <code>length() - 1</code> for the last byte.
   *
   * @param index the position within the sequence to retrieve
   * @return the byte at the specified index
   */
  byte byteAt(int index);

  /**
   * Retrieve a sequence of bytes from the original sequence. The returned sequence includes all
   * bytes between <code>begin</code> and <code>end - 1</code>, inclusive.
   *
   * @param begin the index of the first byte to be included in the result
   * @param end the index after the last byte to be included in the result
   * @return a byte sequence containing the bytes between <code>begin</code> and
   *         <code>end - 1</code>, inclusive
   */
  ByteSequence subSequence(int begin, int end);

  /**
   * Return an IntStream representation of this byte sequence. This avoids auto-boxing when not
   * necessary. Each int represents a single byte from the sequence.
   *
   * @return a stream of integers, one for each byte in the sequence
   */
  IntStream intStream();

  /**
   * Compares this sequence with the provided byte array using a lexicographical comparison.
   *
   * @param bytes the byte array with which to compare this sequence
   * @return a value following the same conventions as {@link Comparable#compareTo(Object)}
   */
  int compareTo(byte[] bytes);

  /**
   * Determines if the contents of this byte sequence is equivalent to the content of the provided
   * byte array.
   *
   * @param bytes the byte array with which to compare this sequence
   * @return true if the bytes they represent are the same
   */
  boolean contentEquals(byte[] bytes);
}
