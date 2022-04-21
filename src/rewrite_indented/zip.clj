(ns rewrite-indented.zip
  (:refer-clojure :exclude [next update])
  (:require
   [clojure.string :as str]
   [clojure.zip :as zip]
   [rewrite-indented.parser :as parser]))

(def next
  "Alias for zip/next"
  zip/next)

(defn update
  [zloc f]
  (let [parent-vector-loc (zip/up zloc)
        parent-node (zip/node parent-vector-loc)]
    (-> parent-vector-loc
        (zip/replace (clojure.core/update parent-node 0 f))
        (zip/down))))

(defn move-to-root
  [loc]
  (loop [loc loc]
    (if-let [loc' (zip/up loc)]
      (recur loc')
      loc)))

(defn find-next
  ([zloc p?]
   (find-next zloc zip/next p?))
  ([zloc f p?]
   (when (and zloc (not (zip/end? zloc)))
     (if (p? (zip/node zloc))
       zloc
       (recur (f zloc) f p?)))))

(defn find-next-string
  [zloc p?]
  (find-next zloc
             #(and (string? %)
                   (p? %))))

(defn find-ancestor-string
  [zloc p?]
  (find-next zloc zip/up #(and (vector? %)
                               (string? (first %))
                               (p? (first %)))))

(defn of-string
  [s]
  (->> (str/split-lines s)
       (parser/parse)
       (zip/zipper vector? seq
                   (fn [existing-node new-node]
                     (with-meta new-node (meta existing-node))))))

(defn of-file
  [f]
  (of-string (slurp f)))

(defn root-string
  [zloc]
  (-> (move-to-root zloc)
      (zip/node)
      (parser/unparse)))
