import { registerPlugin } from '@capacitor/core';
import { CustomHttpPlugin } from './definitions';

const NativeHttp =  registerPlugin<CustomHttpPlugin>('CustomHttp', {
    web: () => import('./web').then((m) => new m.CustomHttpPluginWeb(),
)});


export * from './definitions';
export { NativeHttp };
