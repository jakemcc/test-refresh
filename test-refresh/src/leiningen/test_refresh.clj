(ns leiningen.test-refresh
  (:require [leiningen.test :as test]
            [leiningen.core.project :as project]
            [leiningen.core.eval :as eval]))

(def is-test-refresh? (comp (partial = 'com.jakemccrary/lein-test-refresh)
                            symbol
                            first))

(defn- add-deps [project]
  (let [test-refresh-plugin (first (filter is-test-refresh? (:plugins project)))]
    (if (some is-test-refresh? (:dependencies project))
      project
      (update-in project [:dependencies] (fnil conj []) test-refresh-plugin))))

(defn- clojure-test-directories [project]
  (vec (concat (:test-path project [])
               (:test-paths project []))))

(defn project-options [project args]
  (let [{:keys [notify-command notify-on-success growl
                silence quiet report changes-only run-once
                focus-flag
                with-repl watch-dirs refresh-dirs stack-trace-depth]} (:test-refresh project)
        should-growl (or (some #{:growl ":growl" "growl"} args) growl)
        changes-only (or (some #{:changes-only ":changes-only" "changes-only"} args) changes-only)
        run-once? (or (some #{:run-once ":run-once" "run-once"} args) run-once)
        with-repl? (or (some #{:with-repl ":with-repl" "with-repl"} args) with-repl)
        args (remove #{:growl ":growl" "growl"
                       :changes-only ":changes-only" "changes-only"
                       :with-repl ":with-repl" "with-repl"
                       :run-once ":run-once" "run-once"} args)
        notify-on-success (or (nil? notify-on-success) notify-on-success)
        selectors (filter keyword? args)]
    {:growl should-growl
     :focus-flag focus-flag
     :stack-trace-depth stack-trace-depth
     :changes-only changes-only
     :notify-on-success notify-on-success
     :notify-command notify-command
     :nses-and-selectors (#'test/read-args args project)
     :test-paths (clojure-test-directories project)
     :quiet quiet
     :report report
     :run-once run-once?
     :with-repl with-repl?
     :watch-dirs watch-dirs
     :refresh-dirs refresh-dirs}))

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
                    (project/merge-profiles [:leiningen/test :test])
                    add-deps)
        {:keys [test-paths] :as options} (project-options project args)]
    (eval/eval-in-project
     project
     `(com.jakemccrary.test-refresh/monitor-project ~test-paths
                                                    '~options)
     `(require 'com.jakemccrary.test-refresh))))
