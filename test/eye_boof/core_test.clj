(ns eye-boof.core-test
  (:use clojure.test)
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.processing :as p])
  (:import [boofcv.struct.image ImageUInt8]))


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
    (c/set-pixel! rch 1 1 255)
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
    (is (== 0 (c/get-pixel bch 10)))
    (is (= (c/channel-to-vec img-test 0)
           [255 0 0 0 0 255 0 0 0 0 0 0 0 0 0 0]))
    (is (= (c/channel-to-vec img-test 1)
           [255 0 0 0 0 0 0 0 0 0 255 0 0 0 0 0]))
    (is (= (c/channel-to-vec img-test 2)
           [255 0 0 0 0 0 0 0 0 0 0 0 0 0 0 255]))))

(deftest sub-images
  (let [w 3
        h 3
        si (c/sub-image img-test 0 0 h w)
        [rch gch bch] (c/get-channel si)
        ssi (c/sub-image si 1 1 2 2)
        [r g b] (c/get-channel ssi)]
    (is (== w (c/ncols si)))
    (is (== h (c/nrows si)))
    (is (== 255 (c/get-pixel rch 1 1)))
    (is (== 255 (c/get-pixel gch 2 2)))
    (is (= [0 0] (c/get-parent-point si)))
    (is (== 2 (c/ncols ssi)))
    (is (== 2 (c/nrows ssi)))
    (is (== 255 (c/get-pixel r 0 0)))
    (is (== 255 (c/get-pixel g 1 1)))
    (is (== 0 (c/get-pixel b 1 1)))
    (is (= [1 1] (c/get-parent-point ssi)))))

(deftest img-scaling
  (let [orig (h/load-file-image "resources/boofcv.jpg")
        bigger (time (p/scale orig 1.5 1.5))
        smaller (time (p/scale orig 0.5 0.5))]
    (h/view* orig bigger smaller)))

(defn time-set-pixel
  []
  (let [ch (c/new-channel-matrix 1000 1000 1)]
    (time (dotimes [y 1000]
            (dotimes [x 1000]
              (c/set-pixel!* ch x y 10))))
    (time (dotimes [y 1000]
            (dotimes [x 1000]
              (c/set-pixel! ch x y 10))))
    (time (dotimes [idx 1000000]
            (c/set-pixel!* ch idx 10)))
    (time (dotimes [idx 1000000]
            (aset-byte (.data ch) idx 10)))))

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
