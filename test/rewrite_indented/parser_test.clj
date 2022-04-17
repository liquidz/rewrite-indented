(ns rewrite-indented.parser-test
  (:require
   [clojure.string :as str]
   [clojure.test :as t]
   [rewrite-indented.parser :as sut]))

(defn- parse
  [s]
  (-> (str/split-lines s)
      (sut/parse)))

(t/deftest parse-test
  (let [res (parse "a")]
    (t/is (= [["a"]] res))
    (t/is (= {:level 0 :prefix ""} (meta (first res)))))

  (t/is (= [["a"] ["b"]]
           (parse "a\nb")))

  (t/is (= [["a" [["b"]]]]
           (parse "a\n b")))

  (t/is (= [["a" [["b" [["c"]]]
                  ["d"]]]]
           (parse "a\n b\n  c\n d")))

  (t/is (= [["a" [["b" [["c"]]]]]
            ["d" [["e"]]]]
           (parse "a\n b\n  c\nd\n e")))

  (t/is (= [["a" [["b" [["c"]]]]]
            ["d" [["e"]
                  ["f" [["g"]]]]]
            ["h"]]
           (parse "a\n b\n  c\nd\n e\n f\n  g\nh"))))
