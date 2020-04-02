(ns re-con.core
  (:require
   [reagent.core :as reagent]
   [re-frame.core :as re-frame]
   [re-con.events :as events]
   [re-con.views :as views]
   [re-con.config :as config]
   [babylonjs]
   [babylonjs-gui]
   [re-con.controller :as controller]
   [re-con.game :as game]
   [re-con.main-scene :as main-scene]))
   ; [re-con.scenes.con-panel-scene :as cp-scene]))


;; user code
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
