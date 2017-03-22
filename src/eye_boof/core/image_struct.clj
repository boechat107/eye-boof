(ns eye-boof.core.image-struct
  (:import [boofcv.struct.image ImageBase GrayU8 Planar]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol BasicImageHandlers
  (width [img] "Returns the image's width.")
  (height [img] "Returns the image's height.")
  (as-seq [img] "Returns the image's data as a Clojure sequence."))

(extend-protocol BasicImageHandlers
  ImageBase
  (width [img] (.getWidth img))
  (height [img] (.getHeight img))
  ;;
  GrayU8
  (as-seq [img]
    (map #(bit-and % 0xff) (.data img))))

;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Color image handlers ;;
;;;;;;;;;;;;;;;;;;;;;;;;;;

(defprotocol ColorImageHandlers
  (num-of-bands [pimg]
    "Returns the number of color channels of a Planar image.")
  (band! [pimg n]
    "Returns a GrayU8 image as a color channel of a Planar image. IMPORTANT:
modifying the returned image has the side effect of modifying the given
image."))

(extend-protocol ColorImageHandlers
  Planar
  (num-of-bands [pimg] (.getNumBands pimg))
  (band! [pimg n] (.getBand pimg (int n))))
