(ns rewrite-indented.parser-test
  (:require
   [clojure.test :as t]
   [rewrite-indented.parser :as sut]))

(t/deftest parse-test
  (let [res (sut/parse "a")]
    (t/is (= [["a"]] res))
    (t/is (= {:level 0 :prefix ""} (meta (first res)))))

  (t/is (= [["a"] ["b"]]
           (sut/parse "a\nb")))

  (t/is (= [["a" [["b"]]]]
           (sut/parse "a\n b")))

  (t/is (= [["a" [["b" [["c"]]]
                  ["d"]]]]
           (sut/parse "a\n b\n  c\n d")))

  (t/is (= [["a" [["b" [["c"]]]]]
            ["d" [["e"]]]]
           (sut/parse "a\n b\n  c\nd\n e")))

  (t/is (= [["a" [["b" [["c"]]]]]
            ["d" [["e"]
                  ["f" [["g"]]]]]
            ["h"]]
           (sut/parse "a\n b\n  c\nd\n e\n f\n  g\nh")))

  (t/is (= [["a" [["b"] [""] [""] ["c"]]]]
           (sut/parse "a\n b\n\n\n c"))))

(defn- parse-with-meta
  [s]
  (let [res (sut/parse s)]
    {:res res :meta (meta res)}))

(t/deftest parse-meta-test
  (t/is (= {:res [[""]] :meta {:head-blanks "" :tail-blanks ""}}
           (parse-with-meta "")))
  (t/is (= {:res [["a"]] :meta {:head-blanks "" :tail-blanks ""}}
           (parse-with-meta "a")))
  (t/is (= {:res [["a"]] :meta {:head-blanks "\n\t" :tail-blanks ""}}
           (parse-with-meta "\n\ta")))
  (t/is (= {:res [["a"]] :meta {:head-blanks "" :tail-blanks "\n\t"}}
           (parse-with-meta "a\n\t")))
  (t/is (= {:res [["a"]] :meta {:head-blanks "\t\n" :tail-blanks "\n\t"}}
           (parse-with-meta "\t\na\n\t")))
  (t/is (= {:res [[""]] :meta {:head-blanks "\n\t" :tail-blanks ""}}
           (parse-with-meta "\n\t"))))
