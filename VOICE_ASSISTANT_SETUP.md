# Voice Assistant Setup Guide

Complete setup for the AI-powered voice conversational assistant in Afya Healthcare Assistant.

## 📋 Overview

The voice assistant provides:
- **Speech-to-Text** via Web Speech API (free, built-in)
- **AI Conversation** via multiple free/paid services
- **Text-to-Speech** via Web Speech API (free, built-in)
- **Clinical Context** for healthcare guidance

## 🚀 Quick Start

### 1. Web App Setup (React)

```bash
cd afya-react
npm install
npm run dev
```

Navigate to `http://localhost:5173` and click "Voice Assistant" in the sidebar.

### 2. Backend API Setup

```bash
cd cloud
pip install -r requirements.txt
cp .env.example .env
# Configure your API keys in .env
python voice_api.py
```

The API will run on `http://localhost:5000`

## 🔑 Free vs Paid Options

### Option 1: **Hugging Face (Free)** ⭐ Recommended for Getting Started

**No credit card required!**

1. Sign up: https://huggingface.co/join
2. Create API token: https://huggingface.co/settings/tokens
3. Set in `.env`:
   ```
   HUGGINGFACE_API_TOKEN=hf_xxxxxxxxxxxxx
   ```

Models available:
- `microsoft/DialoGPT-medium` - Good for conversations
- `facebook/blenderbot-400M-distill` - Better contextual responses

**Pros:**
- Free tier with no credit card
- Multiple models available
- Fast inference
- 30,000 API calls/month (free)

**Cons:**
- Requires internet connection
- Free tier has rate limits

### Option 2: **OpenAI API** (Paid, but powerful)

**Get $5 free credit!**

1. Sign up: https://platform.openai.com
2. Add payment method (optional for free credits)
3. Create API key: https://platform.openai.com/account/api-keys
4. Set in `.env`:
   ```
   OPENAI_API_KEY=sk_xxxxxxxxxxxxx
   ```

**Pricing:**
- GPT-3.5-turbo: $0.0015 per 1K tokens (input), $0.002 per 1K tokens (output)
- Free $5 credit (3 months)

**Pros:**
- Most capable model (GPT-4 available)
- Excellent for clinical guidance
- Fast responses
- Best for production

**Cons:**
- Paid after free credits
- Requires payment method

### Option 3: **Anthropic Claude** (Paid, specialized)

1. Sign up: https://console.anthropic.com/
2. Add payment method
3. Create API key
4. Set in `.env`:
   ```
   CLAUDE_API_KEY=sk_xxxxxxxxxxxxx
   ```

**Pricing:**
- Claude 3 Haiku: $0.25 per million input tokens, $1.25 per million output tokens
- Pricing varies by model

**Pros:**
- Strong reasoning capabilities
- Good for clinical analysis
- Handles long documents

**Cons:**
- Higher pricing
- Fewer API calls in free tier

### Option 4: **Local/Self-hosted** (Free, advanced)

Run models locally without API costs:

1. Install Ollama: https://ollama.ai/
2. Download a medical model:
   ```bash
   ollama pull llama2:7b
   ollama pull neural-chat
   ```
3. Run the model:
   ```bash
   ollama serve
   ```
4. Update `voice_api.py` to call `http://localhost:11434/api/generate`

**Pros:**
- Completely free
- No API costs
- Private data
- Works offline (after setup)

**Cons:**
- Requires powerful hardware
- Slower responses
- More complex setup

## 📝 Environment File (.env)

Create `.env` in the `cloud` directory:

```env
# Choose ONE primary service:
# 1. Hugging Face (Free)
HUGGINGFACE_API_TOKEN=hf_xxxxxxxxxxxxx

# 2. OpenAI (Paid, $5 free credit)
OPENAI_API_KEY=sk_xxxxxxxxxxxxx

# 3. Claude (Paid)
CLAUDE_API_KEY=sk_xxxxxxxxxxxxx

# Backend Configuration
FLASK_PORT=5000
FLASK_ENV=development

# Frontend Configuration (React)
REACT_APP_API_URL=http://localhost:5000/api
REACT_APP_DEFAULT_SERVICE=huggingface  # huggingface, openai, or claude
```

## 🔧 Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    React Web App                             │
│  (Web Speech API for voice input/output)                    │
└──────────────────────┬──────────────────────────────────────┘
                       │
                       ▼
┌─────────────────────────────────────────────────────────────┐
│                   Flask Voice API                            │
│  (Handles routing to various AI services)                   │
└────┬──────────────────┬──────────────────┬──────────────────┘
     │                  │                  │
     ▼                  ▼                  ▼
┌─────────────┐  ┌─────────────┐  ┌──────────────┐
│ HuggingFace │  │   OpenAI    │  │    Claude    │
│  (Free)     │  │   (Paid)    │  │    (Paid)    │
└─────────────┘  └─────────────┘  └──────────────┘
```

## 🎯 Features by Platform

### Web App (React)
- ✅ Built-in Web Speech API (no dependencies)
- ✅ Real-time speech recognition
- ✅ Auto text-to-speech responses
- ✅ Chat history with timestamps
- ✅ Copy and speak buttons
- ✅ Responsive design

### Mobile App (Android)
- ✅ Speech recognition via Android Speech API
- ✅ Text-to-speech with natural voices
- ✅ Compose UI components
- ✅ Network API calls to backend
- ✅ Offline fallback responses

## 🌐 API Endpoints

### Chat Endpoint
```bash
POST /api/chat
Content-Type: application/json

{
  "message": "What's the treatment for severe malaria?",
  "context": "clinical-decision-support",
  "service": "huggingface"  # optional, defaults to huggingface
}

Response:
{
  "message": "For severe malaria, use IV artesunate...",
  "context": "clinical-decision-support",
  "service": "huggingface"
}
```

### Health Check
```bash
GET /api/health

Response:
{
  "status": "ok",
  "service": "Afya Voice Assistant API"
}
```

## 🔒 Security Considerations

1. **Never commit `.env`** - Add to `.gitignore`
2. **Use environment variables** for API keys
3. **Rate limit API calls** in production
4. **Validate user input** on backend
5. **Use HTTPS** in production
6. **Implement authentication** for production APIs

## 🧪 Testing

Test the voice assistant:

```bash
# Test API locally
curl -X POST http://localhost:5000/api/chat \
  -H "Content-Type: application/json" \
  -d '{"message": "How to treat malaria?", "context": "clinical-decision-support"}'

# Test speech-to-text
# Use the web app at http://localhost:5173
# Click the microphone button and speak
```

## 🚀 Deployment

### Web App (Vercel, Netlify)
```bash
npm run build
# Deploy the dist/ folder
```

### Backend API (Heroku, AWS, Railway)
```bash
# Push to Git
git push heroku main

# Set environment variables
heroku config:set HUGGINGFACE_API_TOKEN=xxx
```

## 📱 Mobile Integration

The Android implementation uses Compose UI and connects to the Flask API.

Key components:
- `VoiceAssistantScreen.kt` - Main UI screen
- Speech recognition via Android's native API
- Text-to-speech via Android TTS engine
- API calls via standard HTTP requests

## 📚 Useful Links

- **Web Speech API**: https://developer.mozilla.org/en-US/docs/Web/API/Web_Speech_API
- **Hugging Face**: https://huggingface.co/docs/api-inference
- **OpenAI**: https://platform.openai.com/docs
- **Claude**: https://docs.anthropic.com/
- **Ollama**: https://github.com/jmorganca/ollama
- **Flask**: https://flask.palletsprojects.com/

## ✅ Troubleshooting

### "Speech Recognition not supported"
- Use Chrome, Edge, or Safari
- Firefox does not support Web Speech API
- Enable microphone permissions

### "API key invalid"
- Check `.env` file formatting
- Verify API key is correct
- Ensure no extra spaces or quotes

### "CORS errors"
- Backend must have `CORS` enabled (included in `voice_api.py`)
- Check frontend and backend URLs match

### "No audio output"
- Check browser volume
- Enable speaker output
- Verify text-to-speech is not disabled in browser

## 💬 Example Queries

Try these in the voice assistant:

- "What are the treatment options for malaria?"
- "What are the danger signs in pregnancy?"
- "How do I manage a high fever?"
- "When should I refer a patient to the facility?"
- "What's the first aid for severe dehydration?"

## 📄 License

All voice assistant code is part of the Afya Healthcare Assistant project.

## 🤝 Support

For issues or questions:
1. Check the troubleshooting section
2. Review API documentation
3. Test with different services
4. Check browser console for errors
