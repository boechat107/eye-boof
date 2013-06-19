(ns eye-boof.helpers
  (:require 
    [eye-boof.core :as c]
    [eye-boof.processing :as pr]
    [seesaw.core :as w]
    )
  (:import 
    [java.io File]
    [javax.imageio ImageIO]
    [java.awt.image BufferedImage]
    [boofcv.struct.image ImageBase ImageUInt8 MultiSpectral]
    [boofcv.core.image ConvertBufferedImage]
    )
  )

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

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

(defn load-file-image
  "Returns a RGB Image from a file image."
  [^String filepath]
  (let [buff (ImageIO/read (File. filepath))
        nc (.getWidth buff)
        img (c/new-image (.getHeight buff) nc :rgb)
        rch (c/get-channel img 0)
        gch (c/get-channel img 1)
        bch (c/get-channel img 2)]
;    (assert (== BufferedImage/TYPE_4BYTE_ABGR (.getType buff)))
    (c/for-xy
        [x y img]
        (let [pix (.getRGB buff x y)
              i (+ x (* y nc))]
          (c/set-pixel! rch i (r<-intcolor pix))
          (c/set-pixel! gch i (g<-intcolor pix))
          (c/set-pixel! bch i (b<-intcolor pix))
          )
        )
    img
    ))

(defn to-buffered-image
  "Converts an ARGB Image to a BufferedImage."
  ;; fixme: The resultant image is not good, perhaps because of the channels ordering.
  ^BufferedImage [img]
  {:pre [(c/image? img)]}
  (let [b ^ImageBase (:mat img)]
    (if (> (c/dimension img) 1)
      (ConvertBufferedImage/convertTo_U8 b nil)
      (ConvertBufferedImage/convertTo b nil))))

(defn save-to-file!
  "Saves an image into a file."
  ([img filepath] (save-to-file! img filepath "png"))
  ([img ^String filepath ^String ext]
   (-> (to-buffered-image img)
       (ImageIO/write ext (File. filepath)))))

(defn view 
  "Shows the images on a grid-panel window."
  [& imgs]
  (let [buff-imgs (map #(if (instance? java.awt.image.BufferedImage %)
                          %
                          (to-buffered-image %))
                       imgs)
        grid (w/grid-panel
               :border 5
               :hgap 10 :vgap 10
               :columns (min 6 (max 1 (count imgs))) 
               :items (map #(w/label :icon %) buff-imgs))]
    (-> (w/frame :title "Image Viewer" 
                 :content grid)
        w/pack!
        w/show!)))
