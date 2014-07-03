(ns eye-boof.drawing
  (:require [eye-boof.features :as ft]
            [eye-boof.helpers :as h])
  (:import (boofcv.gui.feature VisualizeShapes)
           (java.awt BasicStroke Color)
           (java.awt.image BufferedImage)))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

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

(defn draw-polygons
  "Returns an image where the given polygons is painted on the image with the [r g b]
  color. If the end points are connected, closed? must be true.
  Each polygon is a sequence of Point2D_I32 and, if it has a loop, they are
  considered in counterclockwise."
  [img color closed? & polys]
  (let [buff (h/to-buffered-image img)
        graph (.createGraphics buff)
        [^int r ^int g ^int b] color
        c (Color. r g b)]
    (.setStroke graph (BasicStroke. 2))
    (.setColor graph (Color. r g b))
    (doseq [p polys]
      (VisualizeShapes/drawPolygon p closed? graph))
    (h/to-img buff)))
