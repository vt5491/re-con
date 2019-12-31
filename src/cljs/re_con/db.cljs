(ns re-con.db
  (:require
   [re-frame.core :as re-frame]))

(def default-db
  {:name "re-frame"
   :trigger-pressed false
   :board-cells []})

(re-frame/reg-sub
  :trigger-pressed
  (fn [db _]
    (:trigger-pressed db)))

(re-frame/reg-sub
 :board-cells
 (fn [db _]
   (:board-cells db)))
