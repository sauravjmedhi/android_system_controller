import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  TextInput,
  Platform,
  PermissionsAndroid,
  ToastAndroid,
  Modal,
  Pressable,
} from 'react-native';
import styles from './styles';

import { NativeModules } from 'react-native';
const { SleepTimer } = NativeModules;

export default function App() {
  const [callType, setCallType] = useState<'normal' | 'whatsapp'>('normal');
  const [time, setTime] = useState<string>('30');
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [remainingTime, setRemainingTime] = useState<number>(0);
  const [showA11yModal, setShowA11yModal] = useState<boolean>(false);

  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  const requestNotificationPermission = async () => {
    if (Platform.OS !== 'android') return true;

    if (Platform.Version < 33) return true;

    try {
      const granted = await PermissionsAndroid.request(
        PermissionsAndroid.PERMISSIONS.POST_NOTIFICATIONS,
      );

      return granted === PermissionsAndroid.RESULTS.GRANTED;
    } catch (err) {
      console.warn(err);
      return false;
    }
  };

  const syncTimer = async () => {
    const seconds = await SleepTimer.getRemainingTime();

    if (seconds > 0) {
      setRemainingTime(seconds);
      setIsRunning(true);
    } else {
      setRemainingTime(0);
      setIsRunning(false);
    }
  };

  useEffect(() => {
    syncTimer();
  }, []);

  useEffect(() => {
    if (!isRunning) return;

    intervalRef.current = setInterval(() => {
      syncTimer();
    }, 1000);

    return () => {
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    };
  }, [isRunning]);

  useEffect(() => {
    if (remainingTime === 0 && isRunning) {
      setIsRunning(false);
      if (intervalRef.current) {
        clearInterval(intervalRef.current);
      }
    }
  }, [remainingTime]);

  const startTimer = async () => {
    const hasPermission = await requestNotificationPermission();
    if (!hasPermission) {
      ToastAndroid.show(
        'Please enable notification permission to view timer',
        ToastAndroid.SHORT,
      );
      return;
    }

    const hasAccessibility = await SleepTimer.isAccessibilityEnabled();
    if (!hasAccessibility) {
      setShowA11yModal(true);
      return;
    }

    const minutes = parseInt(time) || 1;
    const seconds = minutes * 60;

    SleepTimer.startService(callType, 20);

    setRemainingTime(seconds);
    setIsRunning(true);
  };

  const stopTimer = () => {
    SleepTimer.stopService();

    setIsRunning(false);
    setRemainingTime(0);

    if (intervalRef.current) {
      clearInterval(intervalRef.current);
    }
  };

  const formatTime = (sec: number) => {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  };

  const setPreset = (minutes: number) => {
    if (!isRunning) {
      setTime(String(minutes));
    }
  };

  return (
    <View style={styles.container}>
      {/* HEADER */}
      <Text style={styles.title}>sleep.controller</Text>

      {/* TOGGLE */}
      <View style={styles.toggleContainer}>
        <TouchableOpacity
          style={[
            styles.toggleButton,
            callType === 'normal' && styles.activeToggle,
          ]}
          disabled={isRunning}
          onPress={() => setCallType('normal')}
        >
          <Text style={styles.toggleText}>phone</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[
            styles.toggleButton,
            callType === 'whatsapp' && styles.activeToggle,
          ]}
          disabled={isRunning}
          onPress={() => setCallType('whatsapp')}
        >
          <Text style={styles.toggleText}>whatsapp</Text>
        </TouchableOpacity>
      </View>

      {/* PRESETS */}
      <View style={styles.presetContainer}>
        <TouchableOpacity
          style={styles.presetBtn}
          onPress={() => setPreset(30)}
          disabled={isRunning}
        >
          <Text style={styles.presetText}>30m</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.presetBtn}
          onPress={() => setPreset(45)}
          disabled={isRunning}
        >
          <Text style={styles.presetText}>45m</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={styles.presetBtn}
          onPress={() => setPreset(60)}
          disabled={isRunning}
        >
          <Text style={styles.presetText}>1h</Text>
        </TouchableOpacity>
      </View>

      {/* TIMER INPUT */}
      <View style={styles.timerBox}>
        <Text style={styles.label}>minutes</Text>
        <TextInput
          style={styles.input}
          value={time}
          onChangeText={setTime}
          keyboardType="numeric"
          editable={!isRunning}
        />
      </View>

      {/* LIVE COUNTDOWN */}
      {isRunning && (
        <Text style={styles.countdown}>{formatTime(remainingTime)}</Text>
      )}

      {/* BUTTON */}
      {!isRunning ? (
        <TouchableOpacity style={styles.startButton} onPress={startTimer}>
          <Text style={styles.buttonText}>start</Text>
        </TouchableOpacity>
      ) : (
        <TouchableOpacity style={styles.stopButton} onPress={stopTimer}>
          <Text style={styles.buttonText}>end timer</Text>
        </TouchableOpacity>
      )}

      <Modal visible={showA11yModal} transparent animationType="fade">
        <View style={styles.modalOverlay}>
          <View style={styles.modalBox}>
            <Text style={styles.modalTitle}>Accessibility Required</Text>

            <Text style={styles.modalText}>
              This app uses Accessibility Service to automatically end calls
              when the timer finishes.
              {'\n\n'}
              No personal data is collected or stored.
            </Text>

            <View style={{ flexDirection: 'row', marginTop: 16 }}>
              <Pressable
                style={styles.modalButtonSecondary}
                onPress={() => setShowA11yModal(false)}
              >
                <Text style={[styles.modalButtonText, { color: '#aaa' }]}>
                  Cancel
                </Text>
              </Pressable>

              <Pressable
                style={styles.modalButtonPrimary}
                onPress={() => {
                  setShowA11yModal(false);
                  SleepTimer.openAccessibilitySettings();
                }}
              >
                <Text style={styles.modalButtonText}>Enable</Text>
              </Pressable>
            </View>
          </View>
        </View>
      </Modal>
    </View>
  );
}
