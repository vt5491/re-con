(ns re-con.controller
  (:require [babylonjs]
            [re-frame.core :as re-frame]
            [re-con.db :as db]))
            ; [re-con.main-scene :as main-scene]))
;; 'scene' and 'vr-helper' need to be passed to us. It's our hook into main_scene.  We can't require it though
;; due to circular dependencies.
(def scene)
(def vr-helper)
;; helper variable
(def camera)

; (def count 0)
(def isGripping false)
(def gripStartPos)
; (def startCtrlPos)
(def playerStartPos)
(def lastPlayerPos)
(def lastGripVel)
(def gripFactor 1.8)
(def lastGripTime)
; (def vrHelper)
; (def camera)
(def leftController)
(def GRIP_DECELERATION_INT 1000)

; (defn ^:export doIt []
;   (println "tick: doIt")
;   (set! count (+ count 1))
;   count)

;; Separated out because I thought at one time these had to be intialized up VR-Enter event.
;; But now it seems OK to call at scene initialization.
(defn init-vars []
  ; (set! vrHelper (js/getVRHelper))
  ; (set! camera (.-webVRCamera vrHelper))
  ; (set! leftController (.-leftController camera))
  ; (set! lastGripTime (.now js/Date))
  (set! lastGripTime 0)
  (set! lastGripVel (js/BABYLON.Vector3. 0 0 0))
  ; (set! lastPlayerPos (.-position camera))
  (set! lastPlayerPos (.-position camera)))

; (defn init [vrHelper]
;   (setup-vr-callbacks vrHelper))

(defn init [tgt-scene tgt-vr-helper]
  (set! scene tgt-scene)
  (set! vr-helper tgt-vr-helper)
  ; (set! camera (.-currentVRCamera vr-helper))
  (set! camera (.-webVRCamera vr-helper))
  (init-vars))


(defn ^:export tick []
  ; (println "tick2: entry: camera.pos=" (.-position re-con.core/camera))
  (when camera
    (set! leftController (.-leftController camera)))
  ; (println "tick: camera=" camera ",leftController=" leftController)
  (when (and camera leftController)
    (cond
      isGripping
      (let [ctrlDeltaPos (-> (.-devicePosition leftController) (.subtract gripStartPos) (.multiplyByFloats gripFactor gripFactor gripFactor))
            newPos (.subtract (.-position camera) ctrlDeltaPos)]
        ; (println "tick: isGripping")
        ; (def ctrlDeltaPos (-> (.-devicePosition leftController) (.subtract gripStartPos) (.multiplyByFloats gripFactor gripFactor gripFactor)))
        ; (def newPos (.subtract (.-position camera) ctrlDeltaPos))
        (set! (.-position camera) newPos)
        (set! lastGripVel (.subtract newPos lastPlayerPos))
        (set! lastPlayerPos (.-position camera)))
      (and (not isGripping) lastGripVel (< (- (.now js/Date)  lastGripTime) 1000))
      (let [deltaTime (- (.now js/Date) lastGripTime)
            velStrength (* 5.6 (- 1.0 (/ deltaTime GRIP_DECELERATION_INT)))
            deltaPos (.multiplyByFloats lastGripVel velStrength velStrength velStrength)]
        ; (println "tick: isSliding")
        ; (def deltaTime (- (.now js/Date) lastGripTime))
        ; (def velStrength (* 5.6 (- 1.0 (/ deltaTime GRIP_DECELERATION_INT))))
        ; (def deltaPos (.multiplyByFloats lastGripVel velStrength velStrength velStrength))
        (set! (.-position camera) (.add (.-position camera) deltaPos))))))
  ; (println "tick2: exit: camera.pos=" (.-position re-con.core/camera)))
;
  ; function setupVRCallbacks(vrHelper) {}
  ;   vrHelper.onEnteringVR.add(() => {})
  ;     console.log("Entered VR");
  ;     utils_vrHelper = vrHelper;
  ;     utils_camera = vrHelper.webVRCamera;
  ;     if (utils_camera.leftController) {}
  ;       utils_leftController = utils_camera.leftController;
  ;
  ;     hello.controller.init();
  ;     console.log("camera=");
  ;     console.log(vrHelper.webVRCamera);
  ;   // (def camera (.-webVRCamera vrHelper))
  ;    // (.log js/console camera)
  ;       // scene.activeCamera.position = new BABYLON.Vector3(1, 1, 1));

(defn enter-vr-cb []
  (println "entered VR")
  (init-vars))

(defn ^:export setup-vr-callbacks [vrHelper]
  (-> vrHelper (.-onEnteringVR) (.add enter-vr-cb)))



    ; webVRController.onSecondaryTriggerStateChangedObservable.add((stateObject) => {
    ;
    ;   if (stateObject.pressed) {}
    ;     if(utils_camera.leftController && !hello.controller.isGripping){}
    ;       hello.controller.gripStartPos = utils_vrHelper.webVRCamera.leftController.devicePosition.clone()
    ;       hello.controller.isGripping = true;
    ;       hello.controller.playerStartPos = utils_camera.position;
    ;       hello.controller.lastGripTime = Date.now();
    ;   else {}
    ;     hello.controller.isGripping = false));

; (defn trigger-handler [stateObject]
;   (if (.-pressed stateObject)
;     (do
;       (println "rear-trigger-handler pressed")
;       (if (not (:trigger-pressed db/default-db))
;         (do
;           (println "now toggling trigger state to true")
;           ; (re-frame/dispatch [:toggle-trigger])
;           (re-frame/dispatch [:trigger-change true]))
;         ()))
;     (do
;       (println "trigger-handler released")
;       (if (:trigger-pressed db/default-db)
;         (do
;           (println "now toggling trigger state to false"))
;           ; (re-frame/dispatch [:toggle-trigger]))
;         ())))
;   (println "trigger-pressed=" (:trigger-pressed db/default-db) ",db=" db/default-db))
;
; (defn trigger-handler-2 [stateObject]
;   (re-frame/dispatch [:toggle-trigger]))
; window.addEventListener("click", function () {})
(defn click-handler [] (fn []
                         (println "controler.cljs: click detected")))

; (defn trigger-handler [stateObject]
;   (println "trigger-handler: db[trigger-pressed]=" (:trigger-pressed re-con.db))
;   (if (and (.-pressed stateObject) (not (:trigger-pressed re-con.db)))
;     (re-frame/dispatch [:trigger-change true])
;     (re-frame/dispatch [:trigger-change false])))

(defn trigger-handler [stateObject]
  ;; simply promote to a re-frame method so we can have access to the db.
  (re-frame/dispatch [:trigger-handler-2 stateObject]))

(defn grip-handler [stateObject]
  (println "now in cljs side-trigger-handler")
  (if (.-pressed stateObject)
    ; (when (and scene.camera.leftController (not (isGripping))))
    ; (when (and (-> scene .-camera .-leftController) (not (isGripping))))
    (when (and (-> camera .-leftController) (not isGripping))
      ; (set! gripStartPos (-> vrHelper (.-webVRCamera) (.-leftController) (.-devicePosition) (.clone)))
      (set! gripStartPos (-> camera (.-leftController) (.-devicePosition) (.clone)))
      (set! isGripping true)
      (set! playerStartPos (.-position camera))
      (set! lastGripTime (.now js/Date)))
    (set! isGripping false)))

(defn controller-mesh-loaded-handler [webVRController]
  (println "now in controller-mesh-loaded-handler")
  ; (.add (.-onSecondaryTriggerStateChangedObservable webVRController) trigger-handler))
  (-> webVRController (.-onTriggerStateChangedObservable) (.add trigger-handler))
  (-> webVRController (.-onSecondaryTriggerStateChangedObservable) (.add grip-handler))
  (js/window (.addEventListener "onclick" click-handler)))

(defn setup-controller-handlers [vrHelper]
  (println "setup-controler-handlers: entered")
  (-> vrHelper (.-onControllerMeshLoaded) (.add controller-mesh-loaded-handler)))

;
; (defn setup-trigger-handler []
;   ())
