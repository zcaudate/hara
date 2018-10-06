(ns hara.protocol.opencl-test
  (:use hara.test)
  (:require [hara.protocol.opencl :refer :all]))

^{:refer hara.protocol.opencl/-opencl-create-input :added "3.0"}
(fact "extensible function for creating the buffer")

^{:refer hara.protocol.opencl/-opencl-write-input :added "3.0"}
(fact "extensible function for writing to buffer")

^{:refer hara.protocol.opencl/-opencl-read-output :added "3.0"}
(fact "extensible function for reading from buffer")
