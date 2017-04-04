(ns eye-boof.test.core
  (:require [clojure.test :refer :all]
            [eye-boof.core :refer :all])
  (:import [boofcv.struct.image GrayU8]))

(deftest test-as-seq
  (testing "Gray images"
    (let [img (load-image->gray-u8 "test/rgbb_gray.jpg")
          img-seq (vec (as-seq img))
          w (.getWidth img)]
      (is
       (every? identity
               ;; For each pixel, checks if both data structures have the same
               ;; value.
               (for [y (range (.getHeight img)) x (range w)]
                 (== (img-seq (+ x (* w y)))
                     (.unsafe_get img x y))))))))

(deftest smoke-testing-basic-handlers
  (let [gimg (load-image->gray-u8 "test/rgbb_gray.jpg")
        pimg (load-image->planar-u8 "test/rgbb.jpg")]
    (testing "General handlers"
      (is (== 30 (width gimg) (width pimg)))
      (is (== 30 (height gimg) (height pimg)))
      (is (== 39 (get-pixel gimg 9 7)))
      (is (== 22 (-> gimg (set-pixel! 9 7 22) (get-pixel 9 7))))
      )))
