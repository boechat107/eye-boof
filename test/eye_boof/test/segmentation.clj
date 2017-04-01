(ns eye-boof.test.segmentation
  (:require [clojure.test :refer :all]
            [eye-boof.core.segmentation :refer :all]
            [eye-boof.core.io :refer [load-image->gray-u8]])
  (:import boofcv.struct.image.GrayU8))

(deftest smoke-testing-global-methods
  (let [img (load-image->gray-u8 "test/rgbb_gray.jpg")
        check #(is (instance? GrayU8 %))]
    (check (threshold img 100 true))
    (check (threshold-f img (constantly 50) false))
    (is (integer? (otsu-threshold img 0 255)))
    (is (integer? (entropy-threshold img 0 255)))
    ;; TODO: to review the function definition.
    (is (thrown? java.lang.ArrayIndexOutOfBoundsException
                 (otsu-threshold img 10 255)))
    (is (thrown? java.lang.ArrayIndexOutOfBoundsException
                 (entropy-threshold img 10 255)))))

(deftest smoke-testing-local-methods
  (let [img (load-image->gray-u8 "test/rgbb_gray.jpg")
        check #(is (instance? GrayU8 %))]
    (check (local-square-threshold img 20 true 2.5))
    (check (local-sauvola-threshold img 5 false))
    (check (local-sauvola-threshold img 5 false 0.5))))
