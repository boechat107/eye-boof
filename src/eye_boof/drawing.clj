(ns eye-boof.drawing
  (:require 
    [eye-boof 
     [features :as ft]
     [helpers :as h :only [to-buffered-image]]
     ]
    )
  (:import 
    [boofcv.gui.feature VisualizeShapes]
    [java.awt Graphics2D BasicStroke Color]
    [java.awt.image BufferedImage]
    )
  )

(defn bounding-box 
  "Returns a BufferedImage where the list of blobs are surrounded by rectangles on
  the given Image."
  ([img blobs] (bounding-box img nil blobs))
  ([img [r g b :as color] blobs]
   (let [buff (h/to-buffered-image img)
         graph (.createGraphics buff)
         c (when color (Color. r g b))]
     (.setStroke graph (BasicStroke. 2))
     (when color (.setColor graph c))
     (doseq [b blobs]
       (when (nil? color) 
         (.setColor graph (Color. (rand-int 256) (rand-int 256) (rand-int 256))))
       (let [[tl br] (ft/bounding-box b)]
         (VisualizeShapes/drawPolygon 
           (list tl (ft/make-2d-point (ft/x br) (ft/y tl))
                 br (ft/make-2d-point (ft/x tl) (ft/y br)))
           true 
           graph)))
     buff)))
