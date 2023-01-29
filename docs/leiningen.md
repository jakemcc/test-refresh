# Leiningen based projects

[![Latest version](https://clojars.org/com.jakemccrary/lein-test-refresh/latest-version.svg)](https://clojars.org/com.jakemccrary/lein-test-refresh)

Add the above to your `~/.lein/profiles.clj`. It should look similar to below.

```clojure
{:user {:plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]]}}
```

Alternatively you may add it to your `project.clj`.

```clojure
(defproject sample
  :dependencies [[org.clojure/clojure "1.8.0"]]
  :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.25.0"]]}})
```

> In my personal setup I also include
> [humane-test-output](https://github.com/pjstadig/humane-test-output)
> which changes clojure.test's output to be more readable. Other users
> include [ultra](https://github.com/venantius/ultra) instead which
> does even more to the output (color, prettifying exceptions and
> diffs, etc).

Enter your project's root directory and run `lein test-refresh`.
The output will look something like this.

    $ lein test-refresh
    *********************************************
    *************** Running tests ***************

    <standard clojure.test output>

    Failed 1 of 215 assertions
    Finished at 08:25:20.619 (run time: 9.691s)

Your terminal will just stay like that.
Whenever there is a code change, `test-refresh` will reload your code and rerun your tests.
