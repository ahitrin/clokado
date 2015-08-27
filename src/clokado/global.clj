(ns clokado.global
  (:use clokado.core
        clokado.graphviz
        [taoensso.nippy :as nip]))

"Simpler functions, seems more useful for REPL evaluation & instant redraw"

(def tree (ref []))
(def filename (ref ""))

(defn redraw! []
  (to-png @tree @filename))

(defn dump! []
  (with-open [f (java.io.DataOutputStream. (java.io.FileOutputStream. @filename))]
    (nip/freeze-to-out! f @tree)))

(defn load! [file]
  (dosync
    (ref-set tree (with-open [f (java.io.DataInputStream. (java.io.FileInputStream. file))]
                    (nip/thaw-from-in! f)))
    (ref-set filename file))
  (redraw!))

(defn mikado! [name file]
  (dosync (ref-set tree (mikado name))
          (ref-set filename file))
  (dump!)
  (redraw!))

(defn add!
  ([name] (dosync (alter tree add name) (dump!) (redraw!)))
  ([name id] (dosync (alter tree add name id) (dump!) (redraw!))))

(defn top! []
  (top @tree))

(defn rename! [id new-name]
  (dosync (alter tree rename id new-name) (dump!) (redraw!)))

(defn close! [id]
  (dosync (alter tree close id) (dump!) (redraw!)))

(defn reopen! [id]
  (dosync (alter tree reopen id) (dump!) (redraw!)))

(defn delete! [id]
  (dosync (alter tree delete id) (dump!) (redraw!)))

(defn link! [a b]
  (dosync (alter tree link a b) (dump!) (redraw!)))

(defn unlink! [a b]
  (dosync (alter tree unlink a b) (dump!) (redraw!)))

(defn insert! [name a b]
  (dosync (alter tree insert name a b) (dump!) (redraw!)))
