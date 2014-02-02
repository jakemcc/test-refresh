# Changes

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
