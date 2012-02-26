# autoexpect

Leiningen plug-in for automatically running [expectations](https://github.com/jaycfields/expectations) whenever your Clojure project's source changes.

## Usage

Either add `[lein-autoexpect "0.0.3"]` to your `project.clj` file under `:dev-dependencies` or install as a Leiningen plugin `lein plugin install lein-autoexpect 0.0.3`. Run using `lein autoexpect`

Here is what it looks like. 

    $ lein autoexpect
    *********************************************
    *************** Running tests ***************
    Ran 3 tests containing 3 assertions in 16 msecs
    0 failures, 0 errors.

Your terminal will just stay like that. Every half a second autoexpect polls the file system to see if anything has changed. When there is a change your code is tested again.

autoexpect should work with any version of expectations. It has been tested with 1.1.0, 1.3.0, and 1.3.2. It does not currently support Leiningen 2.0.

## License

Copyright (C) 2011-2012 [Jake McCrary](http://jakemccrary.com)

Distributed under the Eclipse Public License, the same as Clojure.
