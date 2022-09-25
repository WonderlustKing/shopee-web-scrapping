(ns shopee-web-scrapping.core
  (:require [shopee-web-scrapping.api-client :as api]
            [shopee-web-scrapping.csv-creator :as csv]
            [clojure.string :as string])
  (:gen-class))

(defn search-category-id
  [category-id offset limit]
  (->> (api/search-and-get-items-response category-id offset limit)
       (map-indexed (fn [idx item]
                      (let [basic-item (:item_basic item)]
                        [(:itemid basic-item)
                         (:name basic-item)
                         (float (/ (:price basic-item) 100000))
                         (inc idx)
                         (inc (quot offset limit))])))))

(defn search->X-pages
  [category-id pages page-limit]
  (let [result-promise (promise)]
    (future (deliver result-promise (mapcat #(search-category-id category-id (* page-limit %) page-limit)
                                            (range pages))))
    result-promise))


(defn search-in-3-pages-with-limit-60
  [category-ids]
  (mapcat #(deref % 10000 [])
          (map #(search->X-pages % 3 60) category-ids)))

(defn csv-result-for
  [categories]
  (let [csv-file-name (str (string/join "-" categories)
                           "_"
                           (quot (System/currentTimeMillis) 1000)
                           "_results.csv")]
    (doseq [batch (partition-all 3 categories)]
      (-> (search-in-3-pages-with-limit-60 batch)
          (csv/search-results->csv-file csv-file-name)))
    (println (str "CSV output result file created in: " csv-file-name " file"))))


(defn -main
  [& args]
  (if (< (count args) 3)
    (do
      (println "You should provide at least 3 category-ids")
      (System/exit 1))
    (do
      (println "Fetching results...")
      (csv-result-for args)
      (println "Done!")
      (System/exit 0))))

