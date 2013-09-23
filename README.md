# eye-boof

This is a Clojure wrapper for the image processing library [BoofCV](http://boofcv.org/).
Additionally there are complementary functions implemented in clojure

## Usage

### Loading and viewing images

    (require '[eye-boof.helpers :as h])
    
    (def img (h/load-file-image "test/rgbb.jpg"))

    (require '[eye-boof.visualize :as v])
    
    (v/view img)
    
### Changing colorspace 

To gray img or to bw:

    (require '[eye-boof.processing :as p])

    (v/view img (p/rgb-to-gray img) (p/binarize img 127))

### BW image processing
    
    (def bin-img (p/threshold img 127 :down true))
   
    (require '[eye-boof.binary-ops :as binop])
    
    (def contours (binop/contours bin-img 8))

    (v/view bin-img (bufferedImage<-contours contours :image bin-img))

    (def labeled-image (binop/labeled-image bin-img 8))

    (v/view bin-img (binop/bufferedImage<-labeled-image labeled-image))

## License

Copyright © 2013 Andre Boechat

Distributed under the Eclipse Public License, the same as Clojure.
