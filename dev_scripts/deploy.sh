# DB migrations
rsync -avz --progress deploy madis@milton:~/www/btc-expo

# Only needed the first time
rsync -avz --progress .tool-versions madis@milton:~/www/btc-expo

# Stylesheets
npx esbuild sb/main.css --bundle --outfile=release/public/css/main.css --loader:.woff=file --loader:.woff2=file --loader:.ttf=file
cp public/index.html release/public
cp package.json release

## Normal deployment consists of these 2:
# 1. UI application
npx shadow-cljs release app
# 2. Server
npx shadow-cljs release server
# 3. Copy app & server assets
rsync -avz --progress release/* madis@milton:~/www/btc-expo
# 4. Restart API server


ssh madis@milton << 'EOF'
  cd www/btc-expo
  npm install
  cd deploy
  clojure -P
  clojure -M:migrate migrate
  pm2 restart btc-expo || pm2 start server.js --name btc-expo
  sudo systemctl restart nginx
EOF

