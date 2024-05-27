import { StyleSheet, Text, View } from 'react-native';

import * as MyRiveReactNative from 'my-rive-react-native';

export default function App() {
  return (
    <View style={styles.container}>
      <Text>{MyRiveReactNative.hello()}</Text>
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#fff',
    alignItems: 'center',
    justifyContent: 'center',
  },
});
