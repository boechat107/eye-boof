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
     Applies a mean box filter."))

(defmacro impl-protocol
  [& body]
  `(extend-protocol BlurOps
     ;;
     GrayU8
     ~@body
     Planar
     ~@body))

(impl-protocol
 (gaussian-blur [img sigma radius]
                (BlurImageOps/gaussian img nil (double sigma) (int radius) nil))
 (mean-blur [img radius]
            (BlurImageOps/mean img nil (int radius) nil)))
