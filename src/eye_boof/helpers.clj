(ns eye-boof.helpers
  (:require 
    [eye-boof.core :as c]
    )
  (:import 
    [java.io File]
    [javax.imageio ImageIO]
    [java.awt.image BufferedImage]
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]
    [boofcv.core.image ConvertBufferedImage]
    [boofcv.gui.binary VisualizeBinaryData]
    [eye_boof.core Image]
    )
  )

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn is-buffImg?
  [obj]
  (instance? BufferedImage obj))

(defn argb<-intcolor
  "Convert the 32 bits color to ARGB. It returns a vector [a r g b]."
  [color]
  (vector 
    (bit-and (bit-shift-right color 24) 0xff) 
    (bit-and (bit-shift-right color 16) 0xff) 
    (bit-and (bit-shift-right color 8) 0xff) 
    (bit-and color 0xff)))

(defn r<-intcolor 
  "Returns the red value from a ARGB integer."
  ^long [^long color]
  (bit-and (bit-shift-right color 16) 0xff))

(defn g<-intcolor 
  "Returns the green value from a ARGB integer."
  ^long [^long color]
  (bit-and (bit-shift-right color 8) 0xff))

(defn b<-intcolor 
  "Returns the blue value from a ARGB integer."
  ^long [^long color]
  (bit-and color 0xff))

(defn intcolor<-argb
  "Converts the components ARGB to a 32 bits integer color."
  [a r g b]
  (bit-or (bit-shift-left (int a) 24)
          (bit-or (bit-shift-left (int r) 16)
                  (bit-or (bit-shift-left (int g) 8) (int b)))))

(defn get-raster-array
  "Returns the primitive array of a BufferedImage."
  [^BufferedImage buff]
  (.getDataElements (.getRaster buff) 0 0 (.getWidth buff) (.getHeight buff) nil))

(defn load-file-buffImg
  ^BufferedImage [^String filepath]
  (ImageIO/read (File. filepath)))

;;source
;;http://docs.oracle.com/javase/7/docs/api/java/awt/image/BufferedImage.html
(defn to-img
  "Retuns an eye-boof Image from a given BufferedImage."
  [^BufferedImage buff]
  (condp contains? (.getType buff)
    #{BufferedImage/TYPE_4BYTE_ABGR, BufferedImage/TYPE_INT_ARGB, BufferedImage/TYPE_4BYTE_ABGR_PRE BufferedImage/TYPE_INT_ARGB_PRE}
    (let [img (ConvertBufferedImage/convertFromMulti buff nil ImageUInt8)]
      (ConvertBufferedImage/orderBandsIntoRGB img buff)
      (c/make-image img :argb))
    ;;;;
    #{BufferedImage/TYPE_INT_RGB, BufferedImage/TYPE_3BYTE_BGR
      BufferedImage/TYPE_INT_BGR}
    (let [img (ConvertBufferedImage/convertFromMulti buff nil ImageUInt8)]
      (ConvertBufferedImage/orderBandsIntoRGB img buff)
      (c/make-image img :rgb))
        ;;;;
    #{BufferedImage/TYPE_BYTE_GRAY}
    (let [img (ConvertBufferedImage/convertFromSingle buff nil ImageUInt8)]
      (c/make-image img :gray))

    #{BufferedImage/TYPE_BYTE_BINARY}
    (let [img (ConvertBufferedImage/convertFromSingle buff nil ImageUInt8)]
      (c/make-image img :bw))
    ))

(defn load-file-image
  "Returns a RGB Image from a file image."
  [^String filepath]
  (-> (load-file-buffImg filepath)
      (to-img)))

(defn to-buffered-image
  "Converts an Image to a BufferedImage.
  Note that if img is a bw image, the output is a BufferedImage where 1s in img are
  255."
  ^BufferedImage [img]
  (let [^ImageBase b (:mat img)
        w (c/ncols img)
        h (c/nrows img)]
    (case (c/get-type img)
      :argb
      (throw (Exception.  "Not implemented yet in boofcV"))
      :rgb
      (ConvertBufferedImage/convertTo_U8 b (create-buffered-image w h  BufferedImage/TYPE_3BYTE_BGR))
      :gray
      (ConvertBufferedImage/convertTo b (create-buffered-image w h BufferedImage/TYPE_BYTE_GRAY))
      :bw
      (VisualizeBinaryData/renderBinary b (create-buffered-image w h BufferedImage/TYPE_BYTE_BINARY)))))

(defn create-buffered-image
  (^BufferedImage [width height] 
   (create-buffered-image width height BufferedImage/TYPE_INT_RGB))
  (^BufferedImage [width height c-type]
   (BufferedImage. width height c-type)))

(defn save-to-file!
  "Saves an image into a file. The default extension is PNG."
  ([img filepath] (save-to-file! img filepath "png"))
  ([img ^String filepath ^String ext]
   (-> (to-buffered-image img)
       (ImageIO/write ext (File. filepath)))))

(def buff-img-types
  ['BufferedImage/TYPE_CUSTOM
  'BufferedImage/TYPE_INT_RGB
  'BufferedImage/TYPE_INT_ARGB
  'BufferedImage/TYPE_INT_ARGB_PRE
  'BufferedImage/TYPE_INT_BGR
  'BufferedImage/TYPE_3BYTE_BGR
  'BufferedImage/TYPE_4BYTE_ABGR
  'BufferedImage/TYPE_4BYTE_ABGR_PRE
  'BufferedImage/TYPE_USHORT_565_RGB
  'BufferedImage/TYPE_USHORT_555_RGB
  'BufferedImage/TYPE_BYTE_GRAY
  'BufferedImage/TYPE_USHORT_GRAY
  'BufferedImage/TYPE_BYTE_BINARY
  'BufferedImage/TYPE_BYTE_INDEXED])
