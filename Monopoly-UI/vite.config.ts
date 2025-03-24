import {UserConfigFn} from 'vite';
import {overrideVaadinConfig} from './vite.generated';
import wasm from "vite-plugin-wasm";
import topLevelAwait from "vite-plugin-top-level-await";

const customConfig: UserConfigFn = (env) => ({
    assetsInclude: ['**/*.glb'],
    plugins: [
        wasm(),
        topLevelAwait()
    ]
});

export default overrideVaadinConfig(customConfig);
