import { MyRiveReactNativeView } from "my-rive-react-native";
import { View } from "react-native";

export default function App() {
  return (
    <View style={{ flex: 1 }}>
      <MyRiveReactNativeView resourceName="teddy" />
    </View>
  );
}
