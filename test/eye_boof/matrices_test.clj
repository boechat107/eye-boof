(ns eye-boof.test.matrices-test
  (:require [eye-boof.matrices :as eyem]
            [eye-boof.core :as eyec])
  (:import [boofcv.struct.image ImageSInt8 ImageSInt16 ImageSInt32 ImageSInt64 ImageUInt8 ImageUInt16]))

;; (TODO)


(defn benchmark []
  (let [mat (ImageUInt8. 3 3)]
    (println "Normal get")
    (time
     (dotimes [i 1000000]
       (-> mat .data (aget 3))))

    (println "Enhanced get")
    (time
     (dotimes [i 1000000]
       (eyem/get-pixel ImageUInt8 mat 3)))
    
    (println "From core namespace")
    (time
     (dotimes [i 1000000]
       (eyec/get-pixel* mat 3)))
        (time
     (dotimes [i 1000000]
       (eyec/get-pixel mat 3)))))