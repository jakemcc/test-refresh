(ns leiningen.autotest
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :as eval]))

(defn- add-deps [project]
  (-> project
      (deps/add-if-missing '[com.jakemccrary/lein-autotest "1.1-SNAPSHOT"])
      (deps/add-if-missing '[org.clojure/tools.namespace "0.2.4"])))

(defn- clojure-code-directories [project]
  (doall (concat (:source-path project [])
                 (:source-paths project [])
                 (:test-path project [])
                 (:test-paths project []))))

(defn ^{:help-arglists '([])} autotest
  "Autoruns clojure.test tests on source change

USAGE: lein autotest
Runs tests whenever there is a change to code in classpath.
Reports test successes and failures to STDOUT.

USAGE: lein autotest :growl
Runs tests whenever code changes.
Reports results to growl and STDOUT."
  [project & args]
  (let [should-growl (some #{:growl ":growl" "growl"} args)
        code-dirs (clojure-code-directories project)]
    (eval/eval-in-project
     (add-deps project)
     `(autotest.runner/monitor-project ~should-growl '~code-dirs)
     `(require 'autotest.runner))))
