(ns re-con.events
  (:require
   [re-frame.core :as re-frame]
   [re-con.db :as db]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]
   [re-con.cell :as cell]
   [re-con.game :as game]))

(def tmp)

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
  :toggle-light
  (fn [db _]
    (println "toggle-light clicked: enabled status=" (.isEnabled main-scene/light1))
    (let [light main-scene/light1]
      (if (.isEnabled light)
        (.setEnabled light false)
        (.setEnabled light true)))
    db))


;; usage:  (dispatch [:trigger-change true])
(re-frame/reg-event-db
  :trigger-change
  (fn [db [_ new-state]]
    (println "now in trigger-change, new-state=" new-state)
    ;; babylon side effect here
    (if (and new-state (contains? db :selected-mesh))
      (cp-scene/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)
      ())
    (assoc db :trigger-pressed new-state)))

;; non re-frame func
(defn mat-change-check [new-state db]
  (if (and new-state (contains? db :selected-mesh))
      (cp-scene/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)
      ()))

(re-frame/reg-event-db
  :toggle-trigger
  (fn [db _]
    (println "now in toggle-trigger, not of trigger-pressed=" (not (:trigger-pressed db)))
    ; {:trigger-pressed true}))
    (assoc db :trigger-pressed (not (:trigger-pressed db)))))
    ; (set! tmp (assoc db :trigger-pressed true))
    ; tmp))

(re-frame/reg-event-db
 :trigger-handler
 (fn [db [_ stateObject]]
   (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
    (re-frame/dispatch [:trigger-change true])
    (re-frame/dispatch [:trigger-change false]))))
   ; db))

(re-frame/reg-event-db
 :trigger-handler-2
 (fn [db [_ stateObject]]
   (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
     ; (cp-scene/change-panel-material (-> (get db :selected-mesh) (.-name)) main-scene/blueMaterial)

     (cp-scene/toggle-panel-material (-> (get db :selected-mesh) (.-name))))
   (assoc db :trigger-pressed (.-pressed stateObject))))

; (re-frame/reg-event-db
;  :toggle-trigger
;  {:trigger-pressed true})
(re-frame/reg-event-db
 :print-db
 (fn [db _]
   (println "db=" db ",trigger-pressed=" (:trigger-pressed db))
   db))

(re-frame/reg-event-db
 :mesh-selected
 (fn [db [_ mesh]]
   (assoc db :selected-mesh mesh)))

(re-frame/reg-event-db
 :mesh-unselected
 (fn [db [_ mesh]]
   (dissoc db :selected-mesh)))

(re-frame/reg-event-db
 :info
 (fn [db _]
   (println "selected-mesh=" (if (contains? db :selected-mesh)
                               (-> (get db :selected-mesh) .-name)
                               nil))))

;; cp-scene related events
(re-frame/reg-event-db
 :init-con-panel-scene
 (fn [db [_]]
   ; side effect
   (cp-scene/init-panel-scene db)
   db))

;; cell related events
(re-frame/reg-event-db
 :add-cell
 (fn [db [_ cell]]
   (println "add-cell.fn: entered")
   (assoc db :board (conj (:board db) cell))))

(re-frame/reg-event-db
 :cell-front-img
 (fn [db [_ index img]]
   db))

(re-frame/reg-event-db
 :init-board
 (fn [db [_]]
   ; side effect
   (cell/init-board db base/board-row-cnt base/board-col-cnt game/default-img-map)
   db))

(re-frame/reg-event-db
 :load-front-imgs
 (fn [db [_]]
   ; side effect
   (cell/load-front-imgs db)
   db))
