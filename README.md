> Like many Clojure projects, this is stable and doesn't require active maintenance. There might not be recent commits but it is still used and maintained.

# test-refresh

This is a Clojure tool that notices when your source changes and then reloads your code and runs your `clojure.test` tests.
It works with Leiningen and `deps.edn` based projects.

It also works with [expectations](https://github.com/clojure-expectations/expectations) [clojure.test compatible syntax](https://clojure-expectations.github.io/clojure-test.html).

## Features

- Enables quick feedback cycles by automatically refreshing your code and running your tests.
- Runs previously failing tests first, giving you feedback even quicker.
- Built-in test-selector, `:test-refresh/focus`, that lets you narrow the scope of your testing without restarting `test-refresh`. A different selector can be overridden through configuration. See the sample project.clj or documentation in this README for more details.
- Optionally only automatically runs tests in changed namespaces.
- Can pass result of running your tests to a notification command of your choice.
- Has built in Growl notification support.
- Can be configured to only notify you on failures.
- Times how long it takes to run your tests.
- Can optionally suppress `clojure.test`'s _Testing namespace_ output. This is extremely useful in making test output with larger codebases readable again.
- You can hit `enter` in terminal to force tests to rerun.
- Supports `clojure.test`'s custom reports.
- Supports running your tests once! Useful for taking advantage of custom test reporters or quiet output in CI systems.
- Has optional repl support for changing global state, such as timbre logging levels
- Detects if your project uses [circleci.test](https://github.com/circleci/circleci.test) and uses that instead of clojure.test.
- **Leiningen only**: [Supports](https://github.com/jakemcc/test-refresh/blob/master/CHANGES.md#040) subset of Leiningen test selectors.
- Supports clearing output between test runs, see [PR](https://github.com/jakemcc/test-refresh/pull/91) and sample configuration files for details.
- Supports [custom banner](https://github.com/jakemcc/test-refresh/pull/91) printed between test runs. See sample configuration files.
- Supports reloading and not running tests. See [PR](https://github.com/jakemcc/test-refresh/pull/91) motivation.

[sample.project.clj](sample.project.clj) show optional Leininen configuration.
[example.test-refresh.edn](example.test-refresh.edn) shows configuration options available to `deps.edn` based projects.

The sample configuration files and the rest of this documentation show how `test-refresh` can be used.

## Usage

### Leiningen based projects

See [docs/leiningen.md](docs/leiningen.md).

### deps.edn based projects

See [docs/deps_edn.md](docs/deps_edn.md).

## Features

> Any command line example here that is `lein test-refresh :some-argument` is **only** supported by Leiningen.
> deps.edn usage must be configured through `.test-refresh.edn` files.

### Hit `Enter` to rerun tests

If you need to rerun your tests without changing a file then hit `Enter` when focused on a running `test-refresh`.
This behavior will stop if test-refresh thinks it has read till the end of STDIN.
This is usually caused by hitting ctrl-d, but also when a specific version of bash is invoked by test-refresh which can happen when running tests or notify commands.

### Built-in test narrowing (test selector)

Have you ever been running all of your tests and then want to only focus on one? 
Instead of commenting out the others or quitting test-refresh and restarting with a test-selector you can add `:test-refresh/focused true` to your test(s) or namespace.

With the below code, only `test-addition` will run until the `:test-refresh/focus` marker is removed from it.

```clojure
(deftest ^:test-refresh/focus test-addition
  (is (= 2 (+ 1 1))))

(deftest test-subtraction
  (is (= 0 (- 10 9 1))))
```

You can optionally specify a shorter flag by adding `:focus-flag :your-flag` to test-refresh's configuration.

## Configuration Features

A [sample.project.clj](sample.project.clj) contains the definitive example of configuring `test-refresh` features.
Configuration can appear in any file that Leiningen uses to merge into your project's configuration when running commands.
Often `test-refresh` configuration is a personal preference and should be configured in your personal `~/.lein/profiles.clj`.

### Notifications

`test-refresh` supports specifying a notification command.
This command is passed a short message after your tests have run.
This command is configured through your `project.clj` or `profiles.clj`.
For example, if you want to send OSX notifications using [terminal-notifier](https://github.com/alloy/terminal-notifier) then you would add the following to your `project.clj` or `profiles.clj`

```clojure
:test-refresh {:notify-command ["terminal-notifier" "-title" "Tests" "-message"]}
```

`test-refresh` also has built-in Growl support.
To receive Growl notifications run `lein test-refresh :growl`.
This has been tested with modern (well, asof 2016) versions of Growl for [OS X](http://growl.info/), [Linux](http://mattn.github.com/growl-for-linux/), and [Windows](http://growlforwindows.com/). 
You can also always set this to true by setting `:test-refresh {:growl true}}`.
An example can be found in the [sample project.clj](sample.project.clj).

`:notify-on-success` is another available option.
It can be used to turn off notifications when your tests are successful.
Set `:notify-on-success false` to turn off success notifications. 
An example can be found in the [sample project.clj](sample.project.clj).

### Reduced terminal output

`test-refresh` can be configured to suppress `clojure.test`'s _Testing namespace_ output.
Add `:quiet true` to your `:test-refresh` configuration map to suppress `clojure.test`'s noisy output.
This is particularly useful on codebases with a large number of test namespaces.

### Only run changes in changed namespaces.

`test-refresh` can be configured to only automatically run tests in changed namespaces.
This can be used to get even faster feedback since only tests where something has changed will be run. 
You can toggle this mode by adding a `:changes-only true` entry in your `:test-refresh` configuration or by passing it as a command line option `lein test-refresh :changes-only`.

If you are in this mode and want to run all your tests you can trigger them by hitting `enter` in the terminal where `test-refresh` is running.

### Custom Clojure.test report

`test-refresh` can be configured to use a custom `clojure.test` output report. 
Add `:report myreport.namespace/myreport` to your `:test-refresh` configuration map to use your own reporter for `clojure.test`'s output.
An example can be found in the [sample project.clj](sample.project.clj).

### Running your tests once

At first this seems like a weird feature for a refreshing test runner to support but because of the other features `test-refresh` supports, such as custom test runners, being able to just run tests once can be useful.
See [this](https://github.com/jakemcc/test-refresh/pull/48) pull request for discussion.

You can either configure this option in your project.clj (or profiles.clj) or pass it as a command line option.
Check out `sample.project.clj` for an example of project configuration.

Using it at the command line looks like `lein test-refresh :run-once`.

### Running with a REPL

`test-refresh` can be run with `:with-repl` which will start up a repl
that you can interact with in between test runs.
The main reason for this option is that sometimes you want to affect global state in your application.
An example is when you see a test failure, you can call `(taoensso.timbre/set-level! :debug)` and see more information.

See [this](https://github.com/jakemcc/test-refresh/pull/50) pull request for details.

### Running in a REPL

`test-refresh` supports running in a repl.
This was done to support running in Cursive's repl so [users could click](https://github.com/jakemcc/test-refresh/issues/80) and navigate to source.

To use this feature, add `test-refresh` as a project dependency instead of as a plugin.
Then open your repl and do the following

```clojure
user=> (require 'com.jakemccrary.test-refresh)
nil
user=> (com.jakemccrary.test-refresh/run-in-repl "test")
*********************************************
*************** Running tests ***************
```

The tests will run until you kill the evaluation with ctrl-c. 

This feature is one I never use myself.
I'd consider it experimental.
In my very limited testing I've had some weird behavior where I couldn't get it to stop running.
I'm putting it out there though so others can use it.

## Contributing

I encourage pull requests.
If you're making a large change it is probably a good idea to create an issue and run it by me first.
If you open a pull request you should expect me to review the code and potentially suggest improvements.

Working on `test-refresh` can be a bit tricky.
Despite being a tool to enhance testing it has very few tests itself.
As a result its sort of a pain to work on.
My typical work flow is outlined below.
I encourage you to do the following as well (or better yet, add some useful tests!).

1. Open two terminals, one in the `./test-refresh` directory and one in `./lein2`.
1. In `./test-refresh` run `lein install` to put a version built from your local `test-refresh` checkout into your `~/.m2` directory.
1. The project in `./lein2` is setup to use whatever version is specified in `./test-refresh/project.clj`. As a result it will use the recently `lein install`ed version from the above step. Use the project in `./lein2` to test out your local version of `test-refresh`. Toggle settings in `./lein/project.clj` to test various features. Make tests fail and pass.
1. Make your changes to the project in `./test-refresh` and `lein install`.
1. Repeat manual testing in `./lein2`. Add sample code or configuration to `./lein2` project to show your changes.

Its a bit painful but it works.
If there were more active changes happening to the project I'd invest the time to figure out how to test it but given the stability of `test-refresh` I haven't bothered.
They would be a welcome addition.

## Latest version & Change log

The latest version is the highest non-snapshot version found in [CHANGES.md](CHANGES.md) or whatever the below images says (sometimes image doesn't seem to load).

### Leiningen

![Latest version](https://clojars.org/com.jakemccrary/lein-test-refresh/latest-version.svg)

### deps.edn

![Latest version](https://clojars.org/com.jakemccrary/test-refresh/latest-version.svg)

## Compatibility

lein-test-refresh has been tested to work with Clojure 1.5.1, 1.6, and
1.7, 1.8, 1.9 with Leiningen 2.3+.

Because of [tools.namespace](https://github.com/clojure/tools.namespace) changes `test-refresh` requires that your project use Clojure >= 1.3.0.
If your project also depends on a version of `tools.namespace` < 0.2.1 you may see occasional exceptions.

## Leiningen 1.0

If you are using Leiningen 1 this project is definitely broken starting on lein-test-refresh version 0.21.0.
Try using 0.20.0 or earlier.
These versions were not explicitly tested with Leiningen 1 but they probably work.
Leiningen 1 is not officially supported.

## License

Copyright (C) [Jake McCrary](https://jakemccrary.com)

Distributed under the Eclipse Public License, the same as Clojure.
