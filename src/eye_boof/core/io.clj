(ns eye-boof.core.io
  "Provides functions to load images from different resources and to save them
  into files."
  (:import boofcv.io.image.UtilImageIO
           boofcv.io.image.ConvertBufferedImage
           [boofcv.struct.image ImageBase GrayU8 Planar]
           [javax.imageio ImageIO]
           [java.awt.image BufferedImage]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defprotocol ToBufferedImage
  "Coerce different image resource representations to BufferedImage.
  BufferedImage is usually an intermediary file type to load images from
  different resources."
  (resource->buff-image [r]
    "Returns a BufferedImage from a given resource.")
  (boofcv->buff-image [img]
    "Returns a BufferedImage from a BoofCV image format."))

(extend-protocol ToBufferedImage
  String
  (resource->buff-image [r] (UtilImageIO/loadImage r))
  ;;
  java.io.File
  (resource->buff-image [r] (UtilImageIO/loadImage (.getAbsolutePath r)))
  ;;
  java.io.InputStream
  (resource->buff-image [r] (ImageIO/read r))
  ;;
  java.net.URL
  (resource->buff-image [r] (UtilImageIO/loadImage r))
  ;;
  java.awt.image.BufferedImage
  (resource->buff-image [r] r)
  ;; TODO: BoofCV images to BufferedImage.
  )

(defn load-image->gray-u8
  "Returns a BoofCV GrayU8 image from a given resource, which can be a String
  (file path), File, InputStream or URL. If the resource represents a color
  image, the intensities of each channel are averaged together."
  [resource]
  (ConvertBufferedImage/convertFromSingle
   (resource->buff-image resource)
   nil ; an existent BoofCV image used for mutation.
   GrayU8))

(defn load-image->planar-u8
  "Returns a BoofCV Planar<GrayU8> image from a given resource, which can be a
  String (file path), File, InputStream or URL."
  [resource]
  (ConvertBufferedImage/convertFromMulti
   ^BufferedImage (resource->buff-image resource)
   nil
   true
   GrayU8))

(defprotocol ImageDiskPersistence
  "Persistence of image data as files."
  (save-image! [img filepath] "Saves the given image representation."))

(extend-protocol ImageDiskPersistence
  ImageBase
  (save-image! [img ^String filepath]
    (UtilImageIO/saveImage img filepath))
  ;;
  BufferedImage
  (save-image! [img ^String filepath]
    (UtilImageIO/saveImage img filepath)))
