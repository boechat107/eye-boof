(ns eye-boof.core
  (:require [potemkin :refer [import-vars]]
            [eye-boof.core.io :as io]
            [eye-boof.core.visualization :as v])
  (:import [boofcv.struct.image GrayU8]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(import-vars [eye-boof.core.io
              ;;
              resource->buff-image
              load-image->gray-u8]
             ;;
             [eye-boof.core.visualization
              ;;
              show-image])

(defprotocol BasicImageHandlers
  (as-seq [img] "Returns the image's data as a Clojure sequence."))

(extend-protocol BasicImageHandlers
  GrayU8
  (as-seq [img]
    (map #(bit-and % 0xff) (.data img))))
