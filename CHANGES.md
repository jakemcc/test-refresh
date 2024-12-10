# Changes

## 0.26.0

- Adds support for clearing output
- Adds support for reloading but not running any tests
- Adds support for specifying your own custom banner.

See https://github.com/jakemcc/test-refresh/pull/91 for details

## 0.25.0

Breaks project into two, `com.jakemccrary/test-refresh` and `com.jakemccrary/lein-test-refresh`. As the names suggest, `lein-test-refresh` continues being the Leiningen plugin while `test-refresh` contains the core functionalty. This starts supporting `deps.edn` based projects.

- Upgrades `org.clojure/tools.namespace` dependency to `1.1.0`.
- Introduces dependencies on `org.clojure/tools.cli`.

## 0.24.1

- Fixes bug that caused `^:test-refresh/focus` to no longer focus.

## 0.24.0

- **BREAKING** No longer quits when reaching the end of STDIN
  stream. This will cause behavior of ctrl-d behavior to change (it
  will no longer quit). This was done to work around STDIN closing if
  test-refresh invoked an external process that causes specific
  version of bash to be invoked.
- Allow default focus flag, `:test-refresh/focus`, to be overridden by
  specifying `:focus-flag` key value pair in test-refresh configuration.
- More details are printed to screen when there is an exception
  reloading your code.
- Added `com.jakemccrary.test-refresh/run-in-repl`. This function is
  intended to be called from Cursive's repl so that stacktraces become
  clickable. See issue
  [#80](https://github.com/jakemcc/lein-test-refresh/issues/80). Details
  below.

To call `com.jakemccrary.test-refresh/run-in-repl` from a repl, add `lein-test-refresh` as a dependency to your project. Start a repl and then do the following. `run-in-repl` takes one or more paths to your test directories.

```
user> (require 'com.jakemccrary.test-refresh)
user> (com.jakemccrary.test-refresh/run-in-repl "test")
TEST OUTPUT HERE
```

Hitting CTRL-C stops the tests. This repl is otherwise not useful. Any project.clj or profiles.clj settings are not known by `test-refresh` and as a result are not honored.


## 0.23.0

- Adds support for impromptu test selecting by adding
  `:test-refresh/focus true` as metadata onto test or namespace. Vars
  with this metadata on it are given priority and test-refresh will
  only run them until all `:test-refresh/focus` markers are removed
  from your project. Example below.

```clojure
(deftest ^:test-refresh/focus test-addition
  (is (= 2 (+ 1 1))))
```

## 0.22.0

- **EXPERIMENTAL** Detects if circleci.test runner is in project and uses that instead of clojure.test

## 0.21.1

- Preemptively adds support for Leiningen 2.8.0 support
  for
  [string as dependency name](https://github.com/technomancy/leiningen/blob/master/NEWS.md#280--).

## 0.21.0

- Drops support for Leiningen 1. Leiningen 1 users can use version `0.20.0`
- Fixes issue with Leiningen's managed dependencies from [issue #60](https://github.com/jakemcc/lein-test-refresh/issues/69).

## 0.20.0

- Merges in Leiningen's `:leiningen/test` profile before running
  tests. Addresses
  [PR #67](https://github.com/jakemcc/lein-test-refresh/pull/67).

## 0.19.0

- `lein test-refresh :run-once` exits with a non-zero (1) exit code when tests fail.

## 0.18.1

- Fixes bug introduced by previous change that resulted in a stackoverflow exception.

## 0.18.0

- Adds optional `:stack-trace-depth` flag to configuration. Value is bound to `clojure.test/*stack-trace-depth*`.

## 0.17.0

- Adds optional `:refresh-dirs` flag. The value is a sequence of
  directories that should be refreshed when a refresh is
  triggered. (Value is passed through to
  clojure.tools.namespace.repl/set-refresh-dirs).

## 0.16.0

- Adds optional `:watch-dirs` flag to specify what directories to monitor for
  changes.

## 0.15.0

- Better handles when `org.clojure/tools.namespace` throws an
  exception parsing a namespace declaration.

## 0.14.0

- Can now use the `:with-repl` flag to start a REPL for interacting
  with your project. More details in README.md and
  [this](https://github.com/jakemcc/lein-test-refresh/pull/50) pull
  request.

## 0.13.0

- Adds support for running tests once with the flag `:run-once`.

## 0.12.0

- Adds support for only running tests in changed namespaces by setting
  the `:changes-only` flag in your configuration or at the command
  line. See [sample.project.clj](sample.project.clj) for details.

## 0.11.0

- Adds support for specifying a custom test reporter. See
  sample.project.clj and `lein2` directory for example.

## 0.10.0

- Upgrades `org.clojure/tools.namespace` to 0.2.11. This improves cljc support.

## 0.9.0

- `lein-test-refresh` can suppress `clojure.test`'s _Testing
namespace_ output. Very useful on projects with a large number of test
namespaces for keeping the terminal useful.

## 0.8.0

- Upgrade `org.clojure/tools.namespace` dependency to 0.2.10.

## 0.7.0

- `lein-test-refresh` releases no longer have a Clojure dependency.
- No longer explicitly adds tools.namespace dependency.

## 0.6.0

- Fixtures associated with tests in ignore namespaces no longer run.
  Thanks [lbradstreet](https://github.com/jakemcc/lein-test-refresh/pull/24)!

## 0.5.5

- Upgrade to `org.clojure/tools.namespace` 0.2.8.

## 0.5.4

- Upgrade to `org.clojure/tools.namespace` 0.2.7.

## 0.5.3

- `lein test-refresh` now includes the `:test` profile. (same behavior
as `lein test`)

## 0.5.2

- Upgrade to `org.clojure/tools.namespace` 0.2.6.
- Upgrade to `leinjacker` 0.4.2

## 0.5.1

- test-refresh quits when ctrl+d pressed.

## 0.5.0

- Failed tests are run first. If they pass then all of your tests are run.

## 0.4.2

- Prints how long tests took to run to console.

## 0.4.1

- Fixes bug where `lein-test-refresh` continuously polled filesystem
for changes.

## 0.4.0

- First cut of supporting a subset of leiningen test-selectors.

Pretend you have a `project.clj` that looks like the one below.

    (defproject example "0.1.0-SNAPSHOT"
      :dependencies [[org.clojure/clojure "1.5.1"]]
      :plugins [[com.jakemccrary/lein-test-refresh "0.4.0"]]
      :test-selectors {:default (constantly nil)
                       :integration :integration
                       :unit (complement :integration)
                       :fast (complement :slow)})

If you run `lein test-refresh` then the `:default` entry the value of
`:test-selectors` will be used. In this example that would cause no
tests to run. If you run `lein test-refresh :integration` it will only
run tests that have a truthy value for `:integration` in either the
test's metadata or in the metadata of the containing namespace.

This version does not support either `lein test :only namespace` or `lein
test namespace`.

## 0.3.10

- Can specify `:test-refresh {:notify-on-success false}` in `project.clj` or
`.lein/profiles.clj` to disable notifications and growl alerts if all tests pass.

## 0.3.9

- Can specify `:test-refresh {:growl true}` in `project.clj` or
`.lein/profiles.clj` to turn on growl notifications.

## 0.3.8

- Nothing. This was an accidental release due to testing the release
  script.

## 0.3.7

- Change success message to 'Passed all tests'

## 0.3.6

- Bug fix for language around number of failing assertions.

## 0.3.4

- If `:test-refresh {:notify-command ["command" "arguments"]}` is in
  your project.clj then a summary message is passed into that command.

## 0.3.1 - 0.3.3

Recommend that you don't use. Versions that were published but within
a few minutes realized mistakes were made.

## 0.3.0

- Hitting enter causes tests to be run.

## 0.2.2

- Adds support for reporting test results using Growl. Use `lein
autoexpect :growl`
- Upgrade to `org.clojure/tools.namespace 0.2.1`. This version of
  tools.namespace provides better backwards compatibility with
  versions prior to 0.2.0.
