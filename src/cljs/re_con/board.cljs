(ns re-con.board
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.cell :as cell]
   [re-con.main-scene :as main-scene]))

(defn init-board-status [db]
  (assoc db :board-status {:first-pick-index nil, :first-pick nil, :second-pick-index nil, :second-pick nil}))

(defn cell-picked "maintain :first-pick and :second-pick status" [db selected-mesh]
  (println "board.cell-picked: entered: mesh index=" (utils/get-panel-index selected-mesh))
  ; (println "cell-picked: leftControllerGazeTrackerMesh=" (.-leftControllerGazeTrackerMesh main-scene/vrHelper))
  (let [first-pick (get-in db [:board-status :first-pick])
        second-pick (get-in db [:board-status :second-pick])]
    ; (println "top: first-pick=" first-pick, "db=" db)
    (if (not first-pick)
      (do
        ; (println "first-pick path: first-pick=" (get-in db [:board-status :first-pick]) ", selected-mesh=" selected-mesh)
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
            (when (= (utils/get-front-panel-img db first-pick) (utils/get-front-panel-img db selected-mesh))
              (println "we have a match!"))
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
                (let [first-panel-idx (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name first-pick)) 1))
                      second-panel-idx (js/parseInt (nth (re-matches #"panel-(\d+)" (.-name second-pick)) 1))]
                  (re-frame/dispatch [:reset-panel first-panel-idx])
                  (re-frame/dispatch [:reset-panel second-panel-idx])
                  (re-frame/dispatch [:cell-picked (db :selected-mesh)])
                  ; (re-frame/dispatch-n (list [:reset-panel first-panel-idx]
                  ;                            [:reset-panel second-panel-idx]
                  ;                            [:cell-picked (db :selected-mesh)]))
                  ; (assoc-in db [:board-status] [:first-pick nil :second-pick nil]))))))
                  (assoc-in (assoc-in db [:board-status :first-pick] nil) [:board-status :second-pick] nil)))
              (do (println "cell-picked: last path")
                db))))))))
