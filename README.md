# cls-scala-presentation-play-git
[![Maven Central](https://img.shields.io/maven-central/v/org.combinators/cls-scala-presentation-play-git_2.12.svg)](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22org.combinators%22%20AND%20%22cls-scala-presentation-play-git%22)
[![build status](https://travis-ci.org/combinators/cls-scala-presentation-play-git.svg?branch=master)](https://travis-ci.org/combinators/cls-scala-presentation-play-git)
[![Coverage Status](https://coveralls.io/repos/github/combinators/cls-scala-presentation-play-git/badge.svg?branch=master)](https://coveralls.io/github/combinators/cls-scala-presentation-play-git?branch=master)
[![Join the chat at https://gitter.im/combinators/cls-scala](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/combinators/cls-scala)
## A Play-based web frontend for hosting cls-scala inhabitants via Git

This project complements the Combinatory Logic Synthesizer (CL)S framework by a website to present inhabitants and download them.
Different solutions to the same inhabitation question will be hosted via Git branches.

For more information see our [documentation project](https://combinators.github.io/).

Existing users please refer to the [CHANGELOG](CHANGELOG.md) for news.

## Installation

Add the following dependency to your existing sbt project: 
```scala
libraryDependencies += "org.combinators" %% "cls-scala-presentation-play-git" % "<VERSION>"
```
The string `<VERSION>` has to be replaced by the version you want.
You can search for released versions [here](http://search.maven.org/#search%7Cga%7C1%7Ccls-scala-presentation-play-git).

To obtain the latest unreleased development version, clone the repository and run `sbt publishLocal`.

Currently, Scala 2.11 and 2.12 are supported.

## Examples

Please refer to the [documentation project](https://combinators.github.io/).

## Help and Contributions

Join [combinators/cls-scala](https://gitter.im/combinators/cls-scala) on Gitter.

### Main Authors

- Jan Bessai
- Boris Düdder
- Geroge T. Heineman

### Contributers

-
##### Your name here?
Just the usual: open pull requests and or issues.
Feel free to add yourself to the list in this file, if you contributed something.
