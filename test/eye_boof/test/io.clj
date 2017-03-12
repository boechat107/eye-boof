(ns eye-boof.test.io
  (:require [clojure.test :refer :all]
            [eye-boof.core.io :refer :all])
  (:import [java.awt.image BufferedImage]
           [boofcv.struct.image GrayU8]
           [java.io File]
           [java.net URL]))

(deftest test-loading-gray-images
  (letfn [(check [r w h]
            (let [img (load-image->gray-u8 r)]
              (is (instance? GrayU8 img))
              (is (== w (.getWidth img)))
              (is (== h (.getHeight img)))))]
    (testing "Loading file path"
      (check "test/rgbb_gray.jpg" 30 30))
    (testing "Loading file"
      (check (File. "test/rgbb_gray.jpg") 30 30))
    (testing "Loading URL"
      (check (URL. "http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg")
             200 149))))
