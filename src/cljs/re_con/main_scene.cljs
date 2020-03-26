;; main_scene should reference few and be accessible by many
(ns re-con.main-scene
  (:require-macros [re-con.macros :as macros])
  (:require
   [re-frame.core :as re-frame]
    ;; actually don't need to require babylonjs as it's in the js namespace already.
   ; [babylonjs]
   ; [promesa.core :as p :refer-macros [async]]
   [re-con.base :as base]
   [re-con.utils :as utils]
   ; [re-con.macros :as macros]
   [re-con.controller :as controller]
   ; [re-con.controller-xr :as ctrl-xr]
   [re-con.test-scene-ecsy :as test-scene-ecsy]
   [promesa.core :as p]
   [components]
   [systems]
   [ecsy :refer (World System)] ;; works
   [babylonjs-materials :as bjs-m]
   ; ["babylonjs-loaders" :as bjs-l]
   ;; Note: the namespace for babylonjs-loaders is still "js/BABYLON"
   ;; However, we still need to reference the module here so the additional method
   ;; in fact get appended to the "js/BABYLON" namespace (so don't comment this line out)
   [babylonjs-loaders :as bjs-l]))
   ; [async-await.core :refer [async await]]))
   ; [promesa.async-cljs :refer-macros [async]]))
   ; [clojure.core.async :as async :refer :all]))

(def canvas)
(def engine)
(def scene)
(def env)
(def camera)
(def camera-init-pos (js/BABYLON.Vector3. 0 5 -5))
(def vrHelper)
(def xr)
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
(def gui-3d-manager)
(def obj-moving)
(def object-3d)
(def tmp-obj)
(def last-time)
(def cube-entity)
(def world)
(def ray)
(def picking-ray)
(def ray-helper)
(def features-manager)
(def dummy)
(def model-scale-factor 0.01)
; (def model-scale-factor 1.00)
(def skeleton)
(def dribble-range)
(def defeated-range)
(def idle-range)
(def walking-range)

(declare init-part-2)
(declare init-basic-2-2)
; (declare setup-xr-ctrl-cbs)
(declare cast-ray)
(declare pointer-handler)
(declare set-scaling)

(defn mesh-selected [])

(defn dummy-fn []
  (println "m="))

(defn load-img-cb []
  (fn [task]
    (set! (.-diffuseTexture imgMat) (js/BABYLON.Texture. task.texture))))

(defn init []
  (println "now in init-scene")
  ;; following line necessary for mixamo animations.
  (set! js/BABYLON.Animation.AllowMatricesInterpolation true)
  (set! canvas (-> js/document (.getElementById "renderCanvas")))
  (set! engine (js/BABYLON.Engine. canvas true))
  (set! scene (js/BABYLON.Scene. engine))
  (set! env (.-createDefaultEnvironment scene))
  (set! redMaterial (js/BABYLON.StandardMaterial. "redMaterial" scene))
  (set! (.-diffuseColor redMaterial) (js/BABYLON.Color3. 1 0 0))
  (set! blueMaterial (js/BABYLON.StandardMaterial. "blueMaterial" scene))
  (set! (.-diffuseColor blueMaterial) (js/BABYLON.Color3. 0 0 1))
  (set! greenMaterial (js/BABYLON.StandardMaterial. "greenMaterial" scene))
  (set! (.-diffuseColor greenMaterial) (js/BABYLON.Color3. 0 1 0))
  ;; load in a mixamo asset
  ; BABYLON.SceneLoader.Append("./", "duck.gltf", scene, function (scene) {})
  ;   // do something with the scene
  ; (.Append js/BABYLON.SceneLoader "models/jasper/" "jasper.babylon" scene (fn [] (println "jasper loaded")))
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/jasper_2/" "jasper_2.babylon" scene
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/dummy/" "dummy.babylon" scene)
  ; (.ImportMesh bjs-l/glTF2FileLoader "" "models/dummy_glb/" "dummy.glb" scene)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/dummy_glb/" "dummy.glb" scene) ;;works
  (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_boxing/" "ybot_boxing.glb" scene
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot/" "ybot.babylon" scene
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_glb/" "ybot.glb" scene)
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/ybot_glb/" "ybot.gltf" scene
  ;; Note: scaling down in blender seems to have no effect when importing into babylon
  ; (.ImportMesh js/BABYLON.SceneLoader "" "models/jasper_small/" "jasper_small.babylon" scene)
               (fn [new-meshes particle-systems skeletons]
                 (println "new-meshes=" new-meshes)
                 (println "count=" (count new-meshes))
                 ; (doall (map #(.-scaling %1) new-meshes))
                 ; (let [result (map #(.-scaling %1) new-meshes)]
                 ;   (println "result=" result))
                 ; (set! dummy (map #(set! (.-scaling %1) (js/BABYLON.Vector3. 0.1 0.1 0.1)) new-meshes))
                 (doall (map #(set! (.-scaling %1) (js/BABYLON.Vector3. model-scale-factor model-scale-factor model-scale-factor)) new-meshes))
                 (set! skeleton (nth skeletons 0))
                 ; (js-debugger)
                 ; (println "available animations=" (.-animations skeleton))
                 (set! (.-animationPropertiesOverride skeleton) (js/BABYLON.AnimationPropertiesOverride.))
                 (let [animationPropertiesOverride (.-animationPropertiesOverride skeleton)]
                   (set! (.-enableBlending animationPropertiesOverride) true)
                   (set! (.-blendingSpeed animationPropertiesOverride) 0.05)
                   ; (set! (.-loopMode animationPropertiesOverride) 1))
                   (set! (.-loopMode animationPropertiesOverride) 0))
                 ; (set! dribble-range (.getAnimationRange skeleton "dribble"))
                 ; (let [ar (.getAnimationRanges skeleton)]
                 ;   (js-debugger))
                 (println "main_scene: animation ranges=" (.getAnimationRanges skeleton))
                 (println "main_scene: animation groups=" (.-animationGroups scene))
                 ; (.stopAnimation scene skeleton)))
                 ;; comment out the following or specify ".start" to start the animation
                 (-> (nth (.-animationGroups scene) 0) .stop)))
                 ; (set! defeated-range (.getAnimationRange skeleton "defeated"))
                 ; (set! idle-range (.getAnimationRange skeleton "idle"))
                 ; (set! walking-range (.getAnimationRange skeleton "walking"))
                 ; (println "animation range= from:" (.-from defeated-range) ", to:" (.-to defeated-range))
                 ; (println "animation range= from:" (.-from walking-range) ", to:" (.-to walking-range))
                 ; (js-debugger)
                 ; (.beginAnimation scene skeleton (.-from dribble-range) (.-to dribble-range) true)
                 ; (.beginAnimation scene skeleton (.-from defeated-range) (.-to defeated-range) false)))
                 ; (.beginAnimation scene skeleton (.-from 0) (.-to 0) false 2.0)))
                 ; (.beginAnimation skeleton)))
                 ; (.beginAnimation scene skeleton (.-from walking-range) (.-to walking-range) true)))
                 ; (.beginAnimation scene skeleton (.-from idle-range) (.-to idle-range) true)))
     ;             // ROBOT)
     ; skeleton.animationPropertiesOverride = new BABYLON.AnimationPropertiesOverride();
     ; skeleton.animationPropertiesOverride.enableBlending = true;
     ; skeleton.animationPropertiesOverride.blendingSpeed = 0.05 ;
     ; skeleton.animationPropertiesOverride.loopMode = 1);
                 ; (println "dummy=" dummy)
                 ; (let [result (map #(.-scaling %1) new-meshes)]
                 ;   (println "result2=" result))
                 ;(set! dummy (map #(println "mesh=" %1) (seq new-meshes)))
                 ; (println "dummy=" dummy)
                 ; (map dummy-fn (seq new-meshes))
                 ; (set! dummy (reduce + [1 2 3]))
                 ; (reduce #(println "e=" %1 "e2=" %2) [1 2 3])
                 ; (println "end of anon, dummy=" dummy)))
                 ; (loop)))
  ; (.Append js/BABYLON.SceneLoader "models/jasper_small/" "jasper_small.babylon" scene (fn [] (println "jasper_small loaded")))
  ; (.Load js/BABYLON.SceneLoader "models/jasper_small/" "jasper_small.babylon" (fn [] (println "jasper_small loaded")))
  (set! ground (js/BABYLON.MeshBuilder.CreateGround. "ground" (js-obj "width" 10 "height" 10) scene))
  ; (set! (.-material ground) (js/BABYLON.GridMaterial. "mat" scene))
  (set! (.-material ground) (bjs-m/GridMaterial. "mat" scene))
  ; (set! (.-material ground) (bjs-m/SimpleMaterial. "mat" scene))
  (if (not base/use-xr)
    (do
      (println "now setting up vr")
      (set! vrHelper (.createDefaultVRExperience scene (js-obj "useXR" false)))
      ; var camera = new BABYLON.UniversalCamera("UniversalCamera", new BABYLON.Vector3(0, 0, -10), scene);
      (set! camera (.-webVRCamera vrHelper))
      (set! (.-id camera) "main-camera")
      (controller/init scene vrHelper camera)
      (controller/setup-controller-handlers vrHelper)
      ; var ground = BABYLON.MeshBuilder.CreateGround("ground", { width: 1000, height: 1000 }, scene);
      (.enableTeleportation vrHelper (js-obj "floorMeshName" "ground"))
      (.enableInteractions vrHelper)
      (js/setupControllers vrHelper)
      (init-part-2))
    (do
      (println "now setting up xr")
      ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -10) scene))
      ; (set! camera (js/BABYLON.UniversalCamera. "uni-cam" (js/BABYLON.Vector3. 0 5 -10) scene))
      (set! camera (js/BABYLON.UniversalCamera. "uni-cam" camera-init-pos scene))
      ; (println "camera pos (pre)=" (.-position camera))
      (set! (.-y (.-position camera)) 2)
      (set! (.-z (.-position camera)) (* -5 base/scale-factor))
      ; (println "camera pos (post)=" (.-position camera))
      ; (set! (.-id camera) "main-camera")
      ; (set! xr (.createDefaultXRExperienceAsync scene))
      (-> (.createDefaultXRExperienceAsync scene (js-obj "floorMeshes" (array (.-ground env))))
      ; (-> (.createDefaultXRExperienceAsync scene (js-obj))
      ; (-> (.createDefaultXRExperienceAsync scene (js-obj (.-floorMeshes js/BABYLON.WebXRDefaultExperienceOptions) (array ground)))
      ; (-> (.createDefaultXRExperienceAsync scene (js-obj))
          (p/then
           (fn [x] (println "xr-x=" x ",scene=" scene)
             (set! xr x)
             ; (set! (.-pointerSelection xr) false)
             ; (ctrl-xr/setup-xr-ctrl-cbs xr)
             (re-frame/dispatch [:setup-xr-ctrl-cbs xr])
             ; (js-debugger)
             ; (js/BABYLON.WebXRFeaturesManager.disableFeature "xr-controller-pointer-selection")
             (set! features-manager (-> xr (.-baseExperience) (.-featuresManager)))
             ; (.disableFeature features-manager "xr-controller-pointer-selection")
             ; (.enableFeature features-manager "xr-controller-pointer-selection")
             (println "xr features available=" (.GetAvailableFeatures js/BABYLON.WebXRFeaturesManager))
             (println "xr features acitve=" (-> xr (.-baseExperience) (.-featuresManager) (.getEnabledFeatures)))
             (println "POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
             (println "POINTERPICK=" js/BABYLON.PointerEventTypes.POINTERPICK)
             (.registerBeforeRender scene cast-ray)
             (set! tmp-obj (js/BABYLON.MeshBuilder.CreateBox. "tmp-obj"
                                                              (js-obj "height" 0.25, "width" 0.25, "depth" 0.25)
                                                              scene))
             ; (set! (.-position tmp-obj) (js/BABYLON.Vector3. 0 0.5 0))
             (set! (.-position tmp-obj) (js/BABYLON.Vector3. 2 0.5 0))
             (set! (.-material tmp-obj) greenMaterial)
             (set! camera (-> x (.-baseExperience) (.-camera)))
             ; (ctrl-xr/init scene xr)
             (re-frame/dispatch [:init-xr xr])
             (set! ray (js/BABYLON.Ray.))
             (set! ray-helper (js/BABYLON.RayHelper. ray))
             ; (set! ctrl-xr/left-ray ray)
             (.attachToMesh ray-helper tmp-obj (js/BABYLON.Vector3. 0 0 1) (js/BABYLON.Vector3. 0 0 0) 20)
             (.show ray-helper scene js/BABYLON.Color3. 255 0 0)
             (init-part-2)))))) ;; no work if no obj-moving in stanza
             ; (init-basic-2-2))))))
  (println "at end of init"))
                                                     ; (set! camera (js/BABYLON.FreeCamera. "camera" (js/BABYLON.Vector3. 0 5 -20) scene)))))))
                                                     ; (set! camera (-> x (.-baseExperience) (.-camera))))))))


(defn init-part-2 []
  (println "now in init-part-2")
  ;; svale adjustment
  ;; Note: need if not using webxr (browser) extension, comment out if you are.
  (set! (.-rotationQuaternion camera) (-> js/BABYLON.Quaternion (.FromEulerAngles 0 (/ js/Math.PI 1) 0)))
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 0 5 -3) scene))
  ; (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 0 150 -13) scene))
  (.setEnabled light1 true)
  ;; need to have obj-moving for some reason
  (set! obj-moving (js/BABYLON.MeshBuilder.CreateBox.
                    "status-panel"
                    (js-obj "height" 0.2
                            "width" 0.2
                            "depth" 0.1)
                    scene))
  ; (let [mat (js/BABYLON.StandardMaterial.)]
  ;   (set! (.-diffuseColor mat) (js/BABYLON.Color3. 1 1 0))
  ;   (set! (.-material obj-moving) mat))
  ; (init-basic-2-2))
  (.attachControl camera canvas false)
  ; (println "abc=" (-> js/BABYLON.Quaternion (.FromEulerAngles 0 1 0)))
  (set! imgMat (js/BABYLON.StandardMaterial. "imgMat" scene))
  (set! assetsManager (js/BABYLON.AssetsManager. scene))
  (set! textureTask (.addTextureTask assetsManager "load-texture" "imgs/burj_al_arab.jpg"))
  (set! textureTask.onSuccess (load-img-cb))
  (.load assetsManager)
  ; (.enableInteractions vrHelper)
  (set! light1 (js/BABYLON.PointLight. "pointLight" (js/BABYLON.Vector3. 5 5 0) scene))
  (.setEnabled light1 true)
  (if base/use-xr
    (-> (.-onPointerObservable scene) (.add pointer-handler))))
  ; (set! gui-3d-manager (js/BABYLON.GUI.GUI3DManager. scene)))
  ; (.runRenderLoop engine (fn []
  ;                          (let [time (-> (js/performance.now) (/ 1000))
  ;                                delta (- time last-time)]
  ;                            (set! last-time time)
  ;                            (.render scene)))))

(defn init-panel-scene[]
  (set! panel (js/BABYLON.MeshBuilder.CreateBox. "panel"
                                                (js-obj "height" 5 "width" 2 "depth" 0.5)
                                                scene))
  (set! (.-position panel)(js/BABYLON.Vector3. 0 0 3))
  (set! (.-material panel) redMaterial))

(defn run-scene [render-loop]
  (.runRenderLoop engine (fn [] (render-loop))))

;
; function vecToLocal(vector, mesh) {}
; var m = mesh.getWorldMatrix();
; var v = BABYLON.Vector3.TransformCoordinates(vector, m);
; return v;
(defn vec-to-local [vector mesh]
  (let [m (.getWorldMatrix mesh)
        ; v (.TransformCoordinates js/BABYLON.Vector3) vector m
        v (js/BABYLON.Vector3.TransformCoordinates vector m)]
    v))

;; refer to https://www.babylonjs-playground.com/#BCU1XR#329
(defn cast-ray []
  ; (println "now in cast-ray")
  (let [origin (.-position tmp-obj)
        fwd-1 (js/BABYLON.Vector3. 0 0 1)
        fwd-2 (vec-to-local fwd-1 tmp-obj)
        dir (js/BABYLON.Vector3.Normalize (.subtract fwd-2 origin))]
    (set! ray (js/BABYLON.Ray. origin (js/BABYLON.Vector3. 0 0 0) 100))))
  ; (set! picking-ray
  ;   (.createPickingRay scene (.-pointerX scene) (.-pointerY scene) (.Identity js/BABYLON.Matrix) (.-activeCamera scene)))

; this sets up the pointer hooks for xr support
(defn pointer-handler [pointer-info]
  ; (macros/when-let* [a true b true] 7)
  ; (utils/kwd-to-int :7)
                     ; (let [type (.-type pointer-info)
  (macros/when-let* [type (.-type pointer-info)
                     ; picked-mesh (if (and (.-pickInfo pointer-info) (-> pointer-info (.-pickInfo) (.-pickedMesh))))
                     ;  (-> pointer-info (.-pickInfo) (.-pickedMesh))]
                     picked-mesh (-> pointer-info (.-pickInfo) (.-pickedMesh))]
      ; (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (println "POINTER DOWN, picked-mesh.id=" (.-id picked-mesh) ", picked-mesh=" picked-mesh ",
      type=" type ",POINTERDOWN=" js/BABYLON.PointerEventTypes.POINTERDOWN)
      ; (= type js/BABYLON.PointerEventTypes.POINTERPICK)
    (when (re-matches #"panel-\d+" (.-name picked-mesh))
      (cond
        (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
        (do
          ; (println "pointer-handler: POINTER DOWN, picked-mesh=" picked-mesh)
          ; (js-debugger)
          (re-frame/dispatch [:mesh-selected picked-mesh])
          ; (re-frame/dispatch [:trigger-handler (js-obj "pressed" (.-pressed trigger-state))])
          (re-frame/dispatch [:trigger-handler (js-obj "pressed" true)]))
        (= type js/BABYLON.PointerEventTypes.POINTERUP)
        (do
          (re-frame/dispatch [:trigger-handler (js-obj "pressed" false)]))
        :else nil))
    ; (when (re-matches #"game-tile.*" (.-name picked-mesh))
    ;   (println "game-tile selected")
    ;   (cond
    ;     (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
    ;     (do
    ;       (println "about to dispatch game-board-trigger-handler")
    ;       (re-frame/dispatch [:game-board-trigger-handler (js-obj "pressed" true)]))))
    (when (= type js/BABYLON.PointerEventTypes.POINTERDOWN)
      (cond
        (re-matches #"game-tile.*" (.-name picked-mesh))
        (do
          (println "game-tile selected")
          (println "about to dispatch game-board-trigger-handler")
          (re-frame/dispatch [:game-board-trigger-handler (js-obj "pressed" true)]))))))


(defn set-scaling [mesh, s]
  (set! (.-scaling mesh) (js/BABYLON.Vector3. s s s)))
