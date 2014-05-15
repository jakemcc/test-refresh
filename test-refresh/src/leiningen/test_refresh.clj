(ns leiningen.test-refresh
  (:require [clojure.pprint :refer [pprint]]
            [leinjacker.deps :as deps]
            [leinjacker.eval :as eval]
            [leiningen.test :as test]))

(defn- add-deps [project]
  (let [test-refresh-plugin (first (filter (fn [[name version]] (= name 'com.jakemccrary/lein-test-refresh)) (:plugins project)))]
    (-> project
        (deps/add-if-missing test-refresh-plugin)
        (deps/add-if-missing '[org.clojure/tools.namespace "0.2.4"]))))

(defn- clojure-test-directories [project]
  (vec (concat (:test-path project [])
               (:test-paths project []))))

(defn parse-commandline [project args]
  (let [{:keys [notify-command notify-on-success growl]} (:test-refresh project)
        should-growl (or (some #{:growl ":growl" "growl"} args) growl)
        args (remove #{:growl ":growl" "growl"} args)
        notify-on-success (or (nil? notify-on-success) notify-on-success)
        selectors (filter keyword? args)]
    {:growl should-growl
     :notify-on-success notify-on-success
     :notify-command notify-command
     :nses-and-selectors (#'test/read-args args project)
     :test-paths (clojure-test-directories project)}))

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
  (let [{:keys [growl notify-on-success notify-command nses-and-selectors test-paths]} (parse-commandline project args)]
    (eval/eval-in-project
     (add-deps project)
     `(com.jakemccrary.test-refresh/monitor-project ~test-paths
                                                    ~growl
                                                    '~notify-command
                                                    ~notify-on-success
                                                    '~nses-and-selectors
                                                    )
     `(require 'com.jakemccrary.test-refresh))))
