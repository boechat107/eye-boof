(ns eye-boof.core-test
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p]))

(def img-test 
  "A simple image created with imgpviewer. It is 4x4 image with one white pixel, one
  red, one green and one blue, the others are all black."
  ;(h/load-file-image "test/img_test.png") ; argb image
  (let [img (c/new-image 4 4 :rgb)
        rch (c/get-channel img 0)
        gch (c/get-channel img 1)
        bch (c/get-channel img 2)
        fname "test/img_boofcv.png"]
    (c/set-pixel! rch 0 255)
    (c/set-pixel! gch 0 255)
    (c/set-pixel! bch 0 255)
    (c/set-pixel! rch 5 255)
    (c/set-pixel! gch 10 255)
    (c/set-pixel! bch 15 255)
    (h/save-to-file! img fname)
    (h/load-file-image fname)))

(deftest pixel-vals
  (let [rch (c/get-channel img-test 0)
        gch (c/get-channel img-test 1)
        bch (c/get-channel img-test 2)]
    (is (== 255 (c/get-pixel rch 0)))
    (is (== 255 (c/get-pixel gch 0)))
    (is (== 255 (c/get-pixel bch 0)))
    (is (== 255 (c/get-pixel rch 5)))
    (is (== 0 (c/get-pixel gch 5)))
    (is (== 0 (c/get-pixel bch 5)))
    (is (== 0 (c/get-pixel rch 10)))
    (is (== 255 (c/get-pixel gch 10)))
    (is (== 0 (c/get-pixel bch 10)))))

(defn time-test
  []
  (let [img (time (h/load-file-image "test/cnh.png"))
        gray (time (p/rgb-to-gray img))
        bin (time (p/binarize gray 100))
        blur (time (p/mean-blur img 1))
        ]
    (time (h/view img blur))
    nil
    )
  )
