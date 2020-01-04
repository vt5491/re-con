(ns re-con.views
  (:require
   [re-frame.core :as re-frame]
   [re-con.subs :as subs]))


(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     ; [:h1 "Hello from " @name]
     [:button.abc {:on-click #(re-frame/dispatch [:toggle-light])} "abc"]
     [:button.def {:on-click #(re-frame/dispatch [:trigger-change true])} "def"]
     [:br]
     [:button.toggle-trigger {:on-click #(re-frame/dispatch [:toggle-trigger])} "toggle"]
     [:button.change-color {:on-click #(re-frame/dispatch [:toggle-color])} "toggle-color"]
     [:br]
     [:button.print-db {:on-click #(re-frame/dispatch [:print-db])} "print-db"]
     [:button.print-db {:on-click #(re-frame/dispatch [:info])} "action"]
     [:button.print-db {:on-click #(re-frame/dispatch [:action-2])} "action-2"]
     [:canvas {:style {:width 1024 :height 768 :border 5} :id "renderCanvas"}]]))
