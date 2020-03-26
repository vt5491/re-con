;; Note: macros have to be in .clj or .cljc files, thus this separate file
(ns re-con.macros)

;; example of when-let*
; (when-let* [a 1
;             b 2
;             c (+ a b)]
;            (println "yeah!")
;            c)
; ;;yeah!
(defmacro when-let*
          [bindings & body]
          `(let ~bindings
                (if (and ~@(take-nth 2 bindings))
                  (do ~@body))))
