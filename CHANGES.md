# Changes

## 0.5.0-SNAPSHOT

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
