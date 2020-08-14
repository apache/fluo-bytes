<!--
  Licensed to the Apache Software Foundation (ASF) under one
  or more contributor license agreements.  See the NOTICE file
  distributed with this work for additional information
  regarding copyright ownership.  The ASF licenses this file
  to you under the Apache License, Version 2.0 (the
  "License"); you may not use this file except in compliance
  with the License.  You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing,
  software distributed under the License is distributed on an
  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  KIND, either express or implied.  See the License for the
  specific language governing permissions and limitations
  under the License.
-->

# Apache Fluo Bytes

[![Build Status][ti]][tl] [![Apache License][li]][ll] [![Maven Central][mi]][ml] [![Javadoc][ji]][jl]

**Apache Fluo Bytes is a simple library with the goal of providing an extremely
stable API for handling bytes, suitable for use in [Apache Fluo][fluo] and
other projects' APIs.**

## Features and Goals

This project aims to fill a void in Java, by providing convenient objects to
represent a sequence of bytes and associated utility classes for situations
when a raw byte array is not appropriate.

Specifically, it provides a `ByteSequence` interface, analogous to Java's
`CharSequence`, an immutable `Bytes` implementation analogous to Java's
`String`, and a corresponding `BytesBuilder` analogous to Java's
`StringBuilder`.

The provided classes have appropriate methods for serialization, and proper
equals and hashCode implementations, as well as a comparator for
`ByteSequence`, so they will be suitable for use in `Set`s and as keys in
`Map`s.

An immutable bytes implementation makes it possible to pass data between APIs
without the need for performance-killing protective copies. This benefit is
compounded if this library is used by multiple projects, as the need to make
protective copies while passing data between a project and its dependency's API
is eliminated.

This project aims to provide a fluent and intuitive API, with support for
conversions to/from other common types, such as `ByteBuffer`, `byte[]`, and
`CharSequence`/`String`.

This project requires at least Java 8, and supports `Stream` and functional
APIs where appropriate.

See this [blog post][blog] for some additional background.

## Safe for APIs

Using an external library in a project's API poses some risks to that project,
especially if it and its dependencies depend on different versions of that
library. This project attempts to mitigate those risks, so that it can be used
safely by other projects.

This project is made safe for reuse in other projects' APIs by adopting the
following principles:

* Using [Semantic Versioning 2.0.0][semver] to make strong declarations about
  backwards-compatibility
* Strongly avoid breaking changes (avoid major version bumps), so that projects
  can converge on the latest version of this library required by their code and
  that of their dependencies, without risk of incompatibility
* No runtime dependencies itself, to eliminate any potential conflicts from
  transitive dependencies if this library is used
* Use generic package naming scheme not tied to the Maven coordinates, in case
  the project relocates or becomes independent of Fluo in the future
* Practice review-then-commit during development to protect against poor
  initial design which is stuck with the project for the long-term
* Provide `@since` tags in Javadocs to communicate minimum required versions
  for particular features

## Public API declaration for Semantic Versioning

This project's public API are all the publicly visible classes, methods, and
fields accessible outside the project's packages.

---
*Apache Fluo Bytes is an [Apache Fluo][fluo] project.*

[blog]: https://fluo.apache.org/blog/2016/11/10/immutable-bytes/
[semver]: http://semver.org/spec/v2.0.0.html
[fluo]: https://fluo.apache.org/
[ti]: https://travis-ci.org/apache/fluo-bytes.svg?branch=main
[tl]: https://travis-ci.org/apache/fluo-bytes
[li]: http://img.shields.io/badge/license-ASL-blue.svg
[ll]: https://github.com/apache/fluo-bytes/blob/main/LICENSE
[mi]: https://maven-badges.herokuapp.com/maven-central/org.apache.fluo/fluo-bytes/badge.svg
[ml]: https://maven-badges.herokuapp.com/maven-central/org.apache.fluo/fluo-bytes/
[ji]: https://javadoc-emblem.rhcloud.com/doc/org.apache.fluo/fluo-bytes/badge.svg
[jl]: http://www.javadoc.io/doc/org.apache.fluo/fluo-bytes

