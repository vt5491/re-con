(defproject re-con "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.597"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [thheller/shadow-cljs "2.8.80"]
                 [reagent "0.8.1"]
                 [re-frame "0.10.9"]
                 ;;vt add
                 [funcool/promesa "5.0.0"]]
                 ; [roman01la/cljs-async-await "1.0.0"]]
                 ; [babylonjs-gui "4.0.3"]]

  :plugins [
            [lein-shell "0.5.0"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj" "src/cljs"]

  ;;vt add for re-frame-10x
  ; :compiler    {
  ;                :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
  ;                :preloads             [day8.re-frame-10x.preload]
  ;                :main                 "re-con.core"}
  ;;vt end
  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]


  :shell {:commands {"open" {:windows ["cmd" "/c" "start"]
                             :macosx  "open"
                             :linux   "xdg-open"}}}

  :aliases {"dev"          ["with-profile" "dev" "do"
                            ["clean"]
                            ["run" "-m" "shadow.cljs.devtools.cli" "watch" "app"]]
            "prod"         ["with-profile" "prod" "do"
                            ["clean"]
                            ["run" "-m" "shadow.cljs.devtools.cli" "release" "app"]]
            "build-report" ["with-profile" "prod" "do"
                            ["clean"]
                            ["run" "-m" "shadow.cljs.devtools.cli" "run" "shadow.cljs.build-report" "app" "target/build-report.html"]
                            ["shell" "open" "target/build-report.html"]]
            "karma"        ["with-profile" "prod" "do"
                            ["clean"]
                            ["run" "-m" "shadow.cljs.devtools.cli" "compile" "karma-test"]
                            ["shell" "karma" "start" "--single-run" "--reporters" "junit,dots"]]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.11"]
   ;; vt add for re-frame-10x
                   [day8.re-frame/re-frame-10x "0.4.7"]
                   [day8.re-frame/tracing "0.5.3"]]}
    ; :compiler    {
    ;               :closure-defines      {"re_frame.trace.trace_enabled_QMARK_" true}
    ;               :preloads             [day8.re-frame-10x.preload]
    ;               :main                 "re-con.core"}}


   :prod {}})
