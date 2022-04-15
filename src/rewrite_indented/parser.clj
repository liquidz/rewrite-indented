(ns rewrite-indented.parser
  (:require
   [clojure.string :as str]
   [clojure.walk :as walk]))

(defn- get-indent-level
  [s]
  (or (some-> (re-seq #"^(\s*)([^\s]|$)" (or s ""))
              (first)
              (second)
              (count))
      0))

(defn- normalize-blank-line-level
  [lines]
  (->> (partition 2 1 (concat [(first lines)] lines))
       (map (fn [[prev-line line]]
              (let [prev-level (get-indent-level prev-line)]
                (if (str/blank? line)
                  (apply str (repeat prev-level " "))
                  line))))))

(def ^:private debug-enabled? (atom false))
(defn- debug
  "TODO FIXME DELETE ME"
  [& args]
  (if @debug-enabled?
    (apply println args)))
(comment (reset! debug-enabled? false))

(defn parse
  ([lines]
   (parse 0 lines))
  ([base-level lines]
   (loop [[fst & rst :as x] (partition-all 2 1 (normalize-blank-line-level lines))
          result []]
     (if-not fst
       result
       (let [[line next-line] fst
             level (get-indent-level line)
             next-level (get-indent-level next-line)
             line (str/trim line)]
         (debug (str "b" base-level)
                (str "l" level)
                (str "n" next-level)
                "\t" line
                "\t" (pr-str x) "=>" (pr-str result))
         (cond
           ;; Add to result since all levels are same
           (= base-level level next-level)
           (do (debug 'A)
               (recur rst (conj result (with-meta [line] {:level level}))))

           ;; Add to result and stop parsing here
           (and (< next-level base-level)
                (= base-level level))
           (do (debug 'B)
               (recur [] (conj result (with-meta [line] {:level level}))))

           ;; Stop parsing here
           (< next-level base-level)
           (do (debug 'D)
               (recur [] result))

           ;; Skip parsing
           (not= base-level level)
           (do (debug 'E)
               (recur rst result))

           ;; Link next level to the current level
           (> next-level level)
           (do (debug 'C)
               (recur rst (conj result (with-meta [line (parse next-level (map first rst))] {:level level}))))

           :else
           (do (debug 'X)
               (recur [] result))))))))

(defn unparse
  [parsed-data]
  (let [sw (java.io.StringWriter.)]
    (binding [*out* sw]
      (walk/prewalk (fn [elm]
                      (when-let [level (:level (meta elm))]
                        (let [fst (first elm)]
                          (if (and (string? fst) (str/blank? fst))
                            (println "")
                            (println (str (apply str (repeat level " ")) fst)))))
                      elm)
                    parsed-data))
    (str/trimr (str sw))))
