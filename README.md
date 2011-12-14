# autoexpect

Leiningen plug-in for automatically running [expectations](https://github.com/jaycfields/expectations) whenever your Clojure project's source changes.

## Usage

In your `project.clj` file add `[lein-autoexpect "0.0.2"]` to your dependencies. At the command line run `lein autoexpect` to start the continuous testing.

Here is what it looks like. 

    $ lein autoexpect
    *********************************************
    *************** Running tests ***************
    Ran 3 tests containing 3 assertions in 16 msecs
    0 failures, 0 errors.

Your terminal will just stay like that. Every half a second autoexpect polls the file system to see if anything has changed.

autoexpect has been tested with `expectations 1.1.0` and `expectations 1.3.0`. It is up to you to specify what version of expectations you are using in your `project.clj` file.

## License

Copyright (C) 2011 [Jake McCrary](http://jakemccrary.com)

Distributed under the Eclipse Public License, the same as Clojure.
