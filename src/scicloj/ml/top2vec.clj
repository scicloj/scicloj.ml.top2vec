(ns scicloj.ml.top2vec
  (:require
   [scicloj.metamorph.ml :as mm]
   [tablecloth.api :as tc]
   [clojure.java.io :as io]
   [libpython-clj2.python :as py]
   [camel-snake-kebab.core :as csk]
   [tech.v3.libs.fastexcel]
   [libpython-clj2.require :refer [require-python]]
   [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(require-python 'top2vec
                '[scipy.special :refer [softmax]]
                'wordcloud)
;; https://gist.githubusercontent.com/behrica/91b3f958fad80247069ade3b96646dcf/raw/4f58a93118702d34394e49fb8e1f3c4b4ed6c95f/PWI_top2vec.py
(require-python 'PWI_top2vec)


(defn file->bytes [path]
  (with-open [in (io/input-stream path)
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn bytes->file [bytes]
  (let [temp-file (.getPath (java.io.File/createTempFile "top2vec" ".bin"))]
    (io/copy
     bytes
     (java.io.File. temp-file))
    temp-file))


(defn do-train [s opts]
  (apply top2vec/Top2Vec (py/->py-list s) (apply concat opts)))

(defn train
  [feature-ds label-ds options]
  (let [documents (get feature-ds (options :documents-column))
        top-2-vec-opts (dissoc options :documents-column :model-type :pwi-num-topics)
        model (do-train documents top-2-vec-opts)
        temp-file (.getPath (java.io.File/createTempFile "top2vec" ".bin"))
        _ (py. model save temp-file)
        num-topics (py/py. model get_num_topics)
        _ (assert (> num-topics 1) "The top2vec model needs to have more then 1 topic in order to cacluate the PWI")
        pwi (PWI_top2vec/PWI model (py/py.- model documents) (or  (:pwi-num-topics options) (dec num-topics)))]
    {
     :n-topics num-topics
     :pwi pwi
     :model-file temp-file
     :model-as-bytes (file->bytes temp-file)}))







(defn thaw-fn [model-data]
  (let [model-file (bytes->file (:model-as-bytes model-data))]
    (py/py. top2vec/Top2Vec load model-file)))



(defn wc->svg [top2vec-model word-scores width height]
  (let [wc (wordcloud/WordCloud :width width :height height)
        wco
        (py/py. wc generate_from_frequencies
                (zipmap
                 (:topic-words word-scores)
                 (:softmax word-scores)))]
    (py/py. wco to_svg)))




(defn get-all-word-scores [top2vec-model]

  (let [word-scores
        (map #(hash-map :topic-words %1 :topic-word-scores %2)
             (py/->jvm
              (py/get-attr top2vec-model "topic_words"))
             (py/->jvm
              (py/get-attr top2vec-model "topic_word_scores")))]


    (map
     #(assoc % :softmax
             (->  (:topic-word-scores %)
                  (py/->python)
                  (softmax)
                  (py/->jvm)))
     word-scores)))


(mm/define-model! :top2vec train nil {:unsupervised? true
                                      :thaw-fn thaw-fn})


(comment
  (require '[tablecloth.api :as tc])


  (def raw-data
    (tc/dataset "https://github.com/scicloj/scicloj.ml.smile/blob/main/test/data/reviews.csv.gz?raw=true"
                {:key-fn csk/->kebab-case-keyword
                 :file-type :csv
                 :gzipped? true}))



  (def data
    (-> raw-data
        (tc/shuffle {:seed 123})
        (tc/head 10000)
        (tc/select-columns :text)
        tc/drop-missing))
  ;; (ds/set-inference-target :abstract)

  ;; (ds/feature data)

  (def train-result-learn
    (scicloj.metamorph.ml/train data {:speed :learn
                                      :model-type :top2vec
                                      :min_count 1
                                      :documents-column :text}))

  (def train-result-fast-learn
    (scicloj.metamorph.ml/train data {:speed :fast-learn
                                      :model-type :top2vec
                                      :min_count 1
                                      :documents-column :text}))

  (def train-result-deep-learn
    (scicloj.metamorph.ml/train data {:speed :deep-learn
                                      :model-type :top2vec
                                      :documents-colum :abstract}))
  ;; :pwi-num-topics 10

  (mm/thaw-model train-result-learn)

  (def result (train data nil))
  (def bbs (file->bytes (:model-file result)))
  :ok)
