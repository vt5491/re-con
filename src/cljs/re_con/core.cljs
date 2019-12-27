(ns re-con.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-con.events :as events]
   [re-con.views :as views]
   [re-con.config :as config]
   [babylonjs]
   [re-con.controller :as controller]
   [re-con.game :as game]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]))


;; user code
; (defn render-loop []
;       ; (println "render loop")
;       ; (::controller/tick)
;       (controller/tick)
;       (.render main-scene/scene))
;; end user code

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (reagent/render [views/main-panel]
                  (.getElementById js/document "app")))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root)
  (game/init))
  ; (main-scene/init)
  ; (controller/init vrHelper)
  ; (controller/init main-scene)
  ; (controller/init main-scene/scene vrHelper)
  ; (controller/setup-controller-handlers main-scene/vrHelper)
  ; (def left-controller (.-leftController main-scene/camera))
  ; (println "init: left-controller=" left-controller)
  ; (controller/controller-mesh-loaded-handler (.-leftController main-scene/camera))
  ; (main-scene/init-panel-scene)
  ; (cp-scene/init-panel-scene)
  ; (main-scene/run-scene render-loop))
  ; (main-scene/-main)
  ; (.addEventListener
  ;  js/window
  ;  "DOMContentLoaded"
  ;  (fn [] (.log js/console "DOMContentLoaded callback")
  ;    (main-scene/-main))))

  ; (draw-cube))
