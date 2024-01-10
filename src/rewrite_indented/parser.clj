(ns rewrite-indented.parser
  (:require
   [clojure.string :as str]
   [clojure.walk :as walk]))

(defn- get-indent-level
  [s]
  (if-let [prefix (some-> (re-seq #"^(\s*)([^\s]|$)" (or s ""))
                          (first)
                          (second))]
    {:prefix prefix :level (count prefix)}
    {:level 0}))

(defn- normalize-blank-line-level
  [lines]
  (loop [[[prev-line line] & rest-lines] (partition 2 1 (concat [(first lines)] lines))
         result []]
    (if-not prev-line
      result
      (let [{:keys [level]} (get-indent-level (if (seq result)
                                                (last result)
                                                prev-line))
            new-line (if (str/blank? line)
                       (apply str (repeat level " "))
                       line)]
        (recur rest-lines (conj result new-line))))))

(defn- parse*
  [base-level lines]
  (loop [[fst & rst] (partition-all 2 1 (normalize-blank-line-level lines))
         result []]
    (if-not fst
      result
      (let [[line next-line] fst
            {:keys [level] :as line-meta} (get-indent-level line)
            {next-level :level} (get-indent-level next-line)
            line (str/trim line)]
        (cond
          ;; Add to result since all levels are same
          (= base-level level next-level)
          (recur rst (conj result (with-meta [line] line-meta)))

          ;; Add to result and stop parsing here
          (and (< next-level base-level)
               (= base-level level))
          (recur [] (conj result (with-meta [line] line-meta)))

          ;; Stop parsing here
          (< next-level base-level)
          (recur [] result)

          ;; Skip parsing
          (not= base-level level)
          (recur rst result)

          ;; Link next level to the current level
          (> next-level level)
          (recur rst (conj result (with-meta [line (parse* next-level (map first rst))] line-meta)))

          :else
          (recur [] result))))))

(defn parse
  [s]
  (let [lines (str/split-lines (str/trim s))
        head-blanks (first (re-seq #"^\s*" s))
        tail-blanks (if (re-seq #"^\s*$" s)
                      ""
                      (first (re-seq #"\s*$" s)))]
    (with-meta
      (parse* 0 lines)
      {:head-blanks head-blanks
       :tail-blanks tail-blanks})))

(defn unparse
  [parsed-data]
  (let [sw (java.io.StringWriter.)
        {:keys [head-blanks tail-blanks]} (meta parsed-data)]
    (binding [*out* sw]
      (walk/prewalk (fn [elm]
                      (let [{:keys [prefix level]} (meta elm)]
                        (when level
                          (let [fst (first elm)]
                            (if (and (string? fst) (str/blank? fst))
                              (println "")
                              (println (str (or prefix
                                                (apply str (repeat level " ")))
                                            fst))))))
                      elm)
                    parsed-data))
    (str head-blanks
         (str/trim (str sw))
         tail-blanks)))
