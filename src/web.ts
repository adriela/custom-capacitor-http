import { WebPlugin } from '@capacitor/core';
import { Observable } from 'rxjs';

import type { CustomHttpPlugin } from './definitions';

export class CustomHttpWeb extends WebPlugin implements CustomHttpPlugin {
  progressObservable!: Observable<any>;
  post<T>({}): Promise<T> {
    throw new Error('Method not implemented for web.');
  }
}
