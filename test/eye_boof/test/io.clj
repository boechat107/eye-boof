(ns eye-boof.test.io
  (:require [clojure.test :refer :all]
            [eye-boof.io :refer :all])
  (:import [java.awt.image BufferedImage]
           [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]))

(deftest test-load-image
  (let [img (load-image "test/rgbb_gray.jpg")]
    (is (instance? ImageUInt8 img))
    (is (= [30 30] [(.getWidth img) (.getHeight img)])))
  (testing "Colored image"
    (let [img (load-image "test/rgbb.jpg")
          pix-band (fn [x y b] (.unsafe_get (.getBand img b) x y))]
      (is (instance? MultiSpectral img))
      (is (= [30 30] [(.getWidth img) (.getHeight img)]))
      (is (== 3 (.getNumBands img)))
      ;; Checking if the order of the bands is correct.
      (are [x y, r g b] (and (== r (pix-band x y 0))
                             (== g (pix-band x y 1))
                             (== b (pix-band x y 2)))
           8 7, 255 6 0 ; red blob
           22 7, 0 0 245 ; blue blob
           8 22, 0 254 0 ; green blob
           23 21, 0 0 0))))