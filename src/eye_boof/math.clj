(ns eye-boof.math
  (:require 
    [eye-boof.core :as c]
    )
  (:import 
    [boofcv.alg.misc PixelMath]
    [boofcv.struct.image ImageUInt8 ImageSInt16 ImageFloat32]))

(defmacro def-noargs-function
  [fname doc f]
  `(defn ~fname 
     ~doc
     ([~'img] (~fname ~'img 0))
     ([~'img ~'ch]
      (let [och# (c/new-channel-matrix (c/nrows ~'img) (c/ncols ~'img) 1)]
        (~f (c/get-channel ~'img ~'ch) och#)
        (c/make-image och# (:type ~'img))))))

(def-noargs-function 
  abs
  "Returns a new image where its pixels' values correspond to the absolute values
  of the given image."
  PixelMath/abs)
