(ns eye-boof.core.visualization
  (:import boofcv.gui.image.ShowImages))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol ViewImageWindow
  (show-image [img]))

(extend-protocol ViewImageWindow
  java.awt.image.BufferedImage
  (show-image [img]
    (ShowImages/showWindow img "Image")))
