(ns eye-boof.core.visualization
  "Provides functions to visualize images using JFrame."
  (require [eye-boof.core.io :refer [resource->buff-image]])
  (:import boofcv.gui.image.ShowImages
           boofcv.gui.binary.VisualizeBinaryData
           boofcv.struct.image.ImageBase))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol ViewImageWindow
  (show-image [img] "Shows an image on a JFrame window."))

(extend-protocol ViewImageWindow
  java.awt.image.BufferedImage
  (show-image [img]
    (ShowImages/showWindow img "Image"))
  ;;
  ImageBase
  (show-image [img]
    (show-image (resource->buff-image img))))

(defn render-binary
  "GrayU8 -> boolean -> BufferedImage
  Returns a BufferedImage from a binary GrayU8, rendering 0 as black and 1 as
  white."
  [img & [invert]]
  (VisualizeBinaryData/renderBinary img
                                    (or invert false)
                                    nil))
