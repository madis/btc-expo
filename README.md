# Examples using ClojureScript (shadow-cljs) & Storybook.js together

Files & meanings
```
├── deps.edn
├── package.json
├── package-lock.json
├── Procfile
├── public
│   ├── css
│   ├── index.html         // Base HTML (where the Reagent app will be rendered)
│   └── js                 // Output of the demo app build
├── README.md
├── sb
│   ├── main.css           // Entry to CSS files (processed by esbuild)
│   ├── package.json
│   ├── package-lock.json
│   └── stories            // Configurtion to show Storybook components in Storybook
├── shadow-cljs.edn
└── src
    ├── app                // Demo application
    └── sb                 // Implementation of the Storybook components
```

Start these 2 processes in separate terminal windows:
1. `overmind start`
  - will run the processes defined in the `Procfile` using [overmind](https://github.com/DarthSim/overmind)
2. `npm run storybook`

Then you can see the storybook at http://localhost:6006/ and the app at http://localhost:3000/

## Thanks

This is based on the work by Thomas Heller in https://github.com/thheller/shadow-cljs-storybook-reagent
