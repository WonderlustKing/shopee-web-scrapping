(ns shopee-web-scrapping.api-client
  (:require [clj-http.client :as client]
            [cheshire.core :as json]))

(def SHOPEE-SEARCH-API-BASE-URL "https://shopee.sg/api/v4/search/search_items")

(defn- do-search-request
  [category-id offset limit]
  (try
    (client/get SHOPEE-SEARCH-API-BASE-URL {:query-params {:by        "relevancy"
                                                           :limit     limit
                                                           :match_id  category-id
                                                           :newest    offset
                                                           :order     "desc"
                                                           :page_type "search"
                                                           :scenario  "PAGE_CATEGORY"
                                                           :version   2}}
                {:as :json})
    (catch Exception e
      (println (str "Exception while making an API call for " category-id " category. " (.getMessage e))))))

(defn search-and-get-items-response
  [category-id offset limit]
  (-> (do-search-request category-id offset limit)
      :body
      (json/parse-string true)
      :items))