(ns eye-boof.core-test
  (:refer-clojure :exclude [lazy-seq])
  (:require 
    [clojure.test :refer :all]
    [eye-boof.core :refer :all]))

(deftest single-band
  (let [gray-img (new-image 4 3)]
    (testing "Grayscale images"
      (is (image? gray-img))
      (is (== (nbands gray-img) 1))
      (is (== (width gray-img) 4))
      (is (== (height gray-img) 3))
      (is (not (sub-image? gray-img)))
      (set-pixel! gray-img 2 2 11)
      (is (== (pixel gray-img 2 2) 11))
      (is (= (lazy-seq gray-img) [0 0 0 0
                                  0 0 0 0
                                  0 0 11 0]))
      (let [sub-gray (sub-image gray-img 1 1 2 2)
            [ox oy] (parent-origin sub-gray)]
        (is (== (width sub-gray) 2))
        (is (== (height sub-gray) 2))
        (is (sub-image? sub-gray))
        (is (= [ox oy] [1 1]))
        (is (== (pixel sub-gray 1 1) 11))
        (testing "Shared data between an image and its sub-image"
          (set-pixel! sub-gray 0 0 4)
          (is (== (pixel gray-img ox oy) 4)))))))

(deftest multispectral 
  (let [color-img (new-image 4 3 3)]
    (testing "Colored images"
      (is (image? color-img))
      (is (== (nbands color-img) 3))
      (is (== (width color-img) 4))
      (is (== (height color-img) 3))
      (is (not (sub-image? color-img)))
      (set-pixel! color-img 2 2 11 12 13)
      (is (= (pixel color-img 2 2) [11 12 13])))))
