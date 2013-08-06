# autoexpect

Leiningen plug-in for automatically running [expectations](https://github.com/jaycfields/expectations) whenever your Clojure project's source changes.

## Usage

Here is what using it looks like. 

    $ lein autoexpect
    *********************************************
    *************** Running tests ***************
    Ran 3 tests containing 3 assertions in 16 msecs
    0 failures, 0 errors.

Your terminal will just stay like that. Every half second autoexpect
polls the file system to see if anything has changed. When there is a
change your code is tested again.

If you want to receive notifications using growl, then run `lein
autoexpect :growl`. This has been tested with modern versions of Growl
for [OS X](http://growl.info/),
[Linux](http://mattn.github.com/growl-for-linux/), and
[Windows](http://growlforwindows.com/).


### Leiningen 2.0

Add `[lein-autoexpect "1.0"]` to your `~/.lein/profiles.clj` as
follows:

    {:user {:plugins [[lein-autoexpect "1.0"]]}}
    
Alternatively add to your `:plugins` vector in your project.clj file.
   
    (defproject sample
      :dependencies [[org.clojure/clojure "1.5.1"]]
      :profile {:dev {:dependencies [[expectations "1.4.52"]]}}
      :plugins [[lein-autoexpect "1.0"]])

### Leiningen 1.7 and older

Add `[lein-autoexpect "1.0"]` to your `project.clj` file under `:dev-dependencies` or install as a Leiningen plugin (`lein plugin install lein-autoexpect 1.0`). Run using `lein autoexpect`


## Compatibility

autoexpect should work with any version of expectations. If there is
an issue please report it. I've tested it with versions 1.1.0, 1.3.[023678], and 1.4.*.

Because of
[tools.namespace](https://github.com/clojure/tools.namespace) changes
`lein-autoexpect` requires that your project use Clojure >= 1.3.0. If
your project also depends on a version of `tools.namespace` < 0.2.1
you may see occasional exceptions.

## License

Copyright (C) 2011-2012 [Jake McCrary](http://jakemccrary.com)

Distributed under the Eclipse Public License, the same as Clojure.
