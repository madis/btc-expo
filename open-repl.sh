clojure -Sdeps '{:deps {reply/reply {:mvn/version "0.5.1"}}}' -M -m reply.main --color --attach 3333 --custom-eval "(shadow/repl :ui)"
