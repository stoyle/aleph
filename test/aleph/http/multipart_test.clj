(ns aleph.http.multipart-test
  (:use
   [clojure test])
  (:require
   [aleph.http.multipart :as mp]
   [byte-streams :as bs])
  (:import
   [java.io
    File]))

(def file-to-send (File. (str (System/getProperty "user.dir") "/test/file.txt")))

(deftest test-multipart-builder
  (let [body (mp/encode-body [{:part-name "part1"
                               :content "content1"
                               :charset "UTF-8"}
                              {:part-name "part2"
                               :content "content2"}
                              {:part-name "part3"
                               :content "content3"
                               :mime-type "application/json"}
                              {:part-name "part4"
                               :content "content4"
                               :mime-type "application/xml"
                               :charset "ISO-8859-1"}
                              {:part-name "part5"
                               :content "content5"
                               :name "content5.pdf"}
                              {:name "part6"
                               :content "content6"}])
        body-str (bs/to-string body)]
    (is (.contains body-str "name=\"part1\""))
    (is (.contains body-str "name=\"part2\""))
    (is (.contains body-str "name=\"part3\""))
    (is (.contains body-str "name=\"part4\""))
    (is (.contains body-str "name=\"part5\""))
    (is (.contains body-str "name=\"part6\""))
    (is (.contains body-str "content1"))
    (is (.contains body-str "content2"))
    (is (.contains body-str "content-disposition: form-data;"))
    ;; default mime-type
    (is (.contains body-str "content-type: application/octet-stream;charset=UTF-8"))
    ;; omitting charset
    (is (.contains body-str "content-type: application/json\n"))
    ;; mime-type + charset
    (is (.contains body-str "content-type: application/xml;charset=ISO-8859-1"))
    ;; filename header
    (is (.contains body-str "filename=\"content5.pdf\""))))

(deftest test-custom-boundary
  (let [b (mp/boundary)
        body (mp/encode-body b [{:part-name "part1" :content "content1"}])
        body-str (bs/to-string body)]
    (is (.endsWith body-str (str b "--")))))

(deftest test-content-as-file
  (let [body (mp/encode-body [{:part-name "part1"
                               :content file-to-send}
                              {:part-name "part2"
                               :mime-type "application/png"
                               :content file-to-send}
                              {:part-name "part3"
                               :name "text-file-to-send.txt"
                               :content file-to-send}
                              {:part-name "part4"
                               :charset "UTF-8"
                               :content file-to-send}
                              {:content file-to-send}])
        body-str (bs/to-string body)]
    (is (.contains body-str "name=\"part1\""))
    (is (.contains body-str "name=\"part2\""))
    (is (.contains body-str "name=\"part3\""))
    (is (.contains body-str "name=\"part4\""))
    (is (.contains body-str "name=\"file.txt\""))
    (is (.contains body-str "filename=\"file.txt\""))
    (is (.contains body-str "filename=\"text-file-to-send.txt\""))
    (is (.contains body-str "content-type: text/plain\n"))
    (is (.contains body-str "content-type: text/plain;charset=UTF-8\n"))
    (is (.contains body-str "content-type: application/png\n"))))
