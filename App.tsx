import React, { useEffect } from 'react';
import { Button, NativeModules, PermissionsAndroid, View } from 'react-native';

const { SleepTimer } = NativeModules;

export default function App() {
  useEffect(() => {
    const requestPermission = async () => {
      await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
      );
    };

    requestPermission();
  }, []);

  return (
    <View style={{ marginTop: 100 }}>
      <Button
        title="Start Timer"
        onPress={() => {
          console.log('Sleep timer clicked');
          SleepTimer.startService();
        }}
      />
    </View>
  );
}
