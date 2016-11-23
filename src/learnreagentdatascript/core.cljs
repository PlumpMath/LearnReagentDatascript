(ns learnreagentdatascript.core
  (:require [reagent.core :as reagent :refer [atom]]
            [datascript.core :as d]
            [cljs-uuid-utils.core :as uuid]))

(enable-console-print!)

(println "This text is printed from src/learnreagentdatascript/core.cljs. Go ahead and edit it and see reloading in action.")

(defn bind
  ([conn q]
   (bind conn q (atom nil)))
  ([conn q state]
   (let [k (uuid/make-random-uuid)]
     (reset! state (d/q q @conn))
     (d/listen! conn k (fn [tx-report]
                         (let [novelty (d/q q (:tx-data tx-report))]
                           (when (not-empty novelty) ;; Only update if query results actually changed
                             (reset! state (d/q q (:db-after tx-report)))))))
     (set! (.-__key state) k)
state)))

(defn unbind
  [conn state]
(d/unlisten! conn (.-__key state)))

;; define your app data so that it doesn't get over-written on reload
(defonce app-state (atom {:number-users 0}))

;; Creates a Datascript "connection"
;; (really an atom with the current DB value
(def conn (d/create-conn {}))

;; Add some data
(d/transact! conn
             [{:name "Rex" :age 3 :sex "m"
               :breed "Alsatian" :owner "Marco Polo"}
              {:name "Sally" :age 4 :sex "f"
               :breed "Yorkshire Terrier" :owner "Mrs Pollywell"}
              {:name "Snowy" :age 2 :sex "m"
               :breed "Wire Fox Terrier" :owner "Tintin"}
              {:name "Fido" :age 6 :sex "m"
               :breed "Basset Hound" :owner "Marco Polo"}
              {:name "Same" :age 6 :sex "f"
               :breed "Alsatian" :owner "Guy Fawkes"}])

(defn add-betty! []
  (d/transact! conn [{:name "Betty"
                      :age 3
                      :sex "f"
                      :breed "Wire Fox Terrier"
                      :owner "George Orwell"}]))

(def q-unique-dogs '[:find ?n ?e :where [?e :name ?n]])
(def q-pairings-purebreed '[:find ?m ?f ?e ?a
                            :where [?e :name ?m]
                                   [?e :sex "m"]
                                   [?e :breed ?b]
                                   [?a :name ?f]
                                   [?a :sex "f"]
                                   [?a :breed ?b]])
(def q-dog-names '[:find ?n ?e :where [?e :name ?n]])

(defn new-user! []
  (swap! app-state update-in [:number-users] inc))

(defn woofie []
  (let [dogs (bind conn q-unique-dogs)
        dogpairs (bind conn q-pairings-purebreed)]
    [:div 
     [:header [:h1 "Welcome to Woofie"] [:p "The dog social networkd"]]

     [:div {:class "site-info"}
      [:p (str "Number of dogs on site: " (:number-users @app-state))]]

     [:div {:class "user-tools"}
      [:button {:on-click new-user!} "Register"]
      [:button {:on-click add-betty!} "Add betty"]]

     [:div {:class "members"}
      [:h3 "Members"]
      [:ul 
       (map 
        (fn [n] [:li (str (n 0))])
        @dogs)]]

     [:div {:class "matches"}
      [:h3 "Matches"]
      [:ul
       (map 
        (fn [p] [:li (apply str (p 0) " and " (p 1))])
        @dogpairs)]]

     ]))

(reagent/render-component [woofie]
                          (. js/document (getElementById "app")))

(defn on-js-reload []
  ;; optionally touch your app-state to force rerendering depending on
  ;; your application
  ;; (swap! app-state update-in [:__figwheel_counter] inc)
)
