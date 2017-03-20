(ns eye-boof.core
  (:require [potemkin :refer [import-vars]]
            [eye-boof.core.io :as io]
            [eye-boof.core.image-struct :as im]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(import-vars [eye-boof.core.io
              ;;
              resource->buff-image
              load-image->gray-u8
              load-image->planar-u8]
             ;;
             [eye-boof.core.image-struct
              ;;
              width
              height
              as-seq
              num-of-bands
              band!])
