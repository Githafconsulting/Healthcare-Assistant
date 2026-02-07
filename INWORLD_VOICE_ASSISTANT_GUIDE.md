# Inworld AI Integration for Voice Assistant

This guide explains how to use Inworld AI with the Afya Voice Assistant for intelligent conversational healthcare support.

## What is Inworld AI?

Inworld AI provides:
- **Natural Conversation** - AI understands context and medical terminology
- **Multi-turn Dialogue** - Remembers conversation history
- **Voice Optimization** - Built for speech-based interactions
- **Customizable Characters** - Create healthcare-specific personalities
- **Free Tier** - 10,000 API calls/month at no cost

## Quick Start (5 minutes)

### Step 1: Get Inworld Credentials

1. Go to [inworld.ai](https://inworld.ai)
2. Click "Get Started Free"
3. Create your account and verify email
4. Go to [studio.inworld.ai](https://studio.inworld.ai)
5. Create a new workspace
6. Create a new character (name: "Health Assistant")
7. Click on your character and go to **Settings → API Keys**
8. Copy:
   - **API Key** (starts with `sk_...`)
   - **Workspace ID**
   - **Character ID**

### Step 2: Add to Voice Assistant

1. Open Afya at `http://localhost:5173`
2. Navigate to **Voice Assistant** (left sidebar)
3. Click the **Configure AI** button (gear icon, top right)
4. Paste your credentials:
   - API Key
   - Workspace ID
   - Character ID
5. Click **Save Settings**
6. You'll see **Configured ✓** when complete

### Step 3: Start Using It

1. Type or speak a question: *"Patient has fever for 3 days"*
2. Press Send or wait for microphone to finish
3. Inworld AI responds with intelligent follow-up questions
4. Continue conversation naturally
5. Responses are automatically spoken back to you

## Inworld API Overview

### Endpoints Used

```
POST https://api.inworld.ai/v1/sessions:create
- Creates a conversation session
- Required: API Key, Workspace ID, Character ID

POST https://api.inworld.ai/v1/sessions/{sessionId}:sendText
- Sends a message and gets AI response
- Required: API Key, Session ID, Message text
```

### Response Example

```json
{
  "routing": {
    "source": {
      "name": "CHARACTER"
    }
  },
  "text": {
    "text": "I understand, a three-day fever is important. Is the fever continuous or does it come and go?"
  }
}
```

## Character Setup Recommendations

When creating your Inworld character, use this system prompt template:

```
You are a supportive health consultation assistant helping Community Health 
Workers (CHWs) in rural Africa assess patients and document symptoms accurately.

Your role is to:
1. Ask clarifying questions about patient symptoms naturally
2. Understand medical terminology (fever, diarrhea, cough, etc.)
3. Confirm understanding of the patient condition
4. Ask follow-ups about:
   - Duration ("How long has this been happening?")
   - Severity ("Can the patient eat and drink?")
   - Associated symptoms ("Any cough or rash?")
   - Danger signs ("Difficulty breathing? Unusual tiredness?")
5. Provide supportive feedback while being thorough

IMPORTANT:
- You do NOT diagnose - this is for CHWs to assess severity
- Be brief and practical (CHWs have limited time)
- Ask 1-2 questions at a time, never overwhelming
- Always confirm information you've heard
- Be warm and supportive
- Escalate danger signs appropriately
```

## How It Works

### Session Flow

```
┌─────────────────────────────────────────────┐
│  1. User enters credentials and clicks Save │
│     (stored in browser localStorage)         │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  2. User types/speaks message               │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  3. Frontend initializes Inworld session    │
│     (if not already done)                   │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  4. Message sent to Inworld API             │
│     OPTIONS:                                 │
│     - Direct (frontend) - Fast              │
│     - Via Backend - More Secure (production)│
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  5. Inworld AI processes and responds       │
│     - Understands context                   │
│     - Generates intelligent reply           │
│     - Returns natural conversation          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│  6. Response displayed and spoken aloud     │
│     - Chat bubble appears                   │
│     - Text-to-speech plays voice            │
│     - Ready for next user input             │
└─────────────────────────────────────────────┘
```

## Feature Comparison

### Inworld AI vs Fallback

| Feature | Inworld | Fallback |
|---------|---------|----------|
| Conversation quality | ⭐⭐⭐⭐⭐ Excellent | ⭐⭐ Basic |
| Context awareness | ✓ Yes | ✗ No |
| Medical terminology | ✓ Understands | ✓ Basic |
| Cost | Free (10k/month) | Free |
| Setup | 5 minutes | Instant |
| Customizable | ✓ Full control | ✗ Fixed |
| Offline capable | ✗ Needs internet | ✗ Needs internet |

## Pricing & Limits

### Free Tier
- **10,000 API calls/month** - Plenty for 100+ patient conversations daily
- **Multiple characters** - Create variants for pediatrics, maternal, etc.
- **Full API access** - Same as paid plans
- **No credit card required**

### Usage
Typical conversation uses ~10-20 API calls total:
1. Session creation: 1 call
2. Per message exchange: ~2-4 calls
3. Average 5-turn conversation: 15-20 calls total

**10,000 calls = ~500-1000 conversations/month**

## Backend Proxy Security

For production, route Inworld requests through your backend:

```python
# Backend receives request with API key
POST /api/inworld/chat
{
  "message": "Patient has fever",
  "apiKey": "sk_...",
  "workspaceId": "...",
  "characterId": "..."
}

# Backend proxies to Inworld with secure headers
# Frontend never directly exposes credentials
```

To enable backend proxy, update React code:

```typescript
const response = await client.sendMessageViaBackend(
  userInput,
  'http://localhost:5000/api' // Your backend
);
```

## Troubleshooting

### "Could not connect to Inworld AI"
1. Check API key is correct (from studio.inworld.ai)
2. Verify Workspace ID matches your workspace
3. Verify Character ID matches your character
4. Ensure character is published/active
5. Check network connection

### "Session creation failed"
- Credentials might be expired
- Character might be deleted
- Workspace might be inactive
- Try creating new API key

### Falling back to basic responses
- Inworld is configured but temporarily unavailable
- Check browser console for error details
- Verify API keys in Settings
- Check Inworld API status at status.inworld.ai

### Microphone not recognized
- Check browser microphone permissions
- Try a different browser (Chrome/Edge work best)
- Ensure HTTPS in production (HTTP ok for localhost)

## Next Steps

1. ✅ **Create Inworld account** - Go to inworld.ai
2. ✅ **Set up character** - Create a health assistant
3. ✅ **Get credentials** - Copy API key, workspace, character
4. ✅ **Configure in Afya** - Paste into Settings
5. ✅ **Test conversation** - Ask about patient symptoms
6. ✅ **Customize character** - Edit system prompt in Inworld Studio

## Resources

- **Inworld Studio**: https://studio.inworld.ai
- **Inworld Docs**: https://docs.inworld.ai/
- **API Reference**: https://docs.inworld.ai/api-reference
- **Free Account**: https://inworld.ai/get-started

## Security Notes

⚠️ **Important:**
- API keys stored in browser localStorage (development only)
- For production, use backend proxy (included in voice_api.py)
- Never commit credentials to git
- Rotate keys periodically
- Consider rate limiting in production

## Support

For issues:
1. Check Inworld status: status.inworld.ai
2. Review browser console (F12) for errors
3. Verify character is active in Inworld Studio
4. Test API directly: https://docs.inworld.api/

