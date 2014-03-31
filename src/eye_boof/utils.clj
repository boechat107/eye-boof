(ns eye-boof.utils
  "Functions and macros that helps to develop new functions and algorithms.")

(defmacro def-noargs-function
  "Macro to define new functions that have a single argument, an Image."
  [fname doc f]
  `(defn ~fname 
     ~doc
     [~'img]
     (~f ~(vary-meta 'img assoc :tag 'ImageUInt8))))
