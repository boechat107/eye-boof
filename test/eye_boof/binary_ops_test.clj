(ns eye-boof.test.binary-ops-test
  (:use [clojure.test]
        [eye-boof.resources :only (img-small-connected)])
  (:require [eye-boof
             [binary-ops :as binop]
             [core :as c]
             [helpers :as h]])
  (:import [georegression.struct.point Point2D_I32]
           [boofcv.struct.image ImageSInt32]))

(deftest contour-test
  (is (= (-> (binop/labeled-image img-small-connected 4) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 5])
      "Labeled image with 4-neighbours")
  (is (= (-> (binop/labeled-image img-small-connected 8) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 3])
      "Labeled image with 8-neighbours"))