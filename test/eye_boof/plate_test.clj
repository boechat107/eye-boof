(ns eye-boof.plate-test
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p]))

(def img-test
  (h/load-file-image "test/black_plate.jpg"))

(deftest pre-proc
  (let [orig (p/scale img-test 0.2)
        gray (p/to-gray orig)
        bin (-> (p/gaussian-blur gray 0.5 -1) (p/binarize 100))]
    (h/view* gray bin)))
