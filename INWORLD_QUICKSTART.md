# Quick Start: Inworld AI for Afya Assistant

## 30-Second Setup

1. **Go to** [studio.inworld.ai](https://studio.inworld.ai) and create a free account
2. **Create a workspace** and a character (name it something like "Health Assistant")
3. **Get your credentials:**
   - Go to Settings → API Keys
   - Copy: **API Key**, **Workspace ID**, **Character ID**

4. **Open Afya Assistant** at `http://localhost:5500`
5. **Click the ⚙️ microchip icon** in the top right
6. **Paste your credentials** and click Save
7. **Done!** ✅

## Using Inworld AI

### During a Visit

1. Go to **Capture screen** (after selecting a patient)
2. **Check the box:** "Use AI conversational mode (Inworld)"
3. **Tap the blue microphone button** 🎤
4. **Speak naturally:** *"Patient has fever for 3 days and cough"*
5. **See AI response** appear in blue
6. Keep talking → symptoms auto-extract → AI responds intelligently
7. **Continue →** to next screen when ready

### What the AI Helps With

- 🗣️ **Understands medical speech** - fever, diarrhea, respiratory symptoms
- 💬 **Responds naturally** - "Tell me more about the fever" 
- 🏥 **Context-aware** - "Has the child been eating normally?"
- ✅ **Confirms understanding** - "So the child has had fever for 3 days"
- 🔍 **Extracts structured data** - Auto-fills symptoms, duration, severity

## Example Conversation

```
CHW: "The baby has had fever for two days and is not drinking"

AI: "I understand. A 2-day fever with poor fluid intake is concerning. 
     Can you tell me if the child's urine output has decreased? 
     And is there any diarrhea or vomiting?"

CHW: "Yes, there's diarrhea too, and the urine is dark"

AI: "Thank you. That suggests possible dehydration from diarrhea. 
     Has the child been eating normally? And does the child have 
     any difficulty breathing or unusual tiredness?"

[Symptoms extracted: Fever, Diarrhea, Not drinking → triggers ORS + fluid monitoring suggestions]
```

## Important Notes

⚠️ **API Key Safety:**
- Your key is stored in your browser's local storage
- Don't share it publicly
- For production, use a secure backend to proxy requests

📱 **Offline Mode:**
- Works without Inworld if not configured
- Switches to standard speech recognition automatically
- All clinical features continue working

🎯 **Best Practices:**
- Speak clearly and at normal pace
- Use medical terminology naturally (fever, cough, diarrhea)
- Let the AI finish responding before speaking
- Check extracted symptoms for accuracy

## Troubleshooting

| Problem | Solution |
|---------|----------|
| "Could not connect" | Check API key in settings is correct |
| "No speech heard" | Speak louder, reduce background noise |
| Microphone denied | Allow microphone in browser permissions |
| Not in HTTPS | Use `http://localhost:5500` or deploy with HTTPS |

## Get Your Free Inworld Account

1. Go to [inworld.ai](https://inworld.ai)
2. Click "Get Started Free"
3. Create account
4. Go to [studio.inworld.ai](https://studio.inworld.ai)
5. Create workspace
6. Copy credentials

Free tier includes:
- Up to 10,000 API calls/month
- Multiple characters
- Full conversation API
- Text and voice support

## Next Steps

After setup, try:
- [ ] Record a full patient visit with Inworld enabled
- [ ] Compare results vs standard mode
- [ ] Customize the AI character on studio.inworld.ai (add health expertise)
- [ ] Enable audio playback (text-to-speech) - coming soon
- [ ] Share feedback on what works well

## Feedback

If you find issues or have suggestions:
- Check the error logs in browser developer console (F12)
- Review [INWORLD_AI_INTEGRATION.md](./INWORLD_AI_INTEGRATION.md) for detailed docs
- Test with different browsers (Chrome/Edge/Safari recommended)

---

🎉 **Ready to go!** Your health workers can now have smart conversations while capturing patient data. Enjoy!
