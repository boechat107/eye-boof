(ns eye-boof.test.morphology
  (:require [clojure.test :refer :all]
            [eye-boof.morphology :refer :all]
            [eye-boof.core :refer [threshold]]
            [eye-boof.core.io :refer [load-image->gray-u8
                                      load-image->planar-u8]])
  (:import [boofcv.struct.image GrayU8]))

(deftest smoke-test-binaryops
  (let [img (threshold (load-image->gray-u8 "test/rgbb_gray.jpg") 150 true)
        check #(is (instance? GrayU8 (% img)))]
    (check erode4)
    (check #(erode4 % 4))
    (check erode8)
    (check #(erode8 % 4))
    (check dilate4)
    (check #(dilate4 % 4))
    (check dilate8)
    (check #(dilate8 % 4))))
