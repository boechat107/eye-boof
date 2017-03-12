(ns eye-boof.core.io
  "Provides functions to load images from different resources and to save them
  into files."
  (:import boofcv.io.image.UtilImageIO
           boofcv.io.image.ConvertBufferedImage
           boofcv.struct.image.GrayU8
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
  (file path), File, InputStream or URL."
  [resource]
  (ConvertBufferedImage/convertFromSingle
   (resource->buff-image resource)
   nil ; an existent BoofCV image used for mutation.
   GrayU8))

#_(defn load-image
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

(defprotocol ImageDiskPersistence
  (save-image! [img] "Saves the given image representation."))

#_(defn save-image!
  "Saves an Image into a file in the disk.
  Supported extensions: ImageIO.getWriterFormatNames()

  Example:
  (save-image! img \"path/to/image.png\")"
  [img path]
  (ImageIO/write ^BufferedImage (image->buff-image img)
                 (-> (re-find #"\.\w+$" path) (subs 1)) ; extension
                 (io/file path)))
