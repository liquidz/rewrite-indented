(ns rewrite-indented.zip-test
  (:require
   [clojure.test :as t]
   [clojure.zip :as zip]
   [rewrite-indented.zip :as sut]))

(t/deftest zipper-test
  (let [texts [""
               "\n"
               "\na\t"
               "a\n b\n  c\nd\n\te\n\t\tf"
               "a\n b\n\n\n c"]]
    (doseq [text texts]
      (t/testing text
        (t/is (= text
                 (-> (sut/of-string text)
                     (sut/root-string))))))))

(t/deftest find-next-string-test
  (let [zloc (sut/of-string "a\n x\nb\n x\nc\n x")
        first-x-zloc (-> zloc
                         (sut/find-next-string #(= "x" %)))
        second-x-zloc (-> first-x-zloc
                          (zip/next)
                          (sut/find-next-string #(= "x" %)))]
    (t/is (= ["a" [["x"]]]
             (some-> first-x-zloc
                     (zip/up) (zip/up) (zip/up) (zip/node))))

    (t/is (= ["b" [["x"]]]
             (some-> second-x-zloc
                     (zip/up) (zip/up) (zip/up) (zip/node))))

    (t/is (nil? (sut/find-next-string zloc #(= "unknown" %))))))

(t/deftest find-ancestor-string-test
  ;; a
  ;;  x
  ;; b
  ;;  x
  ;; c
  ;;  x
  (let [zloc (sut/of-string "a\n x\nb\n x\nc\n x")
        first-x-zloc (-> zloc
                         (sut/find-next-string #(= "x" %)))
        second-x-zloc (-> first-x-zloc
                          (zip/next)
                          (sut/find-next-string #(= "x" %)))]

    (t/is (nil? (sut/find-ancestor-string zloc #(= "a" %))))
    (t/is (some? (sut/find-ancestor-string first-x-zloc #(= "a" %))))
    (t/is (nil? (sut/find-ancestor-string second-x-zloc #(= "a" %))))

    (t/is (nil? (sut/find-ancestor-string zloc #(= "b" %))))
    (t/is (nil? (sut/find-ancestor-string first-x-zloc #(= "b" %))))
    (t/is (some? (sut/find-ancestor-string second-x-zloc #(= "b" %))))))

(t/deftest find-previous-string-test
  ;; a
  ;;  b
  ;;  c
  ;;   d
  (let [zloc (sut/of-string "a\n b\n c\n  d")
        d-zloc (-> zloc
                   (sut/find-next-string #(= "d" %)))]
    (t/is (nil? (sut/find-previous-string zloc #(= "a" %))))
    (t/is (some? (sut/find-previous-string d-zloc #(= "a" %))))
    (t/is (some? (sut/find-previous-string d-zloc #(= "b" %))))
    (t/is (some? (sut/find-previous-string d-zloc #(= "c" %))))))

(t/deftest update-test
  (t/testing "spaces"
    (let [text "first\n a\n  b\nsecond\n a\n b"
          zloc (sut/of-string text)]
      (t/is (= "first\n a!\n  b\nsecond\n a\n b"
               (-> zloc
                   (sut/find-next-string #(= "a" %))
                   (sut/update #(str % "!"))
                   (sut/root-string))))))

  (t/testing "tabs"
    (let [text "a\n b\n \tc"
          zloc (sut/of-string text)]
      (t/is (= "a\n b!\n \tc"
               (-> zloc
                   (sut/find-next-string #(= "b" %))
                   (sut/update #(str % "!"))
                   (sut/root-string))))

      (t/is (= "a\n b\n \tc!"
               (-> zloc
                   (sut/find-next-string #(= "c" %))
                   (sut/update #(str % "!"))
                   (sut/root-string)))))))
