(ns rewrite-indented.core-test
  (:require
   [clojure.test :as t]
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
