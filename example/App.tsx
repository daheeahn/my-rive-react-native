import { hello_dahee } from "my-rive-react-native";
import { Text, View } from "react-native";

export default function App() {
  return (
    <View
      style={{
        flex: 1,
        backgroundColor: "skyblue",
        justifyContent: "center",
        alignItems: "center",
      }}
    >
      <Text>{hello_dahee()}</Text>
    </View>
  );
}
