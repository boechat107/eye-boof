(ns eye-boof.visualize
  (:require 
    [eye-boof.helpers :as h]
    [seesaw.core :as w]))

(defn- new-frame
  "Creates a new frame for viewing the images."
  []
  (w/frame :title "Image Viewer" ))

(defonce frame (atom (new-frame)))

(defn view 
  "Shows the images on a grid-panel window."
  [& imgs]
  (let [buff-imgs (map #(if (instance? java.awt.image.BufferedImage %)
                          %
                          (h/to-buffered-image %))
                       imgs)
        n-imgs (count imgs)
        img-col 6 
        grid (w/grid-panel
               :border 5
               :hgap 10 :vgap 10
               :columns (min img-col n-imgs)
               :items (map #(w/label :icon %) buff-imgs))]
    (doto @frame
      (.setContentPane grid)
      w/pack!
      w/show!)))

(defn view*
  "View the images in a new frame"
  [& imgs]
  (reset! frame (new-frame))
  (apply view imgs))

(defn view-new [& imgs]
  (println "'view-new' is depreacted, please start using 'view*'")
  (apply view* imgs))