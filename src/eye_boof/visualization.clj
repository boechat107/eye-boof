(ns eye-boof.visualization
  "Provides functions to visualize images using JFrame."
  (require [eye-boof.core.io :refer [resource->buff-image]])
  (:import boofcv.gui.image.ShowImages
           boofcv.gui.ListDisplayPanel
           boofcv.gui.binary.VisualizeBinaryData
           boofcv.struct.image.ImageBase))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol ViewImageWindow
  (show-image [img]
    "Shows an image on a JFrame window. If a collection of images is passed, a
list panel is generated."))

(extend-protocol ViewImageWindow
  java.awt.image.BufferedImage
  (show-image [img]
    (ShowImages/showWindow img "Image"))
  ;;
  ImageBase
  (show-image [img]
    (show-image (resource->buff-image img)))
  ;;
  clojure.lang.IPersistentCollection
  (show-image [imgs]
    (ShowImages/showWindow
     ^ListDisplayPanel (reduce (fn [^ListDisplayPanel ldp ^ImageBase i]
                                 (.addImage ^ListDisplayPanel ldp i "Image")
                                 ldp)
                               (ListDisplayPanel.)
                               imgs)
     "Images")))

(defn show-grid
  "Shows images in a grid pattern."
  [ncols & imgs]
  (ShowImages/showGrid
   ncols "Images" (into-array (map resource->buff-image imgs))))

(defn render-binary
  "GrayU8 -> boolean -> BufferedImage
  Returns a BufferedImage from a binary GrayU8, rendering 0 as black and 1 as
  white."
  [img & [invert]]
  (VisualizeBinaryData/renderBinary img
                                    (or invert false)
                                    nil))
