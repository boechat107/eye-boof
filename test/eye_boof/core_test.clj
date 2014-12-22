(ns eye-boof.core-test
  (:require 
    [clojure.test :refer :all]
    [eye-boof.core :refer :all]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(deftest single-band
  (letfn [(procedures [img-type]
            (let [img (new-image 4 3 img-type)]
              (is (image? img))
              (is (== (nbands img) 1))
              (is (== (width img) 4))
              (is (== (height img) 3))
              (is (not (sub-image? img)))
              (do (set-pixel! img 2 2 11)
                  (is (== (pixel img 2 2) 11)))
              (let [sub-gray (sub-image img 1 1 3 3)
                    [ox oy] (parent-origin sub-gray)]
                (is (== (width sub-gray) 2))
                (is (== (height sub-gray) 2))
                (is (sub-image? sub-gray))
                (is (= [ox oy] [1 1]))
                (is (== (pixel sub-gray 1 1) 11))
                (testing "Shared data between an image and its sub-image"
                  (set-pixel! sub-gray 0 0 4)
                  (is (== (pixel img ox oy) 4))))
              img))]
    (testing "ImageUInt8 images"
      (procedures :uint8))
    (testing "ImageFloat32 images"
      (let [img (procedures :float32)]
        (do (set-pixel! img 0 0 2.56)
            (is (== (float 2.56) (pixel img 0 0))))))
    (testing "ImageSInt16 images"
      (procedures :sint16))))

(deftest multispectral 
  (let [color-img (new-image 4 3 :uint8 3)]
    (testing "Colored images"
      (is (image? color-img))
      (is (== (nbands color-img) 3))
      (is (== (width color-img) 4))
      (is (== (height color-img) 3))
      (is (not (sub-image? color-img)))
      (set-pixel! color-img 2 2 11 12 13)
      (is (= (pixel color-img 2 2) [11 12 13])))))
