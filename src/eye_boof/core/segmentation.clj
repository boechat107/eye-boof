(ns eye-boof.core.segmentation
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
  "Global threshold with the value calculated from f(img). If down1? is true,
  pixel values <= f(img) are set to 1."
  [^GrayU8 img f down1?]
  (threshold img ^long (f img) down1?))

(defn otsu-threshold
  "Computes the variance based threshold using the Otsu's method."
  [img min-val max-val]
  (GThresholdImageOps/computeOtsu ^GrayU8 img (int min-val) (int max-val)))

(defn entropy-threshold
  "Computes a threshold which maximizes the entropy between the foreground and
  background regions."
  [img min-val max-val]
  (GThresholdImageOps/computeEntropy ^GrayU8 img (int min-val) (int max-val)))
