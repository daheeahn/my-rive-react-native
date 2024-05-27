import { requireNativeViewManager } from 'expo-modules-core';
import * as React from 'react';

import { MyRiveReactNativeViewProps } from './MyRiveReactNative.types';

const NativeView: React.ComponentType<MyRiveReactNativeViewProps> =
  requireNativeViewManager('MyRiveReactNative');

export default function MyRiveReactNativeView(props: MyRiveReactNativeViewProps) {
  return <NativeView {...props} />;
}
