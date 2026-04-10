(ns app.ui.pages.hodl-invoice.helpers)

(defn step-key
  [role-step & deeper-keys]
  (into [:hodl-invoice :steps-progress role-step] deeper-keys))

(defn step-data
  [db role-step & deeper-keys]
  (get-in db (apply step-key (into [role-step] deeper-keys))))
