;; good docs:
;;https://doc.babylonjs.com/how_to/introduction_to_webxr
(ns re-con.controller-xr
  (:require [re-frame.core :as re-frame]))
            ; [re-con.db :as db]))

(def scene)
(def xr)
(def ctrl-xr)
(def left-ctrl-xr)
(def right-ctrl-xr)
(def main-trigger)

(declare trigger-handler)
(declare get-ctrl-handedness)

(defn init [tgt-scene xr-helper]
  (println "controller-xr.init entered")
  (set! scene tgt-scene)
  (set! xr xr-helper))

(defn ctrl-added [xr-controller]
  (println "controller-xr.ctrl-added: xr-controller.uniqueId=" (.-uniqueId xr-controller) ",handedness=" (get-ctrl-handedness xr-controller))
  ; const mainTrigger = xrController.motionController.getComponent(WebXRControllerComponent.TRIGGER);
  (let [handedness (get-ctrl-handedness xr-controller)]
    (when (= handedness :left)
      (set! left-ctrl-xr xr-controller))
    (when (= handedness :right)
      (set! right-ctrl-xr xr-controller)))

  (set! main-trigger (-> xr-controller (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
  ; (set! ctrl-xr xr-controller)
  ; (set! main-trigger (-> ctrl-xr (.-gamepadController) (.getComponent js/BABYLON.WebXRControllerComponent.TRIGGER)))
  (println "controller-xr.ctrl-added: main-trigger=" main-trigger)
  (when main-trigger
    ; (.onButtonStateChanged main-trigger trigger-handler)
    (println "controller-xr.ctrl-added: add onButtonStateChanged to main-trigger=" main-trigger)
    (-> main-trigger (.-onButtonStateChanged) (.add trigger-handler))))

(defn trigger-handler []
  (println "trigger fired"))

;; determine if id of the ctrl is "left" or "right"
(defn get-ctrl-handedness [ctrl]
  (let [id (.-uniqueId ctrl)]
    (if (re-matches #".*-(left).*" id)
      :left
      (if (re-matches #".*-(right).*" id)
        :right))))
