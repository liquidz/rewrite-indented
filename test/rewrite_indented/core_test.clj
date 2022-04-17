(ns rewrite-indented.core-test
  (:require
   [clojure.test :as t]
   [clojure.zip :as zip]
   [rewrite-indented.core :as sut]))

(t/deftest zipper-test
  (let [text "first\n a\n  b\nsecond\n a\n b"]
    (t/is (= text
             (-> (sut/string->zipper text)
                 (sut/zipper->string))))

    (t/is (= "first\n a!\n  b\nsecond\n a\n b"
             (-> (sut/string->zipper text)
                 (sut/find-next-string #(= "a" %))
                 (sut/update #(str % "!"))
                 (sut/zipper->string))))))

(t/deftest find-next-string-test
  (let [zloc (sut/string->zipper "a\n x\nb\n x\nc\n x")
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
  (let [zloc (sut/string->zipper "a\n x\nb\n x\nc\n x")
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

(t/deftest update-test
  (let [text "first\n a\n  b\nsecond\n a\n b"
        zloc (sut/string->zipper text)]
    (t/is (= "first\n a!\n  b\nsecond\n a\n b"
             (-> zloc
                 (sut/find-next-string #(= "a" %))
                 (sut/update #(str % "!"))
                 (sut/zipper->string))))))
