import { WebPlugin } from '@capacitor/core';

import type { CustomHttpPlugin } from './definitions';

export class CustomHttpWeb extends WebPlugin implements CustomHttpPlugin {
  async echo(options: { value: string }): Promise<{ value: string }> {
    console.log('ECHO', options);
    return options;
  }
}
