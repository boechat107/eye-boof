(ns eye-boof.test.helpers-test
  (:use [clojure.test]
        eye-boof.helpers)
  (:require [eye-boof [core :as c]
             [visualize :as v]]
            )
  (:import [java.awt.image BufferedImage]))

(defn equal-images? [img1 img2]
  (when (and (= (c/get-type img1) (c/get-type img2))
             (= (c/dimension img1) (c/dimension img2)))
    (loop [c (dec (c/dimension img1))]
      (or (= -1 c) ;;all channels are equal
          (and (= (c/channel-to-vec img1 c)
                  (c/channel-to-vec img2 c))
               (recur (dec c)))))))

;;(TODO) types are ok, but are the images?
(deftest conversion-tests
  (let [buff-img (load-file-buffImg "test/rgbb.jpg")
        buff-gray (load-file-buffImg "test/rgbb_gray.jpg")
        buff-bw (load-file-buffImg "test/rgbb_bw.jpg")
        img (to-img buff-img)
        gray (to-img buff-gray)
        bw (to-img buff-bw)]
    (is (= BufferedImage/TYPE_3BYTE_BGR (.getType buff-img)) "Buff Loaded RGB")
    (is (= BufferedImage/TYPE_BYTE_GRAY (.getType buff-gray)) "Buff Loaded gray")
    (is (= BufferedImage/TYPE_BYTE_BINARY (.getType buff-bw)) "Buff Loaded bw")
    
    (is (c/rgb-type? img) "Img converted to RGB")
    (is (c/gray-type? gray) "Img converted to rgb ")
    (is (c/bw-type? bw) "Loaded RGB")

    (is (= (.getType (to-buffered-image img))
           BufferedImage/TYPE_INT_RGB) "Buff Loaded RGB")
    (is (= (.getType (to-buffered-image gray))
           BufferedImage/TYPE_BYTE_GRAY) "Buff Loaded RGB")
    (is (= (.getType (to-buffered-image bw))
           BufferedImage/TYPE_BYTE_BINARY) "Buff Loaded RGB")

    (is (equal-images? img
                       (-> img to-buffered-image to-img)) "inter-conversion RGB")
    (is (equal-images? gray
                       (-> gray to-buffered-image to-img)) "inter-conversion gray")
    (is (equal-images? bw
                       (-> bw to-buffered-image to-img)) "inter-conversion bw"))

  )

