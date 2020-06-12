(ns doh.events-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [doh.db :as db]
            [doh.events :as e]
            [doh.test-util :as util :refer [valid-db?]]))

(def test-data
  (let [db db/empty-db
        [db shared] (db/add-ingredient db "Shared" false)
        [db a-excl] (db/add-ingredient db "Exclusive A" false)
        [db a] (db/add-mixture db "Mixture A" {shared 10, a-excl 5})
        [db b] (db/add-mixture db "Mixture B" {shared 20})
        [db _] (db/add-recipe db "Test" [a b])]
    {:db db
     :ingr-a-id a-excl
     :ingr-shared-id shared
     :mix-a-id a
     :mix-b-id b}))

(deftest init-db-conforms-spec
  (let [db (e/init-db nil nil)]
    (is (valid-db? db))))

(deftest test-add-part
  (let [{:keys [db mix-b-id]} test-data]
    (testing "adding an exclusive ingredient"
      (let [db (-> db
                   (e/new-part mix-b-id)
                   (e/change-part-name "Exclusive B")
                   (e/change-part-quantity 7)
                   (e/save-part))
            ingredient (get-in db [:db/ingredients (:db/last-id db)])]
        (is (= 3 (count (:db/ingredients db))))
        (is (= "Exclusive B" (:ingredient/name ingredient)))))))

(deftest test-edit-part
  (let [{:keys [db mix-a-id ingr-a-id ingr-shared-id]} test-data]
    
    (testing "updated quantity is saved"
      (let [db (-> db
                   (e/edit-part mix-a-id ingr-a-id)
                   (e/change-part-quantity 7)
                   (e/save-part))]
        (is (= 7 (get-in db [:db/mixtures mix-a-id
                             :mixture/parts ingr-a-id])))))
    
    (testing "editing shared ingredient name creates new ingredient"
      (let [db (-> db
                   (e/edit-part mix-a-id ingr-shared-id)
                   (e/change-part-name "Edited")
                   (e/save-part))]
        (is (= 3 (count (:db/ingredients db))))
        (is (= "Edited" (get-in db [:db/ingredients (:db/last-id db) :ingredient/name])))
        (is (= 2 (count (get-in db [:db/mixtures mix-a-id :mixture/parts]))))))
    
    (testing "editing exclusive ingredient name updates ingredient"
      (let [db (-> db
                   (e/edit-part mix-a-id ingr-a-id)
                   (e/change-part-name "Edited")
                   (e/save-part))]
        (is (= 2 (count (:db/ingredients db))))
        (is (= 2 (count (get-in db [:db/mixtures mix-a-id :mixture/parts]))))))))
