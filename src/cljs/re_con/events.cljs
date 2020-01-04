(ns re-con.events
  (:require
   [re-frame.core :as re-frame]
   [re-con.db :as db]
   [re-con.base :as base]
   [re-con.utils :as utils]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]
   [re-con.cell :as cell]
   [re-con.board :as board]
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


;; cp-scene events
(re-frame/reg-event-db
 :abc
 (fn [db [_ msg]]
   (cp-scene/abc msg)
   db))

(re-frame/reg-event-db
 :abc-2
 (fn [db [_ msg]]
   (cp-scene/abc-2 msg)
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
 :toggle-panel-material
 (fn [db [_ _]]
   (cp-scene/toggle-panel-material db (-> (db :selected-mesh) (.-name)))
   db))


(re-frame/reg-event-db
  :toggle-trigger
  (fn [db _]
    (println "now in toggle-trigger, not of trigger-pressed=" (not (:trigger-pressed db)))
    ; {:trigger-pressed true}))
    (assoc db :trigger-pressed (not (:trigger-pressed db)))))
    ; (set! tmp (assoc db :trigger-pressed true))
    ; tmp))


;; note: main trigger routine
; (re-frame/reg-event-db
;  :trigger-handler
;  (fn [db [_ stateObject]]
;    (let [board-status (db :board-status)
;          first-pick (get board-status :first-pick)
;          second-pick (get board-status :second-pick)]
;      (cond (or (= first-pick nil) (= second-pick nil))
;        (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
;          ; side effect
;          (cp-scene/toggle-panel-material db (-> (get db :selected-mesh) (.-name)))
;          ; and fire a cell-picked event
;          (re-frame/dispatch [:cell-picked (get db :selected-mesh)]))))
;
;    (assoc db :trigger-pressed (.-pressed stateObject))))

(re-frame/reg-event-fx
 :trigger-handler
 (fn [{:keys [db]} [_ stateObject]]
   (let [board-status (db :board-status)
         first-pick (get board-status :first-pick)
         second-pick (get board-status :second-pick)]
     ; (cond (or (= first-pick nil) (= second-pick nil))
     ;   (if (and (.-pressed stateObject) (not (:trigger-pressed db)))
     ;     ; side effect
     ;     (cp-scene/toggle-panel-material db (-> (get db :selected-mesh) (.-name)))))
         ; and fire a cell-picked event
         ; (re-frame/dispatch [:cell-picked (get db :selected-mesh)]))))
   ; {
    (merge {:db (assoc db :trigger-pressed (.-pressed stateObject))}
           (when-let [dummy-val (and (or (= first-pick nil) (= second-pick nil))
                                     (.-pressed stateObject)
                                     (not (:trigger-pressed db)))]
             {:dispatch-n (list [:toggle-panel-material]
                                [:cell-picked (db :selected-mesh)])})))))
           ; (when-let [val (get {:a 7} :a)])
           ; (when-let [val true]
           ;   {:dispatch-n (list [:abc "mj"] [:abc-2 "angus young"])})))))
    ; (if true
    ;   (:dispatch-n (list [:abc "mj"] [:abc-2 "angus young"])))}))

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
   ;non-rf side effect
    (cp-scene/show-full-rebus db)
    db))
  ; (println "selected-mesh=" (if (contains? db :selected-mesh)
  ;                             (-> (get db :selected-mesh) .-name)
  ;                             nil))))

(re-frame/reg-event-db
 :action-2
 (fn [db _]
   ;side effect
   ; (println "rebus-mat 4=" (db :rebus-mat 4))
   ; (println "board-cells-2=" (db :board-cells-2))
   (cp-scene/show-full-rebus-2 db)
   (println "rebus-mat-4=" (cell/get-rebus-mat db 4))
   db))
;; cp-scene related events
(re-frame/reg-event-db
 :init-con-panel-scene
 (fn [db [_]]
   ; side effect
   (cp-scene/init-con-panel-scene db)
   db))

;; cell related events
(re-frame/reg-event-db
 :add-cell
 (fn [db [_ cell]]
   (println "add-cell.fn: entered, db.do-it=" (db/do-it))
   (assoc db :board-cells (conj (:board-cells db) cell))))

(re-frame/reg-event-db
 :cell-front-img
 (fn [db [_ index img]]
   db))

(re-frame/reg-event-db
 :init-board-cells
 (fn [db [_]]
   ; side effect
   (cell/init-board-cells db base/board-row-cnt base/board-col-cnt base/default-img-map)
   db))

(re-frame/reg-event-db
 :load-front-imgs
 (fn [db [_]]
   ; side effect
   (cp-scene/load-front-imgs db)
   db))

(re-frame/reg-event-db
 :load-rebus-imgs
 (fn [db [_]]
   ;non-rf side effect
   (cp-scene/load-rebus-imgs db)
   db))

(re-frame/reg-event-db
 :front-texture-loaded
 (fn [db [_ task index]]
   ;non-rf side effect
   (cp-scene/front-texture-loaded db task index)
   db))

(re-frame/reg-event-db
 :rebus-texture-loaded
 (fn [db [_ task index]]
   ;side effect
   (cp-scene/rebus-texture-loaded db task index)
   db))

;; board level
(re-frame/reg-event-db
 :init-board-status
 (fn [db [_]]
   ; side effect
   (board/init-board-status db)))

(re-frame/reg-event-db
 :cell-picked
 (fn [db [_ selected-mesh]]
   ; side effect
   (board/cell-picked db selected-mesh)))
