import { PluginImplementations } from "@capacitor/core";

export interface HttpInfo {
  url: string;
  data: Record<string, any>;
  files: {
    fileName: string,
    base64Data: string
  };
  headers : {
    Authorization: string;
  };
}
export interface CustomHttpPlugin extends PluginImplementations{
  post<T>(data: HttpInfo): Promise<T>;
}