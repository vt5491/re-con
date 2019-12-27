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

; (defn load-img-cb [task]
;   (println "load-img-cb: now loading image"))
  ; (set! (.-diffuseColor imgMat) (js/BABYLON.Texture.)))
  ; (fn []

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
  ; (set! meshTask (.addMeshTask assetsManager "load-img" "" "imgs/" "burj_al_arab.jpg"))
  ; (set! meshTask (.addTextureTask assetsManager "load-img" "imgs/burj_al_arab.jpg"))
  ; (set! textureTask (.addTextureTask assetsManager "load-texture" "https://infinitewheelie.org/img/tux_tada.jpg"))
  (set! textureTask (.addTextureTask assetsManager "load-texture" "imgs/burj_al_arab.jpg"))
  ; (.onSuccess meshTask re-con.main-scene/load-image-cb)
  ; (.onSuccess meshTask mesh-selected)
  ; (.onSuccess meshTask load-img-cb)
  ; (.onDoneCallback meshTask load-img-cb)
  ; (.onDoneCallback meshTask (fn [task] (println "hello matey")))
  ; (.onDoneCallback meshTask (load-img-cb))
  ; (.onSuccess textureTask (fn [task]
  ;                           (println "loaded: task=" task)))
  ; (set! textureTask.onSuccess (fn [task] (println "two sheds jackson: task=" task)))
  (set! textureTask.onSuccess (load-img-cb))
  ; ; (.onError meshTask (fn [task msg exception] (println "error loading img: " msg)))
  ; (.onErrorCallback textureTask (fn [task msg exception] (println "error loading img: msg=" msg ",exception=" exception)))
  (.load assetsManager)
  ; (set! light1 (js/BABYLON.HemisphericLight.
  ;               "light1"
  ;               (js/BABYLON.Vector3. 1 19 0)
  ;               scene))
  ; (set! (.-diffuseColor imgMat) (js/BABYLON.Texture.))
  (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 10 "height" 10) scene))
  (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
  (.enableInteractions vrHelper)
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  (.setEnabled light1 true))
; var mat = new BABYLON.StandardMaterial("", scene);
; mat.diffuseTexture = new BABYLON.Texture("https://i.imgur.com/ntIgFT6.jpg", scene);


; (defn draw-cube []
;   (println "now in draw-cube")
;   (set! sphere (js/BABYLON.MeshBuilder.CreateSphere.
;                 "sphere"
;                 (js-obj "diameter" 1.2 "bar" 2)
;                 scene))
;   (set! (.-position sphere) (js/BABYLON.Vector3. -2 3 4))
;   (set! (.-material sphere) redMaterial)
;   ; (render-loop2)
;   ; (.outerDoIt2 js/window))
;   (.runRenderLoop engine (fn []
;                            (render-loop))))


; (defn init-panel-scene[render-loop])
(defn init-panel-scene[]
  (set! panel (js/BABYLON.MeshBuilder.CreateBox. "panel"
                                                (js-obj "height" 5 "width" 2 "depth" 0.5)
                                                scene))
  (set! (.-position panel)(js/BABYLON.Vector3. 0 0 3))
  (set! (.-material panel) redMaterial))
  ; (.runRenderLoop engine (fn [] (render-loop))))

(defn run-scene [render-loop]
  (.runRenderLoop engine (fn [] (render-loop))))
;;;;;;;
; (defn render-loop []
;     ; (println "render-loop: camera.rotation="
;     ;          (.-rotation camera) ",rot-q=" (.-deviceRotationQuaterniom camera)
;     ;          ",pos=" (.-position camera) ",pos2=" (.-devicePosition camera))
;     (println "render loop")
;     ; (hello.controller.tick)
;     (.render scene))
;
; (defn -main [& args]
;   ; (activate-app)
;   (println "now in -main")
;   (set! canvas (-> js/document (.getElementById "renderCanvas")))
;   ; (set! (.-width (-> js/document (.getElementById "renderCanvas"))) (* 1.0 (.-innerWidth js/window)))
;   ; (set! (.-height (-> js/document (.getElementById "renderCanvas"))) (* 1.0 (.-innerHeight js/window)))
;   ; (set! (.-height canvas) (.-height js/window))
;   (set! engine (js/BABYLON.Engine. canvas true))
;   (set! scene (js/BABYLON.Scene. engine))
;   ; (js/initVRHelper scene);
;   ; (set! vrHelper (js/getVRHelper))
;   (set! vrHelper (.createDefaultVRExperience scene))
;   ; (js/setupControllers vrHelper)
;   ; (js/setupVRCallbacks vrHelper)
;   ; (hello.kbdController.init scene)
;   (def abc (js/BABYLON.Vector3. 1 2 3))
;   (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
;   (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 10 "height" 10) scene))
;   (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
;   ; (setup-controllers vrHelper)
;   (set! camera (.-webVRCamera vrHelper))
;   ; (set! non-vr-camera (.-deviceOrientationCamera vrHelper))
;   (set! rig (js/BABYLON.Mesh. "rig" scene))
;   ; (set! (.-parent camera) rig)
;   ; (def rig (js/BABYLON.MeshBuilder.CreateBox "rig" (js-obj "width" 0.1 "height" 0.1)))
;   (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 1 0))
;   (def box (js/BABYLON.MeshBuilder.CreateBox "box" {:height 10 :width 2 :depth 0.5 :size 2} scene))
;   (set! (.-scaling box) (js/BABYLON.Vector3. 2 1 1))
;   (set! (.-position box) (js/BABYLON.Vector3. 2 3 -4))
;   (println "box.size= "(.-Size box))
;   (set! (.-rotation box) (js/BABYLON.Vector3. 0 (/ js/Math.PI 4) 0))
;
;   (set! abc (js/BABYLON.Vector3. 9 2 3))
;   (def x (.-x abc))
;   (set! light1 (js/BABYLON.HemisphericLight.
;                 "light1"
;                 (js/BABYLON.Vector3. 1 1 0)
;                 scene))
;   (set! sphere (js/BABYLON.MeshBuilder.CreateSphere.
;                 "sphere"
;                 (js-obj "diameter" 1.2 "bar" 2)
;                 scene))
;   (set! (.-position sphere) (js/BABYLON.Vector3. -2 3 4))
;   (set! (.-material sphere) redMaterial)
;   (.runRenderLoop engine (fn []
;                            (render-loop))))
