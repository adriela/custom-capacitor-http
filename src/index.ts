import { registerPlugin } from '@capacitor/core';
import type { CustomHttpPlugin } from './definitions';
import { Observable, Subscriber } from 'rxjs';

export * from './definitions';

class CustomHttp {
  
  private taskObserver!: Subscriber<any>;

  upload(url: string, data: any, files: any, options: { headers?: { Authorization: string; }}): Observable<any> {
    const customHttpPlugin = registerPlugin<CustomHttpPlugin>('CustomHttp', {
      web: () => import('./web').then((m) => new m.CustomHttpWeb()),
    });
    const progressObservable = new Observable<any>(subscriber => {
      this.taskObserver = subscriber;
      const progressListener = customHttpPlugin.addListener('progressUpdate', (data: any) => {
          console.log('progressUpdate : ' + data.progress);
          subscriber.next({
            type: 1,
            progress: data.progress
          });
      });

      // Cleanup function to unsubscribe
      return () => {
          progressListener.remove();
      };
    });
    customHttpPlugin.post({url: url,data: data ,files: files ,headers: options.headers!}).then((data: any)=>{
      data.type = 4;
      this.taskObserver.next(data);
      this.taskObserver.complete();
    }).catch((reason: any)=>{
      this.taskObserver.error(reason);
    });
    return progressObservable;
  }
}

export { CustomHttp };
