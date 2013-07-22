(ns eye-boof.statistics-test 
  (:use clojure.test)
  (:require 
    [eye-boof 
     [core :as c]
     [image-statistics :as stat]
     [helpers :as h]]))

(def img
  (let [ch (c/new-channel-matrix 4 4 1)]
    (c/set-pixel! ch 0 0 50)
    (c/set-pixel! ch 3 3 50)
    (c/set-pixel! ch 0 3 100)
    (c/set-pixel! ch 3 0 100)
    (c/make-image ch :gray)))

(deftest statistics 
  (is (== (int (/ 300 16)) (int (stat/mean img))))
  (let [hist (stat/histogram img)]
    (is (== 255) (count hist))
    (is (== (hist 0) 12))
    (is (== (hist 50) 2))
    (is (== (hist 100) 2))))
