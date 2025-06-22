css: npx esbuild sb/main.css --bundle --outfile=public/css/main.css --loader:.woff=file --loader:.woff2=file --loader:.ttf=file --watch
cljs_server: npx shadow-cljs server
# storybook_js: cd sb && npm run storybook
storybook_cljs: sleep 10 && npx shadow-cljs watch storybook
ui: sleep 10 && npx shadow-cljs watch ui
server: sleep 10 && npx shadow-cljs watch server
browser_tests: sleep 10 && npx shadow-cljs watch browser-tests # To run: open http://localhost:8000
server_tests: sleep 10 && npx shadow-cljs watch server-tests # To run: node out/server_tests.js
