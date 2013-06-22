(ns eye-boof.binary-ops
  (:require [eye-boof
             [core :as c]
             [processing :as p]])

  (:require [eye-boof
             [helpers :as h]])

  (:import
   [boofcv.alg.filter.binary ThresholdImageOps]
   [boofcv.alg.filter.binary BinaryImageOps]
   [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageSInt32 ImageFloat32 MultiSpectral])
  )


;; 0 1 1 1 0 0
;; 0 1 1 1 0 0
;; 0 1 1 1 0 0
;; 0 0 0 0 0 0
;; 0 1 1 0 1 0
;; 0 0 0 0 0 1

(def example-img
  (let [img (c/new-image 6 6 :bw)
        chn (c/get-channel img 1)]
    (c/set-pixel!* chn 0 1 1)
    (c/set-pixel!* chn 0 2 1)
    (c/set-pixel!* chn 0 3 1)
    (c/set-pixel!* chn 1 1 1)
;    (c/set-pixel!* chn 1 2 1)
    (c/set-pixel!* chn 1 3 1)
    (c/set-pixel!* chn 2 1 1)
    (c/set-pixel!* chn 2 2 1)
    (c/set-pixel!* chn 2 3 1)
    
    (c/set-pixel!* chn 4 4 1)
    (c/set-pixel!* chn 5 5 1)

    (c/set-pixel!* chn 4 1 1)
    (c/set-pixel!* chn 4 2 1)

    img))

(defn contour [img rule labeled-img]
  {:pre [(= :bw (:type img))]}
  (BinaryImageOps/contour (:mat img) rule labeled-img))