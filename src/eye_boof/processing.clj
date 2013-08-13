(ns eye-boof.processing 
  (:require 
    [eye-boof.core :as c]
    [eye-boof.helpers :as h]
    [eye-boof.image-statistics :as stat]
    [eye-boof.matrices :as m]
    [eye-boof.binary-ops :as bi])
  (:import
    [boofcv.struct.image ImageBase ImageUInt8 ImageSInt16 ImageFloat32 MultiSpectral]
    [boofcv.alg.filter.blur BlurImageOps]
    [boofcv.alg.filter.derivative GradientSobel]
    [boofcv.alg.filter.binary ThresholdImageOps]
    [boofcv.alg.distort DistortImageOps]
    [boofcv.alg.interpolate TypeInterpolate]
    [boofcv.alg.enhance EnhanceImageOps]
    [boofcv.factory.feature.detect.edge FactoryEdgeDetectors]
    [boofcv.alg.feature.detect.edge CannyEdge]
    [boofcv.alg.misc PixelMath ImageStatistics]
    [java.awt.geom AffineTransform]
    [java.awt.image BufferedImage AffineTransformOp]))

(set! *warn-on-reflection* true)
(set! *unchecked-math* true)

(defn- mult-chs-ops
  "Template function to apply an operation on a multispectral image and return an
  image of the same type.
  f is a funtion whose arguments are a channel of the input image and a channel of
  the output image."
  [img f]
  (let [output (c/new-image (c/nrows img) (c/ncols img) (:type img))]
    (dotimes [ch (c/dimension img)]
      (let [ch-img (c/get-channel img ch)
            out-ch (c/get-channel output ch)]
        (f ch-img out-ch)))
    output))

(defmacro mult-ops
  [[img img-ch out-ch] & body]
  `(let [out# (c/new-image (c/nrows ~img) (c/ncols ~img) (:type ~img))]
     (dotimes [ch# (c/dimension ~img)]
       (let [~img-ch (c/get-channel ~img ch#)
             ~out-ch (c/get-channel out# ch#)]
         ~@body))
     out#))

(defn rgb-to-gray
  "Returns a new Image whose color space is the grayscale. Only ARGB or RGB images 
  are accepted.
  Reference:
  http://en.wikipedia.org/wiki/Grayscale"
  [img]
  {:pre [(contains? #{:rgb :argb} (:type img))]}
  (let [res (c/new-image (c/nrows img) (c/ncols img) :gray)
        gray (c/get-channel res) 
        chs-vec (c/get-channel img)
        [rch gch bch] (if (= :argb (:type img)) (subvec chs-vec 1) chs-vec)]
    (c/for-xy [x y img]
      (->> (* 0.2126 (c/get-pixel rch x y))
           (+ (* 0.7152 (c/get-pixel gch x y)))
           (+ (* 0.0722 (c/get-pixel bch x y)))
           (c/set-pixel! gray x y)))
    res))

(def to-gray rgb-to-gray)

(defn average-channels
  "Retuns a grayscale image where the pixels' value are the average of the channels
  of the given image."
  [img]
  {:pre [(> (c/dimension img) 1)]}
  (let [gray (c/new-channel-matrix (c/nrows img) (c/ncols img) 1)]
    (PixelMath/averageBand ^MultiSpectral (:mat img) ^ImageUInt8 gray)
    (c/make-image gray :gray)))

(defn gray-to-rgb
  "Repeats the only grayscale channel for each color channel and returns a new RGB
  Image."
  [img]
  {:pre [(= :gray (:type img))]}
  (let [nr (c/nrows img)
        nc (c/ncols img)
        gray (c/get-channel img 0)
        res (c/new-image nr nc :rgb)
        [rch gch bch] (c/get-channel res)]
    (c/for-idx [idx img]
      (let [p (c/get-pixel gray idx)]
        (c/set-pixel! rch idx p)
        (c/set-pixel! gch idx p)
        (c/set-pixel! bch idx p)))
    res))

(defn binarize
  "Returns a new Image where each pixel value of the its first channel is set to 0 or
  1. If the original pixel value is below the threshold, the value is set to 0;
  otherwise, the value is set to 1."
  [img ^long th]
  (let [nr (c/nrows img)
        nc (c/ncols img)
        res (c/new-image nr nc :bw)
        threshold (fn [^long n] (if (> n th) 1 0))
        img-m (c/get-channel img 0)
        res-m (c/get-channel res 0)]
    (c/for-xy [x y img]
      (->> (c/get-pixel img-m x y)
           threshold 
           (c/set-pixel! res-m x y)))
    res))

(defn mean-binarize
  "Like binarize, but uses the mean intensity value of the given image as threshold."
  [img]
  (let [out-ch (c/new-channel-matrix (c/nrows img) (c/ncols img) 1)
        img-ch (c/get-channel img 0)
        mean (stat/mean img)]
    (c/for-xy
      [x y img]
      (->> (if (< (c/get-pixel img-ch x y) mean) 0 1)
           (c/set-pixel! out-ch x y)))
    (c/make-image out-ch :bw)))

(defn threshold
  [img threshold & {:keys [down]}]
  (let [result (c/new-image (c/nrows img) (c/ncols img) :bw)
        img-ch (c/get-channel img 0)
        res-chan (c/get-channel result 1)]
    (ThresholdImageOps/threshold img-ch res-chan
                                 (int threshold) (boolean down))
    result))

(defn quantize
  "Quantizes the color information to n-levels."
  [img n-levels]
  (let [multp (/ 256 n-levels)]
    (mult-ops 
      [img ich och]
      (c/for-xy [x y img]
                (c/set-pixel! och x y
                              (-> (c/get-pixel ich x y)
                                  (quot multp)
                                  (* multp)))))))

(defn invert 
  "Inverts the value of each channel, i.e., pixels' values equal 255 become 0 and
  vice versa."
  [img]
  (let [out-img (c/new-image (c/nrows img) (c/ncols img) (:type img))]
    (dotimes [ch (c/dimension img)]
      (let [out-ch (c/get-channel out-img ch)
            img-ch (c/get-channel img ch)]
        (c/for-xy
          [x y img]
          (->> (c/get-pixel img-ch x y)
               (- 255)
               (c/set-pixel! out-ch x y)))))
    out-img))

(defn convolve
  "Applies a convolution mask over an image.
  mask is an one dimensional collection or array with (* mask-size mask-size)
  elements."
  ;; todo: Write a macro for the loops over the mask elements. 
  [img mask ^long mask-size]
  (let [mask (if (c/doubles? mask) mask (into-array Double/TYPE mask))
        nc (c/ncols img)
        nr (c/nrows img)
        res (c/new-image nr nc (:type img))
        offset (long (/ mask-size 2))]
    (dotimes [ch (c/dimension img)]
      (let [res-m (c/get-channel res ch)
            img-m (c/get-channel img ch)]
        (c/for-xy 
          [x y img]
          (loop [xn (long 0), kv (double 0.0)]
            ;; Loop over the x values of the mask.
            (if (< xn mask-size)
              (recur 
                (inc xn)
                (+ kv
                   (loop [yn (long 0), kyv (double 0.0)]
                     ;; Loop over the y values of the mask.
                     (if (< yn mask-size)
                       (recur (inc yn)
                              (->> (c/get-pixel
                                     img-m 
                                     ;; Ensures that the coordinates are out of the
                                     ;; image's bounds.
                                     (-> (+ xn (- x offset))
                                         (max 0)
                                         (min (dec nc)))
                                     (-> (+ yn (- y offset))
                                         (max 0)
                                         (min (dec nr))))
                                   (* (m/mult-aget doubles mask (+ xn (* yn mask-size))))
                                   (+ kyv)))
                       kyv))))
              (c/set-pixel! res-m
                            (+ x (* y nc))
                            (-> (max 0.0 kv) (min 255.0))))))))
    res))

(defn mean-blur
  "Returns a new image resulting from the application of a mean filter with a given
  radius."
  [img ^long rad]
  (mult-ops [img img-ch out-ch]
            (BlurImageOps/mean img-ch out-ch rad nil)))

(defn median-blur
  "Returns a new image resulting from the application of a median filter with a given
  radius."
  [img ^long rad]
  (mult-ops [img img-ch out-ch]
            (BlurImageOps/median img-ch out-ch rad)))

(defn gaussian-blur
  "Returns a new image as an output of a gaussian blur.
  http://boofcv.org/javadoc/boofcv/alg/filter/blur/BlurImageOps.html#gaussian(boofcv.struct.image.ImageUInt8, boofcv.struct.image.ImageUInt8, double, int, boofcv.struct.image.ImageUInt8)"
  [img ^double sigma ^long radius]
  (mult-ops [img img-ch out-ch]
            (BlurImageOps/gaussian img-ch out-ch sigma radius nil)))

(defn sharpen-4
  "Applies a Laplacian-4 based sharpen filter to the image and returns a new one."
  [img]
  (mult-ops [img ich och]
            (EnhanceImageOps/sharpen4 ich och)))

(defn sharpen-8
  "Applies a Laplacian-8 based sharpen filter to the image and returns a new one."
  [img]
  (mult-ops [img ich och]
            (EnhanceImageOps/sharpen8 ich och)))

(defn eq-local-histogram
  "Equalizes the local image histogram on a per pixel basis.
  rad is the radius of square local histogram."
  [img ^long rad]
  (mult-ops 
    [img ich och]
    (EnhanceImageOps/equalizeLocal ich rad och (int-array 256) (int-array 256))))

(defn eq-histogram
  "Equalizes the histogram of a given image."
  [img]
  (mult-ops 
    [img ich och]
    (let [transf (int-array 256)]
      (EnhanceImageOps/equalize (int-array (stat/histogram* ich 256))
                                transf)
      (EnhanceImageOps/applyTransform ich transf och))))

(defn canny-edge
  "Returns a binary image where edges are represented as 1 and the rest of pixels
  are zeros.
  blur-int      Blur intensity (suggested 2)
  thr-low       Lower threshold value (suggested 0.1)
  thr-high      Higher threshold value (suggested 0.3)
  http://boofcv.org/index.php?title=Example_Canny_Edge"
  [img blur-int thr-low thr-high]
  (let [img (if (> (c/dimension img) 1) (to-gray img) img)
        nr (c/nrows img)
        nc (c/ncols img)
        res (c/new-image nr nc :bw)
        canny (FactoryEdgeDetectors/canny blur-int true true ImageUInt8 ImageSInt16)]
    (.process canny (:mat img) thr-low thr-high (:mat res))
    res))

(defn sobel-edges*
  "Returns the X and Y Sobel derivatives as a vector of ImageSInt16."
  [img]
  (let [img (if (> (c/dimension img) 1) (to-gray img) img)
        w (c/ncols img)
        h (c/nrows img)
        dx (ImageSInt16. w h)
        dy (ImageSInt16. w h)]
    (GradientSobel/process (c/get-channel img 0) dx dy nil)
    [dx dy]))

(defn sobel-edges 
  "Returns the X and Y Sobel derivatives as a vector of images. The signed values 
  were scaled to [0, 255]."
  [img]
  (let [[dx dy] (sobel-edges* img)
        w (c/ncols img)
        h (c/nrows img)
        sx (c/new-channel-matrix h w 1)
        sy (c/new-channel-matrix h w 1)
        scaler! (fn [^ImageSInt16 mat ^ImageUInt8 out]
                  (let [minv (ImageStatistics/min mat)
                        _ (c/for-xy 
                            [x y img]
                            (->> (- (m/mget :sint16 mat x y) minv)
                                 (c/set-pixel! out x y)))
                        maxv (ImageStatistics/max out)]
                    (c/for-xy 
                      [x y img]
                      (->> (/ (c/get-pixel out x y) maxv)
                             (* 255)
                             (c/set-pixel! out x y)))
                    (c/make-image out :gray)))]
    [(scaler! dx sx) (scaler! dy sy)]))

(defn sobel-edge
  "Returns a grayscale image where the X Sobel derivative and Y Sobel derivative are 
  combined like (sqrt (+ dx^2 dy^2)). The edges of the picture can be seen as white."
  [img]
  (let [img (if (> (c/dimension img) 1) (to-gray img) img)
        w (c/ncols img)
        h (c/nrows img)
        out-ch (c/new-channel-matrix h w 1)
        dx (ImageSInt16. w h)
        dy (ImageSInt16. w h)
        sq (fn ^long [^long n] (* n n))]
    (GradientSobel/process (c/get-channel img 0) dx dy nil)
    (c/for-xy 
      [x y img]
      (->> (Math/sqrt (+ (sq (m/mget :sint16 dx x y))
                         (sq (m/mget :sint16 dy x y))))
           (c/set-pixel! out-ch x y)))
    (c/make-image out-ch :gray)))

(defn scale-buffImg
  "Returns a new bufferedImage as a scaled version of the input bufferedImage."
  ([buffImg factor] (scale-buffImg buffImg factor factor))
  ([^BufferedImage buffImg xfactor yfactor]
   (let [out-buff (h/create-buffered-image (* (.getWidth buffImg) xfactor)
                                           (* (.getHeight buffImg) yfactor)
                                           (.getType buffImg))]
     (-> (AffineTransformOp. (doto (AffineTransform.) (.scale xfactor yfactor))
                             AffineTransformOp/TYPE_BICUBIC)
         (.filter buffImg out-buff)))))

(defn scale
  "Returns a new image as a scaled version of the input image."
  ([img factor] (scale img factor factor))
  ([img xfactor yfactor]
   (let [out (c/new-image (* yfactor (c/nrows img))
                          (* xfactor (c/ncols img)) 
                          (:type img))
         s-type (TypeInterpolate/valueOf "BICUBIC")]
     (if (= :bw (:type img))
       ;; Binary images suffer of numerical errors. Therefore img is converted to
       ;; gray, scaled and then binarized again.
       (let [img-gray (bi/render-binary img)]
         (DistortImageOps/scale (:mat img-gray) (:mat out) s-type)
         (binarize out 100))
       (do (DistortImageOps/scale (:mat img) (:mat out) s-type)
           out)))))

;;(TODO) try and converge to buff-img scaling
(defn scale-with-buff
  "Returns a new image as a scaled version of the input image."
  ([img factor] (scale-with-buff img factor factor))
  ([img xfactor yfactor]
   (let [buff (h/to-buffered-image img)]
     (-> (scale-buffImg buff xfactor yfactor)
         (h/to-img)))))

(defn rotate 
  "Rotates the image around its center."
  [img ang]
  (let [out (c/new-image (c/nrows img) (c/ncols img) (:type img))
        s-type (TypeInterpolate/valueOf "BICUBIC")]
    (if (= :bw (:type img))
      ;; Binary images suffer of numerical errors. Therefore img is converted to
      ;; gray, scaled and then binarized again.
      (let [img-gray (bi/render-binary img)]
        (DistortImageOps/rotate (:mat img-gray) (:mat out) s-type ang)
        (binarize out 100))
      (do (DistortImageOps/rotate (:mat img) (:mat out) s-type ang)
          out))))

; (defn erode
;   "Erodes a Image, a basic operation in the area of the mathematical morphology.
;    http://homepages.inf.ed.ac.uk/rbf/HIPR2/erode.htm
;    The corner and edge values of the mask can be specified. The default values are 0.2."
;    ([img] (erode img 0.2 0.2))
;    ([img corner edge]
;     {:pre [(c/gray-type? img)]}
;     (let [mask [corner  edge    corner
;                 edge    1.0     edge
;                 corner  edge    corner]]
;       (convolve img mask 3))))
; 
; (defn small-blur 
;   "Blurs an image by taking the average of 4 neighbour pixels. The default mask is
;   [0    0.2   0
;    0.2  0.2   0.2
;    0    0.2   0]
;   http://lodev.org/cgtutor/filtering.html"
;   ([img] (small-blur img 0.2))
;   ([img factor]
;    (let [mask [0 factor 0
;                factor factor factor
;                0 factor 0]]
;      (convolve img mask 3))))
; 
; (defn big-blur 
;   "Like small-blur, but with a 5x5 mask.
;   http://lodev.org/cgtutor/filtering.html"
;   ([img] (big-blur img (/ 1 13)))
;   ([img factor]
;    (let [factor (double factor)
;          mask [0 0 factor 0 0
;                0 factor factor factor 0
;                factor factor factor factor factor
;                0 factor factor factor 0
;                0 0 factor 0 0]]
;      (convolve img mask 5))))
; 
; ;
; ;(defn smoothing
; ;  "Returns a new Image resulting of the application of a edge preserving smoothing on
; ;  the given Image.
; ;  -----------
; ;  Reference: 
; ;  Nikolaou, N., & Papamarkos, N. (2009). Color reduction for complex document images.
; ;  International Journal of Imaging Systems and Technology, 19(1), 14–26.
; ;  doi:10.1002/ima.20174"
; ;  [img]
; ;  {:pre [(c/color-type? img)]}
; ;  (letfn [;; Calculates the coefficient value for a pixel.
; ;          (coef [c1 c2] 
; ;               (-> (rgb/abs-distance c1 c2)
; ;                   (/ (* 3 255))
; ;                   (#(- 1 %))
; ;                   (ic/pow 10)))
; ;          ;; Calculates the new value of the pixel [x, y] by applying a convolution.
; ;          (pix-val [x y]
; ;            (->> (get-neighbour-pixels img x y)
; ;                 (map #(coef (c/get-xy img x y) %))
; ;                 ((fn [cs] (map #(/ % (ic/sum cs)) cs)))
; ;                 (apply-kernel img x y)))]
; ;    (->> (reduce (fn [m y]
; ;                   (->> (reduce #(conj! %1 (pix-val %2 y))
; ;                                (transient [])
; ;                                (range (c/ncols img)))
; ;                        persistent!
; ;                        (conj! m))) 
; ;                 (transient [])
; ;                 (range (c/nrows img)))
; ;      persistent!
; ;;      (grid-apply pix-val
; ;;                  0 (c/ncols img)
; ;;                  0 (c/nrows img))
; ;;      (partition (c/ncols img))
; ;;      (mapv vec)
; ;      (#(c/make-image % (:type img)))
; ;      )))
