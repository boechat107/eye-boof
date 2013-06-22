(ns eye-boof.test.binary-ops-test
  (:use [clojure.test])
  (:require [eye-boof
             [binary-ops :as binop]
             [core :as c]
             [helpers :as h]])
  (:import [georegression.struct.point Point2D_I32]
           [boofcv.struct.image ImageSInt32]))



(def example-img
  "Sample binary Image with the following pixels set on
   0 1 1 1 0 0
   0 1 1 1 0 0
   0 1 1 1 0 0
   0 0 0 0 0 0
   0 1 1 0 1 0
   0 0 0 0 0 1"
  (let [img (c/new-image 6 6 :bw)
        chn (c/get-channel img 1)]
    (c/set-pixel!* chn 0 1 1)
    (c/set-pixel!* chn 0 2 1)
    (c/set-pixel!* chn 0 3 1)
    (c/set-pixel!* chn 1 1 1)
    (c/set-pixel!* chn 1 2 1)
    (c/set-pixel!* chn 1 3 1)
    (c/set-pixel!* chn 2 1 1)
    (c/set-pixel!* chn 2 2 1)
    (c/set-pixel!* chn 2 3 1)
    
    (c/set-pixel!* chn 4 4 1)
    (c/set-pixel!* chn 5 5 1)

    (c/set-pixel!* chn 4 1 1)
    (c/set-pixel!* chn 4 2 1)

    img))

(deftest contour-test
  (is (= (-> (binop/labeled-img example-img 4) :mat .data vec)
         [0 0 0 0 0 0 1 1 1 0 2 0 1 1 1 0 2 0 1 1 1 0 0 0 0 0 0 0 3 0 0 0 0 0 0 4])
      "Labeled image with 4-neighbours")
  (is (= (-> (binop/labeled-img example-img 8) :mat .data vec)
         [0 0 0 0 0 0 1 1 1 0 2 0 1 1 1 0 2 0 1 1 1 0 0 0 0 0 0 0 3 0 0 0 0 0 0 3])
      "Labeled image with 8-neighbours"))