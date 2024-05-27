import { NativeModulesProxy, EventEmitter, Subscription } from 'expo-modules-core';

// Import the native module. On web, it will be resolved to MyRiveReactNative.web.ts
// and on native platforms to MyRiveReactNative.ts
import MyRiveReactNativeModule from './MyRiveReactNativeModule';
import MyRiveReactNativeView from './MyRiveReactNativeView';
import { ChangeEventPayload, MyRiveReactNativeViewProps } from './MyRiveReactNative.types';

// Get the native constant value.
export const PI = MyRiveReactNativeModule.PI;

export function hello(): string {
  return MyRiveReactNativeModule.hello();
}

export async function setValueAsync(value: string) {
  return await MyRiveReactNativeModule.setValueAsync(value);
}

const emitter = new EventEmitter(MyRiveReactNativeModule ?? NativeModulesProxy.MyRiveReactNative);

export function addChangeListener(listener: (event: ChangeEventPayload) => void): Subscription {
  return emitter.addListener<ChangeEventPayload>('onChange', listener);
}

export { MyRiveReactNativeView, MyRiveReactNativeViewProps, ChangeEventPayload };
