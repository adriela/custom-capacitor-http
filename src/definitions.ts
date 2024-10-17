import { PluginImplementations } from "@capacitor/core";
export interface CustomHttpPlugin extends PluginImplementations{
  post<T>(args: {url: string, body: any, options: {}}): Promise<T>
}