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

(def deps [['lein-autoexpect "0.2.1-SNAPSHOT"]])

(defn- add-deps [project]
  (if-let [conj-dependency (resolve 'leiningen.core.project/conj-dependency)]
    (reduce conj-dependency project deps)
    (reduce (partial update-in project [:dependencies] conj) deps)))

(defn autoexpect
  "Autoruns expecations on source change"
  [project & args]
  (eval-in-project
   (add-deps project)
   `(autoexpect.runner/monitor-project)
   `(require 'autoexpect.runner)))
