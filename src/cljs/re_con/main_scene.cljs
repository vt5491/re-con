;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require
   [babylonjs]
   ; [re-con.utils as utils]))
   [re-con.controller :as controller]))

(def canvas)
(def engine)
(def scene)
(def camera)
(def vrHelper)
(def ground)
(def light1)
(def rig)
(def sphere)
(def panel)
(def redMaterial)
(def blueMaterial)
(def greenMaterial)
(def imgMat)
(def assetsManager)
(def meshTask)
(def textureTask)


(defn mesh-selected [])

(defn load-img-cb []
  (fn [task]
    (println "hello matey 2")
    (set! (.-diffuseTexture imgMat) (js/BABYLON.Texture. task.texture))))

(defn init []
  (println "now in init-scene")
  (set! canvas (-> js/document (.getElementById "renderCanvas")))
  (set! engine (js/BABYLON.Engine. canvas true))
  (set! scene (js/BABYLON.Scene. engine))
  (set! vrHelper (.createDefaultVRExperience scene))
  (controller/init scene vrHelper)
  (controller/setup-controller-handlers vrHelper)
  (js/setupControllers vrHelper)
  ; (-> vrHelper .-onNewMeshSelected (.add (fn [] (println "new mesh selected"))))
  (set! camera (.-webVRCamera vrHelper))
  ; (js/setupLoaders scene)
  (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
  (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 0 0))
  (set! blueMaterial (js/BABYLON.StandardMaterial. "blueMaterial" scene))
  (set! (.-diffuseColor blueMaterial) (js/BABYLON.Color3. 0 1 0))
  (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMaterial" scene))
  (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 0 1))
  (set! imgMat (js/BABYLON.StandardMaterial. "imgMat" scene))
  (set! assetsManager (js/BABYLON.AssetsManager. scene))
  (set! textureTask (.addTextureTask assetsManager "load-texture" "imgs/burj_al_arab.jpg"))
  (set! textureTask.onSuccess (load-img-cb))
  (.load assetsManager)
  (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 10 "height" 10) scene))
  (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
  (.enableInteractions vrHelper)
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  (.setEnabled light1 true))

(defn init-panel-scene[]
  (set! panel (js/BABYLON.MeshBuilder.CreateBox. "panel"
                                                (js-obj "height" 5 "width" 2 "depth" 0.5)
                                                scene))
  (set! (.-position panel)(js/BABYLON.Vector3. 0 0 3))
  (set! (.-material panel) redMaterial))

(defn run-scene [render-loop]
  (.runRenderLoop engine (fn [] (render-loop))))
