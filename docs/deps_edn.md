# deps.edn based projects

> **This is a feature that the author, Jake McCrary, hasn't used much. Please report back any issues and feedback as Github issues or through email.**

![Latest version](https://clojars.org/com.jakemccrary/test-refresh/latest-version.svg)

If you want to use `test-refresh` with a `deps.edn` based project then add it to your `:aliases` section like below but using the version specified above.

```
{:paths ["src"]
 :deps {org.clojure/clojure {:mvn/version "1.10.3"}}
 :aliases
 {:test-refresh {:extra-paths ["test"]
                 :extra-deps {com.jakemccrary/test-refresh
                              {:mvn/version "0.25.0"}}
                 :main-opts ["-m" "com.jakemccrary.test-refresh"]}}}
```

Run with `clojure -M:test-refresh`


```
$ clojure -M:test-refresh
*********************************************
*************** Running tests ***************
:reloading (hello-test)

<standard clojure.test output>

Failed 1 of 1 assertions
Finished at 19:05:30.927 (run time: 0.034s)
```

`test-refresh` will notice when code changes and then reload and rerun your tests.
When there is a change your code is tested again.

Configuration for `test-refresh` in deps.edn files **must** be specified through `.test-refresh.edn` files.
By default `~/.test-refresh.edn` and `$PWD/.test-refresh.edn` files are loaded, with the project specific file overriding settings from the `~/.test-refresh.edn` file.

You can also specify an alternative configuration file at the command line `clojure -M:test-refresh -c config-file.edn`

By default, `test-refresh` looks for tests under the `test` directory.
You can use the `-d` flag to specify a different directory.

`clojure -M:test-refresh --help` will display a helpful message with descriptions of supported command line flags.

Here is an [example.test-refresh.edn](example.test-refresh.edn).

