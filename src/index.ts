import { registerPlugin } from '@capacitor/core';

import type { CustomHttpPlugin } from './definitions';

const CustomHttp = registerPlugin<CustomHttpPlugin>('CustomHttp', {
  web: () => import('./web').then((m) => new m.CustomHttpWeb()),
});

export * from './definitions';
export { CustomHttp };
