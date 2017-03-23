(ns eye-boof.core.segmentation
  "Set of image segmentation techniques.
  Ref.: http://boofcv.org/index.php?title=Tutorial_Image_Segmentation"
  (:import [boofcv.struct.image GrayU8]
           [boofcv.alg.filter.binary ThresholdImageOps GThresholdImageOps]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;;;;;;;;;;;;;;;;;;;;
;; Global methods ;;
;;;;;;;;;;;;;;;;;;;;

(defn threshold
  "Global threshold. If down1? is true, pixel values <= n are set to 1."
  [^GrayU8 img ^long n down1?]
  (ThresholdImageOps/threshold img nil n ^boolean down1?))

(defn threshold-f
  "GrayU8 -> (GrayU8 -> int) -> boolean -> GrayU8
  Global threshold with the value calculated from f(img). If down1? is true,
  pixel values <= f(img) are set to 1."
  [^GrayU8 img f down1?]
  (threshold img ^long (f img) down1?))

(defn otsu-threshold
  "GrayU8 -> int -> int -> int
  Computes the variance based threshold using the Otsu's method."
  [img min-val max-val]
  (GThresholdImageOps/computeOtsu ^GrayU8 img (int min-val) (int max-val)))

(defn entropy-threshold
  "GrayU8 -> int -> int -> int
  Computes a threshold which maximizes the entropy between the foreground and
  background regions."
  [img min-val max-val]
  (GThresholdImageOps/computeEntropy ^GrayU8 img (int min-val) (int max-val)))

;;;;;;;;;;;;;;;;;;;
;; Local methods ;;
;;;;;;;;;;;;;;;;;;;

(defn local-square-threshold
  "GrayU8 -> int -> boolean -> GrayU8
  Locally adaptive threshold computed using a square region centered on each
  pixel. The threshold value is equal the average intensity of the square region
  times the optional scale (default to 1.0).
  Ref.: https://goo.gl/nQn90i"
  [img radius down1? & [scale]]
  (GThresholdImageOps/localSquare
   img
   nil ; optional output
   radius
   (or scale 1.0)
   down1?
   ;; Optional internal workspaces.
   nil
   nil))

(defn local-sauvola-threshold
  "GrayU8 -> int -> boolean -> GrayU8
  Applies Sauvola thresholding algorithm to the input image. k is a tuning
  parameter whose default value is 0.3.
  Refs.: https://goo.gl/hhyGyc, https://goo.gl/1iRba2"
  [img radius down1? & [k]]
  (GThresholdImageOps/localSauvola img nil radius (or k 0.3) down1?))
