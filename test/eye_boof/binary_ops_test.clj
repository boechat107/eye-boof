(ns eye-boof.test.binary-ops-test
  (:use [clojure.test]
        [eye-boof.resources :only (img-small-connected)])
  (:require [eye-boof
             [binary-ops :as binop]
             [core :as c]
             [helpers :as h]])
  (:import [georegression.struct.point Point2D_I32]
           [boofcv.struct.image ImageSInt32]))

(deftest labeled-img-test
  (is (= (-> (binop/labeled-image img-small-connected 4) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 5])
      "Labeled image with 4-neighbours")
  ;;Perhaps this test is reduntant, but test it anyway =)
  (is (= (-> (binop/labeled-image img-small-connected 8) .data vec)
         [0 0 0 0 1 1 1 2 2 2 0 1 1 1 2 0 2 0 1 1 0 2 2 2 0 0 0 0 0 0 0 0 3 3 0 0 4 4 0 3 3 0 0 4 4 0 0 0 3])
      "Labeled image with 8-neighbours"))

(deftest contour-test
  (let [contours4 (binop/contour img-small-connected 4)]
    (is (= 5 (count contours4))
        "Contours4: correct amount of contours")
    (is (= 1 (count (keep #(-> % .internal seq) contours4)))
        "Contours4: correct amount of internal contours")
    (is (= 5 (count (keep #(-> % .external seq) contours4)))
        "Contours4: correct amount of external contours"))
  
  (let [contours8 (binop/contour img-small-connected 8)]
    (is (= 4 (count contours8))
        "Contours8: correct amount of contours")
    (is (= 1 (count (keep #(-> % .internal seq) contours8)))
        "Contours8: correct amount of internal contours")
    ;;Perhaps this test is reduntant, but test it anyway =)
    (is (= 4 (count (keep #(-> % .external seq) contours8)))
        "Contours8: correct amount of external contours")))


