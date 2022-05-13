(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as deploy]))

(def ^:private version (format "0.1.%s" (b/git-count-revs nil)))
(def ^:private class-dir "target/classes")
(def ^:private jar-file "target/rewrite-indented.jar")
(def ^:private lib 'com.github.liquidz/rewrite-indented)
(def ^:private scm
  {:connection "scm:git:git://github.com/liquidz/rewrite-indented.git"
   :developerConnection "scm:git:ssh://git@github.com/liquidz/rewrite-indented.git"
   :url "https://github.com/liquidz/rewrite-indented"
   :tag version})

(defn print-version
  "cf. https://docs.github.com/en/actions/using-workflows/workflow-commands-for-github-actions#example-setting-a-value"
  [_]
  (println (str "::set-output name=version::" version)))

(defn pom
  [_]
  (let [basis (b/create-basis)]
    (b/write-pom {:basis basis
                  :class-dir class-dir
                  :lib lib
                  :version version
                  :src-dirs (:paths basis)
                  :scm scm})))

(defn jar
  [arg]
  (let [basis (b/create-basis)]
    (pom arg)
    (b/copy-dir {:src-dirs (:paths basis)
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file jar-file})))

(defn install
  [arg]
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :local
                  :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))

(defn deploy
  [arg]
  (assert (and (System/getenv "CLOJARS_USERNAME")
               (System/getenv "CLOJARS_PASSWORD")))
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :remote
                  :pom-file (b/pom-path {:lib lib :class-dir class-dir})}))
