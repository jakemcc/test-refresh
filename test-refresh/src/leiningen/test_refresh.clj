(ns leiningen.test-refresh
  (:require [clojure.pprint :refer [pprint]]
            [leinjacker.deps :as deps]
            [leinjacker.eval :as eval]
            [leiningen.test :as test]
            [leiningen.core.project :as project]))

(defn- add-deps [project]
  (let [test-refresh-plugin (first (filter (fn [[name version]] (= name 'com.jakemccrary/lein-test-refresh)) (:plugins project)))]
    (deps/add-if-missing project test-refresh-plugin)))

(defn- clojure-test-directories [project]
  (vec (concat (:test-path project [])
               (:test-paths project []))))

(defn parse-commandline [project args]
  (let [{:keys [notify-command notify-on-success growl silence quiet report changes-only once]} (:test-refresh project)
        should-growl (or (some #{:growl ":growl" "growl"} args) growl)
        changes-only (or (some #{:changes-only ":changes-only" "chnages-only"} args) changes-only)
        args (remove #{:growl ":growl" "growl" :changes-only ":changes-only" "changes-only"} args)
        notify-on-success (or (nil? notify-on-success) notify-on-success)
        selectors (filter keyword? args)
        once? (or (some #{:once ":once" "once"} args) once)]
    {:growl should-growl
     :changes-only changes-only
     :notify-on-success notify-on-success
     :notify-command notify-command
     :nses-and-selectors (#'test/read-args args project)
     :test-paths (clojure-test-directories project)
     :quiet quiet
     :report report
     :once once?}))

(defn test-refresh
  "Autoruns clojure.test tests on source change or
  on the ENTER key being pressed.

  USAGE: lein test-refresh
  Runs tests whenever there is a change to code in classpath.
  Reports test successes and failures to STDOUT.

  USAGE: lein test-refresh :growl
  Runs tests whenever code changes.
  Reports results to growl and STDOUT."
  [project & args]
  (let [project (-> project
                    (project/merge-profiles [:test])
                    add-deps)
        {:keys [test-paths] :as options} (parse-commandline project args)]
    (eval/eval-in-project
     project
     `(com.jakemccrary.test-refresh/monitor-project ~test-paths
                                                    '~options)
     `(require 'com.jakemccrary.test-refresh))))
