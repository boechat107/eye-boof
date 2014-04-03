(ns eye-boof.dev-tools
  (:require 
    [eye-boof.core :refer [new-image ncols nrows sub-image]]))

(defn into-blocks
  "Divides an image into blocks of size x size and returns them as a lazy sequence."
  [img size]
  (let [h (nrows img)
        w (ncols img)]
    (for [y-orig (range 0 (nrows img) size)
          x-orig (range 0 (ncols img) size)]
      (sub-image img x-orig y-orig 
                 (min size (- w x-orig))
                 (min size (- h y-orig))))))

(defn block-apply
  "Returns the result of applying f (which must accept an input image and an output
  image) on the input image's blocks of size x size."
  [img size f!]
  (let [out-img (new-image (nrows img) (ncols img) (:type img))]
    (dorun 
      (map #(f! %1 %2)
           (into-blocks img size)
           (into-blocks out-img size)))
    out-img))
