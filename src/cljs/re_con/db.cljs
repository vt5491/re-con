(ns re-con.db
  (:require
   [re-frame.core :as re-frame]))

(def default-db
  {:name "re-frame"
   :trigger-pressed false
   :board []})

(re-frame/reg-sub
  :trigger-pressed
  (fn [db _]
    (:trigger-pressed db)))

(re-frame/reg-sub
 :board
 (fn [db _]
   (:board db)))
