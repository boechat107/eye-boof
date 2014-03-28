# eye-boof

This is a Clojure wrapper for the image processing library [BoofCV](http://boofcv.org/).
Some algorithms, not implemented in BoofCV, are implemented in Clojure using the
BoofCV data structures.

## Installation

The newest version of the library, `[org.clojars.boechat107/eye-boof "1.0.0"]`, is
not compatible with the older versions. A lot of modifications and simplifications
were made! It is better to forget about the previous versions.

## Features

* We are trying to provide wrappers for most of algorithms of 
[BoofCV](http://boofcv.org/index.php?title=Manual).

## Usage

Most of times we need to work with images that already exist, such files in the
disk. So, to load a file from the disk, we write something like

```clojure
```

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
