(ns re-con.db
  (:require
   [re-frame.core :as re-frame]))

(def default-db
  {:name "re-frame"
   :trigger-pressed false
   :rebus-board-cells []})

(defn do-it []
  (+ 7 1))

(re-frame/reg-sub
  :trigger-pressed
  (fn [db _]
    (:trigger-pressed db)))

; (re-frame/reg-sub
;  :board-cells
;  (fn [db _]
;    (:board-cells db)))

; (re-frame/reg-sub
;  :board-cells-2
;  (fn [db _]
;    (println "now in events.board-cells-2")
;    (db :board-cells)))

; note: this doesn't seem to work
; (re-frame/reg-sub
;  :rebus-mat
;  (fn [db [_ index]]
;    (println "get rebus-mat: index=" index)
;    (get (nth (:board-cells db) index) :rebus-mat)))
