(ns re-con.utils
  (:require
   [re-frame.core :as re-frame]))

(defn get-panel-index
  "given a panel (babylon mesh), return the index within the board.  Assumes a panel naming convention of 'panel-xx'"
  [panel]
  ; (println "get-panel-index, panel name=" (.-name panel) ",match=" (re-matches #"panel-(\d+)" (.-name panel)))
  ; (println "result=" (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name panel)) 1)))
  (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name panel)) 1)))

(defn get-front-panel-img [db panel]
  ((nth (db :board-cells) (get-panel-index panel)) :front-img))
