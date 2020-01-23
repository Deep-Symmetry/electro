# Change Log

All notable changes to this project will be documented in this file.
This change log follows the conventions of
[keepachangelog.com](http://keepachangelog.com/).

## [Unreleased][unreleased]

### Added
- Assigned a stable automatic module name so this project can safely
  be used as a dependency in modular Java projects.
- Updated JavaDoc builder version to include search field.

## [0.1.3] - 2018-09-04

### Fixed

- Protect against loss of floating point precision by always rounding
  to long values _before_ performing arithmetic with system timestamp
  values (since those are huge numbers of milliseconds).

### Added

- Some new utility methods to measure the distance in time to the
  nearest beat, bar, and phrase boundaries.

## [0.1.2] - 2018-09-03

### Fixed

- Use double-precision floating point values for beat, bar, and phrase
  intervals, rather than long integers. This was a bad mistake in the
  port from Clojure, which was using precise rational values for them.
  This was leading to inconsistent beat calculations, which was causing
  sync problems on the Pioneer network.

## [0.1.1] - 2018-09-02

### Added

- Support for enhanced phases (oscillating marker phases at fractions
  and/or multiples of their normal speed), now that I have found a way
  to convert floating point numbers to ratios in Java.

## 0.1.0 - 2018-08-08

### Added

- Created as a new project.

[unreleased]: https://github.com/Deep-Symmetry/electro/compare/v0.1.3...HEAD
[0.1.3]: https://github.com/Deep-Symmetry/electro/compare/v0.1.2...v0.1.3
[0.1.2]: https://github.com/Deep-Symmetry/electro/compare/v0.1.1...v0.1.2
[0.1.1]: https://github.com/Deep-Symmetry/electro/compare/v0.1.0...v0.1.1
