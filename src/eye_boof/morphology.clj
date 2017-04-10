(ns eye-boof.morphology
  (:import boofcv.alg.filter.binary.BinaryImageOps
           [boofcv.struct.image GrayU8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;;;;;;;;;;;;;;;;;;;;;;;
;; Binary operations ;;
;;;;;;;;;;;;;;;;;;;;;;;

(defmacro impl-ops
  "Defines a function fname that simply calls a BoofCV function with some
  specific parameters."
  [fname boofcv-fn docs]
  `(defn ~fname
     ~docs
     [~'img & [~'n]]
     (~boofcv-fn ~'img (or ~'n 1) nil)))

(impl-ops erode4 BinaryImageOps/erode4
          "GrayU8 -> int -> GrayU8
  Erodes an image using a 4-neighborhood. n is the number of times the operation
  will be applied.")

(impl-ops erode8 BinaryImageOps/erode8
          "GrayU8 -> int -> GrayU8
  Erodes an image using a 8-neighborhood. n is the number of times the operation
  will be applied.")

(impl-ops dilate4 BinaryImageOps/dilate4
          "GrayU8 -> int -> GrayU8
  Dilates an image using a 8-neighborhood. n is the number of times the operation
  will be applied.")

(impl-ops dilate8 BinaryImageOps/dilate8
          "GrayU8 -> int -> GrayU8
  Dilates an image using a 8-neighborhood. n is the number of times the operation
  will be applied.")

(defn remove-point-noise
  "GrayU8 -> GrayU8
  If a pixel is connected to less than 2 neighbors, then its value zero; if it's
  connected to more than 6, then its value is one."
  [img]
  (BinaryImageOps/removePointNoise ^GrayU8 img nil))
