;; namespace for all things related to a cell slot in the concentration board.  The equivalent
;; of a "brick" in the game of breakout.
;; First created 2019-12-26.
(ns re-con.cell
  (:require
   [re-frame.core :as re-frame]
   [re-con.base :as base]
   [re-con.main-scene :as main-scene]))

(defn load-img-cb []
  (fn [task]))

(defn init-front-img [cell])

(defn load-front-imgs [db]
  (println "load-front-imgs: db=" db)
  (println "load-front-imgs: board=" (db :board))
  ; (for [i (range 1 5)] (println "hi"))
  ; (let [c (db :board)]
  ;   (println "let: c=" c ",count=" (count c) ",first=" (first c))
  ;   (for [i (range 0 10)]
  ;     (println i ", c[i]=" (nth c i)))))
             ; (for [d c])))
      ; (println "d=" d)))
  (doseq [c (db :board)]
    (println "cell=" c)
    (println "cell.front-img=" (get c :front-img))
    (let [task (.addTextureTask main-scene/assetsManager "load-texture" (get c :front-img))]
      (set! task.onSuccess (load-img-cb)))))

(defn load-front-imgs-2 [db]
  (for [c (db :board)]
    (println "c=" c)))

(defn init-board [db row-cnt col-cnt default-img-map]
  (println "init-board: entered")
  (dotimes [i (* row-cnt col-cnt)]
           ; (println "i=" i)
           (re-frame/dispatch [:add-cell {:front-img (default-img-map (keyword (str i)))}]))
  (re-frame/dispatch [:load-front-imgs]))

  ;; now loop over the previously created cell vector, and load and set the fron image
  ; (println "init-board: db.board=" (db :board))
  ; (for [cell (db :board)]
  ;   (println "cell.front-img=" (get cell :front-img))))
