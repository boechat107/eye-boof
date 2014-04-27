(ns eye-boof.io
  "Provides functions to import resources as Images and to export Images as files in
  the disk or as BufferedImages."
  (:require
    [eye-boof.core :refer [width height]]
    [clojure.java.io :as io :only [as-file file]])
  (:import 
    [javax.imageio ImageIO]
    [java.awt.image BufferedImage]
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]
    [boofcv.core.image ConvertBufferedImage]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

;; Used the Mikera's implementation for imagez
;; https://github.com/mikera/imagez/blob/develop/src/main/clojure/mikera/image/protocols.clj
(defprotocol PToBufferedImage
  "Coerce different image resource representations to BufferedImage."
  (resource->buff-image [r] "Returns a BufferedImage from a given resource.")
  (image->buff-image [img] 
                     "Returns a BufferedImage from an ImageUInt8 or MultiSpectral."))

(extend-protocol PToBufferedImage 
  String
  (resource->buff-image [r] (ImageIO/read (io/as-file r)))
  
  java.io.File
  (resource->buff-image [r] (ImageIO/read r))
  
  java.io.InputStream
  (resource->buff-image [r] (ImageIO/read r))
  
  java.net.URL
  (resource->buff-image [r] (ImageIO/read r))
  
  java.awt.image.BufferedImage
  (resource->buff-image [r] r)
  
  boofcv.struct.image.ImageUInt8
  (image->buff-image [img]
    (ConvertBufferedImage/convertTo 
      img 
      (BufferedImage. (width img) (height img) BufferedImage/TYPE_BYTE_GRAY)))
  
  boofcv.struct.image.MultiSpectral
  (image->buff-image [img]
    ;; TODO: It works only for images with 3 channels for now.
    (ConvertBufferedImage/convertTo_U8
      img
      (BufferedImage. (width img) (height img) BufferedImage/TYPE_INT_RGB)
      true)))

(defn load-image
  "Returns a MultiSpectral image, in RGB order, from the given resource. The resource
  can be a string, File, InputStream, URL or BufferedImage."
  [resource]
  (let [^BufferedImage buff-image (resource->buff-image resource)]
    (condp contains? (.getType buff-image)
      #{BufferedImage/TYPE_4BYTE_ABGR, BufferedImage/TYPE_INT_ARGB,
        BufferedImage/TYPE_4BYTE_ABGR_PRE BufferedImage/TYPE_INT_ARGB_PRE,
        BufferedImage/TYPE_INT_RGB, BufferedImage/TYPE_3BYTE_BGR,
        BufferedImage/TYPE_INT_BGR}
      (ConvertBufferedImage/convertFromMulti buff-image nil true ImageUInt8)
      
      #{BufferedImage/TYPE_BYTE_GRAY BufferedImage/TYPE_BYTE_INDEXED
        BufferedImage/TYPE_BYTE_BINARY BufferedImage/TYPE_USHORT_GRAY}
      (ConvertBufferedImage/convertFromSingle buff-image nil ImageUInt8))))

(defn save-image!
  "Saves an Image into a file in the disk. 
  Supported extensions: ImageIO.getWriterFormatNames()
  
  Example:
  (save-image! img \"path/to/image.png\")"
  [img path]
  (ImageIO/write ^BufferedImage (image->buff-image img)
                 (-> (re-find #"\.\w+$" path) (subs 1)) ; extension
                 (io/file path)))
