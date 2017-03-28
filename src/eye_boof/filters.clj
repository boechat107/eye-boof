(ns eye-boof.filters
  (:import boofcv.alg.filter.blur.BlurImageOps
           [boofcv.struct.image GrayU8 Planar]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol BlurOps
  (gaussian-blur [img sigma radius]
    "Image -> double -> int -> Image
  Applies a Gaussian blur. If sigma <= 0, it's chosen based on radius. If
  radius <= 0, it's selected based on sigma.")
  (mean-blur [img radius]
    "Image -> int -> Image
     Applies a mean box filter.")
  (median-blur [img radius]
    "Image -> int -> Image
     Applies median filter."))

(defmacro impl-protocol
  [protocol & body]
  `(extend-protocol ~protocol
     ;;
     GrayU8
     ~@body
     Planar
     ~@body))

(impl-protocol BlurOps
 (gaussian-blur [img sigma radius]
                (BlurImageOps/gaussian img nil (double sigma) (int radius) nil))
 (mean-blur [img radius]
            (BlurImageOps/mean img nil (int radius) nil))
 (median-blur [img radius]
              (BlurImageOps/median img nil (int radius))))
