(ns re-con.board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]))

(defn init-board-status [db]
  (assoc db :board-status {:first-pick nil, :second-pick nil}))

(defn cell-picked "maintain :first-pick and :second-pick status" [db selected-mesh]
  (println "board.cell-picked: entered: ")
  (let [first-pick (get (db :board-status) :first-pick)
        second-pick (get (db :board-status) :second-pick)]
    (cond
      (not first-pick) (assoc db (-> db :board-status :first-pick) selected-mesh)
      (not second-pick) (assoc (db :board-status) :second-pick selected-mesh)
      :else db)))
