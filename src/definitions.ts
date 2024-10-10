export interface CustomHttpPlugin {
  echo(options: { value: string }): Promise<{ value: string }>;
}
