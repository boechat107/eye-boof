(ns eye-boof.segmentation 
  (:require
    [eye-boof
     [core :refer :all]
     ]
    )
  (:import 
    [boofcv.struct.image ImageUInt8]
    [boofcv.alg.filter.binary ThresholdImageOps]))

(defmulti threshold
  "Converts a grayscale Image into a binary one by setting the pixels' values to
  1 or 0 accordingly a threshold value."
  (fn [img alg & opts] alg))

(defmethod threshold :default simple
  [^ImageUInt8 img _ & opts]
  (let [^ImageUInt8 output (new-image (width img) (height img))]
    (ThresholdImageOps/threshold img output (int (first opts)) false)))