(ns re-con.board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.cell :as cell]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]))


(defn init-board-status [db]
  (assoc db :board-status {:first-pick-index nil, :first-pick nil, :second-pick-index nil, :second-pick nil}))

;; basically a giant state-machine
(defn cell-picked "maintain :first-pick and :second-pick status" [db selected-mesh]
  (println "board.cell-picked: entered: mesh index=" (utils/get-panel-index selected-mesh))
  ; (println "cell-picked: leftControllerGazeTrackerMesh=" (.-leftControllerGazeTrackerMesh main-scene/vrHelper))
  (let [first-pick (get-in db [:board-status :first-pick])
        second-pick (get-in db [:board-status :second-pick])
        selected-mesh-index (utils/get-panel-index selected-mesh)]
    ; (println "top: first-pick=" first-pick, "db=" db)
    (if (not first-pick)
      (do
        ; (println "first-pick path: first-pick=" (get-in db [:board-status :first-pick]) ", selected-mesh=" selected-mesh)
        ; (.drawText (-> cp-scene/status-panel .-material .-diffuseTexture) "first pick")
        (cp-scene/update-status-panel "first pick")
        ;; first pick
        ; (assoc-in db [:board-status :first-pick] selected-mesh)
        (assoc-in (assoc-in db [:board-status :first-pick] selected-mesh) [:board-status :second-pick] nil))
        ; (assoc-in db [:board-status] [:first-pick selected-mesh :second-pick nil]))
      (do
        (println "second-pick path")
        (println "second-pick path, first-pick.name=" (.-name first-pick) ",selected-mesh.name=" (.-name selected-mesh))
        (if (and (not second-pick) (not= (.-name first-pick) (.-name selected-mesh)))
          (do
            ;; second pick
            (println "setting second-pick, first-pick-index=" (utils/get-panel-index first-pick) ",second-pick-img=" (utils/get-front-panel-img db selected-mesh))
            (if (= (utils/get-front-panel-img db first-pick) (utils/get-front-panel-img db selected-mesh))
              (do
                (println "we have a match!, first-pick=" first-pick ", second-pick=" second-pick)
                (cp-scene/update-status-panel "match")
                (let [
                      ; first-pick (get-in db [:board-status :first-pick])
                      first-pick-index (utils/get-panel-index first-pick)
                      first-pick-rebus-mat (-> (nth (db :board-cells) first-pick-index) :rebus-mat)
                      ; first-pick-rebus-mat (get-in db [:board-cells first-pick-index :rebus-mat])
                      ; second-pick-index (utils/get-panel-index second-pick)
                      second-pick-index selected-mesh-index
                      second-pick-rebus-mat (get-in db [:board-cells second-pick-index :rebus-mat])]
                      ; second-pick-mat (-> (nth (db :board-cells) second-pick-index) :rebus-mat)]
                  (re-frame/dispatch [:cell-matched first-pick-index])
                  (re-frame/dispatch [:cell-matched second-pick-index])
                  (cp-scene/show-panel-rebus first-pick-index first-pick-rebus-mat)
                  (cp-scene/show-panel-rebus second-pick-index second-pick-rebus-mat)))
              (do
                (cp-scene/update-status-panel "non-match")))

            ; (when cell/match)
            (assoc-in db [:board-status :second-pick] selected-mesh))
          (do
            (println "third-pick checking")
            ; (if (and second-pick (not= (.-name second-pick) (.-name selected-mesh))))
            (if (and second-pick (and (not= (.-name first-pick) (.-name selected-mesh)) (not= (.-name second-pick) (.-name selected-mesh))))
              ;; third (unique) pick
              ; (println "third-pick processing")
              (do
                (println "now resetting first and second picks")
                (cp-scene/update-status-panel "select a panel")
                (let [
                      ; first-panel-idx (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name first-pick)) 1))
                      ; second-panel-idx (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name second-pick)) 1))
                      first-panel-index (utils/get-panel-index first-pick)
                      second-panel-index (utils/get-panel-index second-pick)]
                  (when-not (= (get-in db [:board-cells first-panel-index :status]) :matched)
                    (re-frame/dispatch [:reset-panel first-panel-index]))
                  (when-not (= (get-in db [:board-cells second-panel-index :status]) :matched)
                    (re-frame/dispatch [:reset-panel second-panel-index]))
                  (re-frame/dispatch [:cell-picked (db :selected-mesh)])
                  ; (re-frame/dispatch-n (list [:reset-panel first-panel-idx]
                  ;                            [:reset-panel second-panel-idx]
                  ;                            [:cell-picked (db :selected-mesh)]))
                  ; (assoc-in db [:board-status] [:first-pick nil :second-pick nil]))))))
                  (assoc-in (assoc-in db [:board-status :first-pick] nil) [:board-status :second-pick] nil)))
              (do (println "cell-picked: last path")
                db))))))))
