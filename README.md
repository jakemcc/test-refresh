# lein-test-refresh

Leiningen plug-in for automatically running `clojure.test` tests whenever your Clojure project's source changes.

## Usage

Here is what using it looks like. 

    $ lein test-refresh
    *********************************************
    *************** Running tests ***************
    (standard clojure.test output here)

Your terminal will just stay like that. Fairly often `lein-test-refresh`
polls the file system to see if anything has changed. When there is a
change your code is tested again.

If you want to receive notifications using growl then run `lein
test-refresh :growl`. This has been tested with modern versions of Growl
for [OS X](http://growl.info/),
[Linux](http://mattn.github.com/growl-for-linux/), and
[Windows](http://growlforwindows.com/).

If you want to receive notifications using some other notification
program then you can add `:test-refresh {:notify-command
["command" "arg1"]}` to your `project.clj` file. `lein-test-refresh`
will pass a short message to the command specified indicating success
or failure. There is a sample [project.clj](sample.project.clj) file
which specifies this feature.

`lein-test-refresh` also will run your tests if you hit the enter key.

### Latest version & Change log

The latest version is the highest non-snapshot version found in
[CHANGES.md](CHANGES.md).

### Leiningen 2.0

Add whatever is shown above to your to your `~/.lein/profiles.clj`. An example using version `0.4.1` is shown below.

    {:user {:plugins [[com.jakemccrary/lein-test-refresh "0.4.1"]]}}
    
Alternatively add to your `:plugins` vector in your project.clj file.
   
    (defproject sample
      :dependencies [[org.clojure/clojure "1.5.1"]]
      :profiles {:dev {:plugins [[com.jakemccrary/lein-test-refresh "0.4.1"]]}})

### Leiningen 1.0

This project has not been tested with versions of Leiningen 1. This
project is heavily based of `lein-autoexpect` which has been tested
against Leiningen 1. I would expect this project to work as well but
I'm not going to bother testing it nor do I plan on supporting it.

## Compatibility

lein-test-refresh has been tested to work with Clojure 1.5.1 and
Leiningen 2.3.[24].

Because of
[tools.namespace](https://github.com/clojure/tools.namespace) changes
`lein-test-refresh` requires that your project use Clojure >= 1.3.0. If
your project also depends on a version of `tools.namespace` < 0.2.1
you may see occasional exceptions.

## License

Copyright (C) 2011-2014 [Jake McCrary](http://jakemccrary.com)

Distributed under the Eclipse Public License, the same as Clojure.


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/jakemcc/lein-test-refresh/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

