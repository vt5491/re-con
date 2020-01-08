(ns re-con.utils
  (:require
   [re-frame.core :as re-frame]))

(defn get-panel-index
  "given a panel (babylon mesh), return the index within the board.  Assumes a panel naming convention of 'panel-xx'"
  [panel]
  (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name panel)) 1)))
