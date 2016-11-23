(ns learnreagentdatascript.core
  (:require [reagent.core :as reagent :refer [atom]]
            [datascript.core :as d]))

(enable-console-print!)

(println "This text is printed from src/learnreagentdatascript/core.cljs. Go ahead and edit it and see reloading in action.")

;; define your app data so that it doesn't get over-written on reload

(defonce app-state (atom {:number-users 0}))

;; Creates a Datascript "connection"
;; (really an atom with the current DB value
(def conn (d/create-conn {}))

;; Add some data
(d/transact! conn
             [{:name "Rex" :age 3 :sex :m
               :breed "Alsatian" :owner "Marco Polo"}
              {:name "Sally" :age 4 :sex :f
               :breed "Yorkshire Terrier" :owner "Mrs Pollywell"}
              {:name "Snowy" :age 2 :sex :m
               :breed "Wire Fox Terrier" :owner "Tintin"}
              {:name "Same" :age 6 :sex :f
               :breed "Basset Hound" :owner "Marco Polo"}
              {:name "Same" :age 6 :sex :f
               :breed "Alsatian" :owner "Guy Fawkes"}])

(def q-unique-dogs '[:find ?n ?e :where [?e :name ?n]])

(defn new-user! []
  (swap! app-state update-in [:number-users] inc))

(defn woofie []
  (let [numdogs (:number-users @app-state)]
    [:div 
     [:header [:h1 "Welcome to Woofie"] [:p "The dog social networkd"]]

     [:div {:class "site-info"}
      [:p (str "Number of dogs on site: " (:number-users @app-state))]]

     [:div {:class "user-tools"}
      [:button {:on-click new-user!} "Register"]]

     [:div {:class "members"}
      [:h3 "Members"]
      [:ul 
       (map 
        (fn [n] [:li (str (n 0))])
        (d/q q-unique-dogs @conn))]]

     ]))

(reagent/render-component [woofie]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
