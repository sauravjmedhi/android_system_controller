import { StyleSheet } from 'react-native';

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

  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0,0,0,0.85)',
    justifyContent: 'center',
    alignItems: 'center',
  },

  modalBox: {
    width: '85%',
    backgroundColor: '#0a0a0a',
    borderRadius: 12,
    padding: 20,
    borderWidth: 1,
    borderColor: '#00ff99',
  },

  modalTitle: {
    color: '#00ff99',
    fontSize: 16,
    marginBottom: 10,
  },

  modalText: {
    color: '#ccc',
    fontSize: 13,
    lineHeight: 18,
  },

  modalButtonPrimary: {
    flex: 1,
    backgroundColor: '#00ff99',
    padding: 10,
    borderRadius: 6,
    alignItems: 'center',
    marginLeft: 6,
  },

  modalButtonSecondary: {
    flex: 1,
    backgroundColor: '#111',
    padding: 10,
    borderRadius: 6,
    alignItems: 'center',
    marginRight: 6,
    borderWidth: 1,
    borderColor: '#00ff99',
  },

  modalButtonText: {
    color: '#000',
    fontSize: 13,
  },
});

export default styles;
