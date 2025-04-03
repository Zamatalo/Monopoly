import { defineConfig } from 'vite'
import { nodePolyfills } from 'vite-plugin-node-polyfills'
import wasm from "vite-plugin-wasm";
import topLevelAwait from "vite-plugin-top-level-await";

export default defineConfig({
    assetsInclude: ['**/*.glb'],
    plugins: [
        nodePolyfills({
            protocolImports: true,

        }),
        wasm(),
        topLevelAwait()
    ],
    server: {
        port: 3000, // Client port
    },
    build: {
        target: 'esnext' // For RAPIER
    }
})

