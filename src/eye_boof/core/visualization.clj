(ns eye-boof.core.visualization
  "Provides functions to visualize images using JFrame."
  (:import boofcv.gui.image.ShowImages))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol ViewImageWindow
  (show-image [img] "Shows an image on a JFrame window."))

(extend-protocol ViewImageWindow
  java.awt.image.BufferedImage
  (show-image [img]
    (ShowImages/showWindow img "Image")))
