import * as React from 'react';

import { MyRiveReactNativeViewProps } from './MyRiveReactNative.types';

export default function MyRiveReactNativeView(props: MyRiveReactNativeViewProps) {
  return (
    <div>
      <span>{props.name}</span>
    </div>
  );
}
