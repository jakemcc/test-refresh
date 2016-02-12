# Changes

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
