(ns rewrite-indented.readme-test
  (:require
   [clojure.test :as t]
   [testdoc.core]))

(t/deftest readme-test
  (t/is (testdoc (slurp "README.adoc"))))
