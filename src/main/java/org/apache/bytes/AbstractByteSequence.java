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

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.PrimitiveIterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.StreamSupport;

/**
 * Abstract class for implementations of {@link ByteSequence}.
 *
 * @since 1.0.0
 */
abstract class AbstractByteSequence implements ByteSequence {

  private void checkBounds(int i, String positionName) {
    int len = length();
    if (i < 0 || i >= len) {
      String msg = positionName + " not valid for ";
      msg += len == 0 ? "empty Bytes" : "range [0," + len + ")";
      msg += ": " + i;
      throw new IndexOutOfBoundsException(msg);
    }
  }

  protected void checkBounds(int i) {
    checkBounds(i, "Index");
  }

  protected void checkBounds(int begin, int end) {
    if (begin > end) {
      throw new IndexOutOfBoundsException(
          "End position (" + end + ") occurs before begin position (" + begin + ")");
    }
    checkBounds(begin, "Begin position");
    checkBounds(begin, "End position");
  }

  @Override
  public IntStream intStream() {
    class ByteIterator implements PrimitiveIterator.OfInt {
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur < length();
      }

      @Override
      public int nextInt() {
        if (hasNext()) {
          return byteAt(cur++); // upcast to int
        } else {
          throw new NoSuchElementException();
        }
      }

      @Override
      public void forEachRemaining(IntConsumer block) {
        for (; cur < length(); cur++) {
          block.accept(byteAt(cur));
        }
      }

    }

    return StreamSupport.intStream(
        () -> Spliterators.spliterator(new ByteIterator(), length(), Spliterator.ORDERED),
        Spliterator.SUBSIZED | Spliterator.SIZED | Spliterator.ORDERED, false);
  }

  @Override
  public Iterator<Byte> iterator() {
    return new Iterator<Byte>() {
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur < length();
      }

      @Override
      public Byte next() {
        if (hasNext()) {
          return byteAt(cur++); // auto-boxing here
        } else {
          throw new NoSuchElementException();
        }
      }

    };
  }

  @Override
  public Spliterator<Byte> spliterator() {
    return Spliterators.spliterator(iterator(), length(), Spliterator.ORDERED);
  }

}
