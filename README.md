# Bitcoin Expo application

An browser application & HTTP API server to demonstrate some useful things that can be done with Bitcoin.

[![Watch the video](https://raw.githubusercontent.com/madis/btc-expo/master/docs/btc-expo-short-demo.png)](https://raw.githubusercontent.com/madis/btc-expo/master/docs/btc-expo-short-demo.webm)

Technologies & tools used:
  - [ClojureScript](https://clojurescript.org/)
  - [shadow-cljs](https://shadow-cljs.github.io/docs/UsersGuide.html)
  - [re-frame](https://day8.github.io/re-frame/re-frame/) (for UI application architecture and internal communication)
  - PostgreSQL (as storage back-end)
  - React (via [Reagent](https://reagent-project.github.io/))

## Development

First set up a Core Lightning regtest node (or two nodes if you want sender & receiver be different).
The simplest way to do it in local environment is to use [Polar](https://github.com/jamaljsr/polar).

![](docs/polar-topology.png)

Start these 2 processes in separate terminal windows:
1. `overmind start`
  - will run the processes defined in the `Procfile` using [overmind](https://github.com/DarthSim/overmind)
2. `node out/server.js`
  - starts the API server application
3. Open browser: `http://localhost:8008` to try it out
