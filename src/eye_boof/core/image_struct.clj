(ns eye-boof.core.image-struct
  (:import [boofcv.struct.image ImageBase GrayU8 Planar]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol BasicImageHandlers
  (as-seq [img] "Returns the image's data as a Clojure sequence."))

(extend-protocol BasicImageHandlers
  ;;
  GrayU8
  (as-seq [img]
    (map #(bit-and % 0xff) (.data img))))

(defn width
  "Returns the image's width."
  [img]
  (.getWidth ^ImageBase img))

(defn height
  "Returns the image's height."
  [img]
  (.getHeight ^ImageBase img))

(defn get-pixel
  "GrayU8 -> int -> int -> int
  Returns the intensity value of a pixel."
  [img x y]
  (.unsafe_get ^GrayU8 img x y))

(defn set-pixel!
  "GrayU8 -> int -> int -> int -> GrayU8
  Mutates the given image by setting a new value for the given pixel."
  [img x y intensity]
  (.unsafe_set ^GrayU8 img x y intensity)
  img)

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
