(ns build
  (:require
   [clojure.tools.build.api :as b]
   [clojure.xml :as xml]
   [deps-deploy.deps-deploy :as deploy]))

(def ^:private class-dir "target/classes")
(def ^:private jar-file "target/rewrite-indented.jar")
(def ^:private lib 'com.github.liquidz/rewrite-indented)
(def ^:private pom-file "./pom.xml")

(defn- get-current-version
  [pom-file-path]
  (->> (xml/parse pom-file-path)
       (xml-seq)
       (some #(and (= :version (:tag %)) %))
       (:content)
       (first)))

(defn pom
  [_]
  (b/write-pom {:basis (b/create-basis)
                :class-dir class-dir
                :lib lib
                :version (get-current-version pom-file)
                :src-dirs ["src"]})
  (b/copy-file {:src (b/pom-path {:lib lib :class-dir class-dir})
                :target pom-file}))

(defn jar
  [arg]
  (pom arg)
  (b/copy-dir {:src-dirs (:paths (b/create-basis))
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file}))

(defn install
  [arg]
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :local}))

(defn deploy
  [arg]
  (assert (and (System/getenv "CLOJARS_USERNAME")
               (System/getenv "CLOJARS_PASSWORD")))
  (jar arg)
  (deploy/deploy {:artifact jar-file
                  :installer :remote
                  :pom-file (b/pom-path {:lib (or (:lib arg) lib)
                                         :class-dir class-dir})}))
