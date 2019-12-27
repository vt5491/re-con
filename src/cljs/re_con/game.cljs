(ns re-con.game
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.controller :as controller]
   [re-con.cell :as cell]
   [re-con.main-scene :as main-scene]
   [re-con.scenes.con-panel-scene :as cp-scene]))

(def default-img-map
  {:0 base/burj-al-arab-img, :1 base/smiley-img, :2 base/smiley-img, :3 base/smiley-img,
   :4 base/smiley-img, :5 base/smiley-img, :6 base/smiley-img, :7 base/smiley-img,
   :8 base/burj-al-arab-img, :9 base/smiley-img, :10, base/smiley-img, :11 base/smiley-img,
   :12 base/smiley-img, :13 base/smiley-img, :14 base/smiley-img, :15 base/smiley-img})

(defn render-loop []
      ; (println "render loop")
      ; (::controller/tick)
      (controller/tick)
      (.render main-scene/scene))

(defn init []
  (main-scene/init)
  ; (cell/init-board base/con-row-cnt base/con-col-cnt default-img-map)
  (re-frame/dispatch [:init-board])
  (doseq [i (range 1 5)] (println "tmp"))
  ; (cp-scene/init-panel-scene)
  (re-frame/dispatch [:init-con-panel-scene])
  (main-scene/run-scene render-loop))
