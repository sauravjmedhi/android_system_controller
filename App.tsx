import React, { useState, useEffect, useRef } from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  TextInput,
  StyleSheet,
} from 'react-native';

import { NativeModules } from 'react-native';
const { SleepTimer } = NativeModules;

export default function App() {
  const [callType, setCallType] = useState<'normal' | 'whatsapp'>('normal');
  const [time, setTime] = useState<string>('30');
  const [isRunning, setIsRunning] = useState<boolean>(false);
  const [remainingTime, setRemainingTime] = useState<number>(0);

  const intervalRef = useRef<ReturnType<typeof setInterval> | null>(null);

  useEffect(() => {
    if (isRunning && remainingTime > 0) {
      intervalRef.current = setInterval(() => {
        setRemainingTime(prev => prev - 1);
      }, 1000);
    }

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

  const startTimer = () => {
    const minutes = parseInt(time) || 1;
    const seconds = minutes * 60;

    SleepTimer.startService(callType, seconds);

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
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#0a0a0a',
    padding: 24,
    justifyContent: 'center',
  },

  title: {
    color: '#00ff9f',
    fontSize: 14,
    marginBottom: 30,
    letterSpacing: 2,
  },

  toggleContainer: {
    flexDirection: 'row',
    marginBottom: 20,
    gap: 10,
  },

  toggleButton: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#333',
    paddingVertical: 10,
    alignItems: 'center',
  },

  activeToggle: {
    borderColor: '#00ff9f',
    backgroundColor: 'rgba(0,255,159,0.08)',
  },

  toggleText: {
    color: '#aaa',
    fontSize: 12,
    letterSpacing: 1,
  },

  presetContainer: {
    flexDirection: 'row',
    marginBottom: 20,
    gap: 10,
  },

  presetBtn: {
    flex: 1,
    borderWidth: 1,
    borderColor: '#333',
    paddingVertical: 8,
    alignItems: 'center',
  },

  presetText: {
    color: '#888',
    fontSize: 11,
  },

  timerBox: {
    marginBottom: 20,
  },

  label: {
    color: '#666',
    fontSize: 10,
    marginBottom: 6,
  },

  input: {
    borderWidth: 1,
    borderColor: '#333',
    color: '#00ff9f',
    padding: 12,
    fontSize: 14,
    letterSpacing: 2,
  },

  countdown: {
    color: '#00ff9f',
    fontSize: 18,
    textAlign: 'center',
    marginBottom: 20,
    letterSpacing: 3,
  },

  startButton: {
    borderWidth: 1,
    borderColor: '#00ff9f',
    padding: 14,
    alignItems: 'center',
  },

  stopButton: {
    borderWidth: 1,
    borderColor: '#ff4d4d',
    padding: 14,
    alignItems: 'center',
  },

  buttonText: {
    color: '#00ff9f',
    fontSize: 12,
    letterSpacing: 2,
  },
});
