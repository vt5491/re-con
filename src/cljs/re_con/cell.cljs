;; namespace for all things related to a cell slot in the concentration board.  The equivalent
;; of a "brick" in the game of breakout.
;; First created 2019-12-26.
(ns re-con.cell
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.main-scene :as main-scene]))

(def textureTask)

(defn init-front-img [cell])

(defn init-board-cells [db row-cnt col-cnt default-img-map]
  (dotimes [i (* row-cnt col-cnt)]
           ; (println "i=" i)
           (re-frame/dispatch [:add-cell { :front-img (default-img-map (keyword (str i)))
                                          :front-mat (js/BABYLON.StandardMaterial. (str "front-mat-" i) main-scene/scene)
                                          :rebus-img-stem base/rebus-img-stem
                                          :rebus-mat (js/BABYLON.StandardMaterial. (str "rebus-mat-" i main-scene/scene))
                                          :status :active}]))
  ; (println "init-board: entered name=" db.board-cells)
  (re-frame/dispatch [:load-rebus-imgs])
  (re-frame/dispatch [:load-front-imgs]))

  ;; now loop over the previously created cell vector, and load and set the fron image
  ; (println "init-board: db.board=" (db :board))
  ; (for [cell (db :board)]
  ;   (println "cell.front-img=" (get cell :front-img))))
(defn get-rebus-mat [db index]
  (get (nth (db :board-cells) index) :rebus-mat))
