{:store :database
 :init-in-transaction? false
 :migration-table-name "migration_versions"
 :db {:jdbcUrl (System/getenv "DATABASE_URL")}}
