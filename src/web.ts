import {  WebPlugin } from '@capacitor/core';
import { CustomHttpPlugin } from './definitions';

export class CustomHttpPluginWeb extends WebPlugin implements CustomHttpPlugin {

  constructor() {
    super({
      name: 'CustomHttpPlugin',
      platforms: ['web']
    });
  }

  post<T>(args: { url: string; body: any; options: {}; }): Promise<T> {
    throw new Error(`No web implementation ${args}`);
  }
}