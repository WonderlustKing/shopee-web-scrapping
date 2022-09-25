(ns shopee-web-scrapping.csv-creator
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]))

(defn search-results->csv-file
  [results file-name]
  (let [file-exists? (.exists (io/file file-name))]
    (with-open [writer (io/writer file-name :append true)]
      (csv/write-csv writer (if file-exists?
                              results
                              (concat [["Item-id" "Name" "Price" "Order-in-page" "Page-number"]]
                                      results))))))
