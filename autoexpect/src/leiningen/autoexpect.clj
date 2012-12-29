(ns leiningen.autoexpect
  (:require [leinjacker.deps :as deps]
            [leinjacker.eval :as eval]))

(defn- add-deps [project]
  (-> project
      (deps/add-if-missing '[lein-autoexpect "0.2.6-SNAPSHOT"])
      (deps/add-if-missing '[org.clojure/tools.namespace "0.2.2"])))

(defn ^{:help-arglists '([])} autoexpect
  "Autoruns expecations on source change

USAGE: lein autoexpect
Runs expectations whenever there is a change to code in classpath.
Reports test successes and failures to STDOUT.

USAGE: lein autoexpect :growl
Runs expectations whenever code changes.
Reports results to growl and STDOUT."
  [project & args]
  (let [should-growl (some #{:growl ":growl" "growl"} args)]
    (eval/eval-in-project
     (add-deps project)
     `(autoexpect.runner/monitor-project ~should-growl)
     `(require 'autoexpect.runner))))
