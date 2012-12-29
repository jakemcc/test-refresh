(ns leiningen.autoexpect)

(defn- eval-in-project
  "Support eval-in-project in both Leiningen 1.x and 2.x."
  [project form init]
  (let [[eip two?] (or (try (require 'leiningen.core.eval)
                            [(resolve 'leiningen.core.eval/eval-in-project)
                             true]
                            (catch java.io.FileNotFoundException _))
                       (try (require 'leiningen.compile)
                            [(resolve 'leiningen.compile/eval-in-project)]
                            (catch java.io.FileNotFoundException _)))]
    (if two?
      (eip project form init)
      (eip project form nil nil init))))

(def deps [['lein-autoexpect "0.2.3-SNAPSHOT"]
           ['org.clojure/tools.namespace "0.2.2"]])

(defn- add-deps [project]
  (if-let [conj-dependency (resolve 'leiningen.core.project/conj-dependency)]
    (binding [*out* (java.io.StringWriter.)]
      (reduce conj-dependency project deps))
    (reduce (partial update-in project [:dependencies] conj) deps)))

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
    (eval-in-project
     (add-deps project)
     `(autoexpect.runner/monitor-project ~should-growl)
     `(require 'autoexpect.runner))))
