(ns eye-boof.test.helpers-test
  (:use [clojure.test]
        eye-boof.helpers)
  (:require [eye-boof.core :as c])
  (:import [java.awt.image BufferedImage]))


;;(TODO) types are ok, but are the images?
(deftest conversion-tests
  (let [buff-img (load-file-buffImg "test/rgbb.jpg")
        buff-gray (load-file-buffImg "test/rgbb_gray.jpg")
        buff-bw (load-file-buffImg "test/rgbb_bw.jpg")
        img (to-img buff-img)
        gray (to-img buff-gray)
        bw (to-img buff-bw)]
    (is (= BufferedImage/TYPE_3BYTE_BGR (.getType buff-img)) "Buff Loaded RGB")
    (is (= BufferedImage/TYPE_BYTE_GRAY (.getType buff-gray)) "Buff Loaded RGB")
    (is (= BufferedImage/TYPE_BYTE_BINARY (.getType buff-bw)) "Buff Loaded RGB")
    
    (is (c/rgb-type? img) "Loaded RGB")
    (is (c/gray-type? gray) "Loaded gray")
    (is (c/bw-type? bw) "Loaded RGB")

    (is (= (.getType (to-buffered-image img))
           BufferedImage/TYPE_3BYTE_BGR) "Buff Loaded RGB")
    (is (= (.getType (to-buffered-image gray))
           BufferedImage/TYPE_BYTE_GRAY) "Buff Loaded RGB")
    (is (= (.getType (to-buffered-image bw))
           BufferedImage/TYPE_BYTE_BINARY) "Buff Loaded RGB")))