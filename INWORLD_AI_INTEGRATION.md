# Inworld AI Integration Guide

## Overview

The Afya Assistant web app now integrates **Inworld AI** for conversational voice capabilities. This allows Community Health Workers to have intelligent, context-aware conversations with an AI assistant while capturing patient information.

## Features

✅ **Conversational AI Voice Mode** - Switch between standard speech recognition and Inworld AI powered conversations  
✅ **Intelligent Responses** - AI provides context-aware feedback based on patient information  
✅ **Seamless Integration** - Works alongside symptom extraction and clinical decision support  
✅ **Easy Configuration** - Simple settings panel for API credentials  

## Setup Instructions

### 1. Get Your Inworld AI Credentials

1. Go to [studio.inworld.ai](https://studio.inworld.ai)
2. Create an account or sign in
3. Create a new workspace
4. Create a character for health consultations
5. Get your API key, Workspace ID, and Character ID from your project settings

### 2. Configure in Afya Assistant

1. **Open the app** at `http://localhost:5500`
2. **Sign in as CHW** and navigate to any visit
3. **Click the microchip icon** (⚙️) in the header to open AI Settings
4. **Enter your credentials:**
   - **API Key**: Your Inworld API key (keep this secret!)
   - **Workspace ID**: Your workspace identifier
   - **Character ID**: Your character's ID
5. **Click "Save"** - settings are stored locally

### 3. Use Inworld AI Mode

1. **Start a patient visit**
2. **On the Capture screen**, check the box: "Use AI conversational mode (Inworld)"
3. **Tap the microphone button** to start recording
4. **Speak naturally** - describe the patient's symptoms
5. **See AI responses** appear in blue below your transcript
6. **Continue capturing symptoms** - they're automatically extracted
7. **Continue to the next step** when done

## How It Works

### Voice Capture Flow

```
1. CHW taps microphone button
2. Browser captures speech via Web Speech API
3. Transcript sent to Inworld AI (if enabled)
4. AI generates intelligent response
5. Response displayed in conversational format
6. Symptoms automatically extracted from speech
7. CHW continues with next patient steps
```

### Key Components

**Voice Button States:**
- **🎤 Blue (listening)** - Recording in Inworld mode
- **🎤 Recording pulse** - Standard speech recognition mode
- **Hover state** - Ready to record

**Inworld AI Advantages:**
- Natural language understanding
- Context-aware suggestions
- Multi-turn conversations
- Better handling of medical terminology
- Real-time feedback to CHW

## Configuration Storage

Settings are stored in browser **localStorage** under key: `afya_inworld_settings`

```javascript
{
  "apiKey": "your-api-key-here",
  "workspaceId": "workspace-123",
  "characterId": "character-456"
}
```

**Security Note:** Store your API key securely. Consider using a backend proxy for production deployments.

## Troubleshooting

### Issue: "Inworld API key not configured"
**Solution:** Click the microchip icon in the header and enter your credentials.

### Issue: "Could not connect to Inworld AI"
**Solution:** 
- Check your API key is correct
- Verify Workspace ID and Character ID
- Ensure your network allows API calls to `api.inworld.ai`
- Check browser console for detailed error messages

### Issue: Microphone not working
**Solution:**
- Check browser permissions for microphone access
- Ensure you're using HTTPS or localhost (not file://)
- Try a different browser (Chrome/Edge recommended)
- Check your system microphone settings

### Issue: "No speech heard"
**Solution:**
- Speak closer to the microphone
- Ensure it's not muted at system level
- Reduce background noise
- Try again

## Fallback Behavior

If Inworld AI is unavailable:
1. App automatically switches to standard browser speech recognition
2. All other features (symptom extraction, suggestions) continue normally
3. CHW can work offline without Inworld
4. Try again later when connection is restored

## API Endpoints

The integration calls these Inworld AI endpoints:

- **Session Creation**: `POST /api/v1/session/create`
- **Message Sending**: `POST /api/v1/message/send`

See [Inworld SDK Documentation](https://docs.inworld.ai) for more details.

## Privacy & Data

- **Local Processing**: Speech recognition happens locally in the browser
- **API Calls**: User speech/text is sent to Inworld AI servers
- **Storage**: Configuration keys stored in browser localStorage
- **Data**: Follow Inworld AI's privacy policy for API calls

## Development Notes

### Adding Inworld to Your Setup

The Inworld SDK is loaded via CDN:
```html
<script src="https://sdk.inworld.ai/web/latest/inworld.js"></script>
```

### Integration Points

Key functions for Inworld integration:

```javascript
// Initialize session
async initializeInworldSession()

// Send message to AI
async sendToInworldAI(userMessage)

// Settings management
openInworldSettings()
saveInworldSettings()
closeInworldSettingsModal()

// Voice modes
startVoiceWithInworld()  // Inworld-powered
startVoiceWithBrowserSTT() // Standard recognition
```

### Extending the Integration

To customize Inworld behavior:

1. Modify `sendToInworldAI()` to change API parameters
2. Update prompt/system message in character setup on studio.inworld.ai
3. Add context data to session for better AI responses
4. Implement response parsing for specific information extraction

## Support

- **Inworld Docs**: https://docs.inworld.ai
- **Inworld Studio**: https://studio.inworld.ai
- **Afya Issues**: Check the project README for support channels

## Future Enhancements

- [ ] Audio playback of AI responses (text-to-speech)
- [ ] Multi-language support
- [ ] Session history and learning
- [ ] Custom health worker persona configuration
- [ ] Integration with clinical guidelines
- [ ] Real-time translation
- [ ] Sentiment analysis for patient assessment

---

**Version**: 1.0  
**Last Updated**: February 2026  
**Status**: Experimental - Feedback Welcome!
