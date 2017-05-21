## Skinny Framework Project Contributors' Guide

### Issues

- Questions should be posted to the [Google Users Group](https://groups.google.com/forum/#!forum/skinny-framework) or http://stackoverflow.com/
- Please describe your issue in detail (version, situation, examples)
- We may close your issue if we have no plan to take action on it. We appreciate your understanding.

### Pull Requests

- Pull requests should be sent to the "master" branch
- Source and binary compatibility must always be kept
- scalafmt must be applied to all Scala source code by running `sbt scalafmt`
- Prefer creating separate Scala source files for each class/object/trait (except, of course, for sealed traits)

#### Testing your pull request

All pull requests should pass the Travis CI jobs before they can be merged:

https://travis-ci.org/skinny-framework/skinny-framework

