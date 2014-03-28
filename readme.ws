;; gorilla-repl.fileformat = 1

;; **
;;; # eye-boof usage
;;; 
;;; Most of times we need to work with images that already exist, such files in the
;;; disk. So, to load a file from the disk, we write something like
;; **

;; @@
(refer-clojure :exclude '[lazy-seq])
(require '[eye-boof.core :refer :all]
         '[eye-boof.io :refer :all])

(def color-img (-> "http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg"
                   (java.net.URL.)
                   (load-image)))
 
 color-img
;; @@
;; =>
;;; #<MultiSpectral boofcv.struct.image.MultiSpectral@13500587>
;; <=

;; **
;;; ![color eye](http://png-3.vector.me/files/images/8/0/807142/realistic_vector_eye_thumb.jpg)
;;; 
;;; Note that a `MultiSpectral` data structure is returned, which is the common data structure
;;; to store the pixels' intensities of color images. To know how many color channels this
;;; image holds, we can type
;; **

;; @@
(nbands color-img)
;; @@
;; =>
;;; 3
;; <=

;; **
;;; what probably means a RGB image.
;; **

;; @@

;; @@
