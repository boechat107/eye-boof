(ns eye-boof.core.image-struct
  (:import [boofcv.struct.image GrayU8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol BasicImageHandlers
  (as-seq [img] "Returns the image's data as a Clojure sequence."))

(extend-protocol BasicImageHandlers
  GrayU8
  (as-seq [img]
    (map #(bit-and % 0xff) (.data img))))
