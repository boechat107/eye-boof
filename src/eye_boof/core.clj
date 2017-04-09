(ns eye-boof.core
  (:require [potemkin :refer [import-vars]]
            [eye-boof.core.io :as io]
            [eye-boof.core.image-struct :as im]
            [eye-boof.core.segmentation :as seg]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(import-vars [eye-boof.core.io
              ;;
              resource->buff-image
              load-image->gray-u8
              load-image->planar-u8
              save-image!]
             ;;
             [eye-boof.core.image-struct
              ;;
              width
              height
              as-seq
              get-pixel
              set-pixel!
              num-of-bands
              band!
              sub-image
              sub-image!
              sub-image?]
             [eye-boof.core.segmentation
              ;;
              threshold
              threshold-f
              otsu-threshold
              entropy-threshold
              local-square-threshold
              local-sauvola-threshold])
