(ns eye-boof.io
  "Provides functions to import resources as Images and to export Images as files in
  the disk or as BufferedImages."
  (:require
    [eye-boof.core :refer [width height]]
    [clojure.java.io :as io :only [as-file file]]
    )
  (:import 
    [javax.imageio ImageIO]
    [java.awt.image BufferedImage]
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]
    [boofcv.core.image ConvertBufferedImage]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;; Used the Mikera's implementation for imagez
;; https://github.com/mikera/imagez/blob/develop/src/main/clojure/mikera/image/protocols.clj
(defprotocol PImageResource
  "Coerce different image resource representations to BufferedImage."
  (resource->buff-image [r] "Returns a BufferedImage from a given resource."))

(extend-protocol PImageResource 
  String
  (resource->buff-image [r] (ImageIO/read (io/as-file r)))
  
  java.io.File
  (resource->buff-image [r] (ImageIO/read r))
  
  java.io.InputStream
  (resource->buff-image [r] (ImageIO/read r))
  
  java.net.URL
  (resource->buff-image [r] (ImageIO/read r))
  
  java.awt.image.BufferedImage
  (resource->buff-image [r] r))

(defn load-image
  "Returns a MultiSpectral image, in RGB order, from the given resource. The resource
  can be a string, File, InputStream, URL or BufferedImage."
  [resource]
  (ConvertBufferedImage/convertFromMulti 
    ^BufferedImage (resource->buff-image resource)
    nil ; output image.
    true ; RGB ordering.
    ImageUInt8))

(defn to-buff-image
  "Returns a BufferedImage TYPE_INT_RGB from the given Image."
  [^ImageBase img]
  (ConvertBufferedImage/convertTo 
        img
        (BufferedImage. (width img) (height img) BufferedImage/TYPE_INT_RGB)
        true))

(defn save-image!
  "Saves an Image into a file in the disk. 
  Supported extensions: ImageIO.getWriterFormatNames()
  
  Example:
  (save-image! img \"path/to/image.ext\" :png)"
  [img path]
  (ImageIO/write ^BufferedImage (to-buff-image img)
                 (-> (re-find #"\.\w+$" path) (subs 1)) ; extension
                 (io/file path)))
