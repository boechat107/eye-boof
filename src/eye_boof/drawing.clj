(ns eye-boof.drawing
  (:require 
    [eye-boof 
     [core :as c :only [make-image]]
     [features :as ft]
     [processing :as p :only [gray-to-rgb]]
     [helpers :as h :only [to-buffered-image]]
     [binary-ops :as bi :only [render-binary]]])
  (:import 
    [boofcv.gui.feature VisualizeShapes]
    [boofcv.alg.feature.shapes ShapeFittingOps]
    [java.awt Graphics2D BasicStroke Color]
    [java.awt.image BufferedImage]))

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
  "Returns an image where the given polygons (sequence of points) is painted on the
  image. The RGB color (vector) of the painting should be specified. If the end
  points are connected, closed? must be true."
  [img color closed? dist-tol ang-tol & polys]
  (let [buff (-> (bi/render-binary img) p/gray-to-rgb h/to-buffered-image)
        graph (.createGraphics buff)
        [^int r ^int g ^int b] color
        c (Color. r g b)]
    (.setStroke graph (BasicStroke. 2))
    (.setColor graph (Color. r g b))
    (doseq [poly polys]
      (-> (ShapeFittingOps/fitPolygon poly closed? dist-tol ang-tol 100)
          (VisualizeShapes/drawPolygon closed? graph)))
    (h/to-img buff)))
