(ns eye-boof.test.filters
  (:require [clojure.test :refer :all]
            [eye-boof.filters :refer :all]
            [eye-boof.core.io :refer [load-image->gray-u8
                                      load-image->planar-u8]])
  (:import [boofcv.struct.image GrayU8 Planar]))

(deftest smoke-test-blur
  (let [img (load-image->gray-u8 "test/rgbb_gray.jpg")
        imgp (load-image->planar-u8 "test/rgbb.jpg")
        check (fn [f]
                (is (instance? GrayU8 (f img)))
                (is (instance? Planar (f imgp))))]
    (check #(gaussian-blur % 0.5 5))
    (check #(gaussian-blur % 0. 5))
    (check #(gaussian-blur % 0.5 -1))
    ;;
    (check #(mean-blur % 5))
    ;;
    (check #(median-blur % 5))))
