# electro

[![project chat](https://img.shields.io/badge/chat-on%20zulip-brightgreen)](https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger)
 <img align="right" width="275" height="250" alt="Beat Link" src="assets/BeatLink-logo-padded-left.png">

A Java library to help work with musical time. Based on the
[rhythm](https://github.com/Deep-Symmetry/afterglow/blob/master/src/afterglow/rhythm.clj)
namespace in
[Afterglow](https://github.com/Deep-Symmetry/afterglow#afterglow), ported
to plain Java to give these capabilities to [Beat
Link](https://github.com/Deep-Symmetry/beat-link#beat-link) without
requiring it to embed the Clojure ecosystem.

[![License](https://img.shields.io/github/license/Deep-Symmetry/electro?color=blue)](#license)

## Installation

Electro is available through Maven Central, so to use it in your Maven
project, all you need  is to include the appropriate dependency.

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.deepsymmetry/electro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.deepsymmetry/electro)

Click the **maven central** badge above to view the repository entry
for electro. The proper format for including the latest release as a
dependency in a variety of tools can be found in the **Dependency
Information** section. If you want these kinds of capabilities from
Clojure, you are better off depending on
[Afterglow via Clojars](https://clojars.org/afterglow) and using its
Clojure-oriented `rhythm` namespace, linked at the top of the page.

If you want to manually install electro, you can download the library
from the [releases](https://github.com/Deep-Symmetry/electro/releases)
page and put it on your project&rsquo;s class path.

## Usage

See the [API Documentation](http://deepsymmetry.org/electro/apidocs/)
for full details.

## Getting Help

<a href="http://zulip.com"><img align="right" alt="Zulip logo"
 src="assets/zulip-icon-circle.svg" width="128" height="128"></a>

Deep Symmetry&rsquo;s projects are generously sponsored with hosting
by <a href="https://zulip.com">Zulip</a>, an open-source modern team
chat app designed to keep both live and asynchronous conversations
organized. Thanks to them, you can <a
href="https://deep-symmetry.zulipchat.com/#narrow/stream/275322-beat-link-trigger">chat
with our community</a>, ask questions, get inspiration, and share your
own ideas.

## License

<a href="http://deepsymmetry.org"><img align="right" alt="Deep Symmetry"
 src="assets/DS-logo-github.png" width="250" height="150"></a>

Copyright © 2018–2022 [Deep Symmetry, LLC](http://deepsymmetry.org)

Distributed under the [Eclipse Public License
2.0](https://opensource.org/licenses/EPL-2.0). By using this software
in any fashion, you are agreeing to be bound by the terms of this
license. You must not remove this notice, or any other, from this
software. A copy of the license can be found in
[LICENSE.md](LICENSE.md) within this project.
