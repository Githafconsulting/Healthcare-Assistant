from flask import Flask, request, jsonify
from flask_cors import CORS
import os
from dotenv import load_dotenv
import requests
import json

load_dotenv()

app = Flask(__name__)
CORS(app)

# Configuration
HF_API_TOKEN = os.getenv('HUGGINGFACE_API_TOKEN', '')
OPENAI_API_KEY = os.getenv('OPENAI_API_KEY', '')
CLAUDE_API_KEY = os.getenv('CLAUDE_API_KEY', '')
INWORLD_API_BASE = 'https://api.inworld.ai/v1'

# Free Hugging Face models for clinical conversation
HF_MODELS = {
    'dialoai': 'microsoft/DialoGPT-medium',
    'blenderbot': 'facebook/blenderbot-400M-distill',
}

def get_clinical_system_prompt():
    return """You are Afya, a clinical decision support AI assistant for Community Health Workers (CHWs) in resource-limited settings. You provide evidence-based clinical guidance following WHO and local guidelines.

Your role:
- Provide concise, actionable clinical guidance
- Suggest when to refer patients to health facilities
- Help with treatment decisions based on available resources
- Emphasize danger signs and emergency protocols
- Support malaria, fever, respiratory, and nutritional disorders management
- Always prioritize patient safety

Guidelines:
- Use simple, clear language suitable for frontline health workers
- Include specific treatment information when relevant
- Identify danger signs that require immediate referral
- Suggest follow-up schedules
- Consider resource constraints in low-income settings
- Be concise and practical"""

@app.route('/api/health', methods=['GET'])
def health():
    return jsonify({'status': 'ok', 'service': 'Afya Voice Assistant API'})

@app.route('/api/chat', methods=['POST'])
def chat():
    """Handle chat messages with AI responses"""
    try:
        data = request.json
        message = data.get('message', '')
        context = data.get('context', 'clinical-decision-support')
        use_service = data.get('service', 'huggingface')  # huggingface, openai, or claude

        if not message:
            return jsonify({'error': 'Message is required'}), 400

        # Route to appropriate service
        if use_service == 'openai' and OPENAI_API_KEY:
            response = call_openai(message)
        elif use_service == 'claude' and CLAUDE_API_KEY:
            response = call_claude(message)
        else:
            # Default to free HuggingFace
            response = call_huggingface(message)

        return jsonify({
            'message': response,
            'context': context,
            'service': use_service
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

def call_huggingface(message):
    """Call Hugging Face free API using DialoGPT"""
    try:
        if not HF_API_TOKEN:
            return fallback_response(message)

        url = f"https://api-inference.huggingface.co/models/{HF_MODELS['dialoai']}"
        headers = {"Authorization": f"Bearer {HF_API_TOKEN}"}
        payload = {"inputs": message}

        response = requests.post(url, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            if isinstance(result, list) and len(result) > 0:
                return result[0].get('generated_text', fallback_response(message))
        
        return fallback_response(message)

    except Exception as e:
        print(f"HuggingFace API error: {e}")
        return fallback_response(message)

def call_openai(message):
    """Call OpenAI GPT API"""
    try:
        if not OPENAI_API_KEY:
            return fallback_response(message)

        url = "https://api.openai.com/v1/chat/completions"
        headers = {
            "Authorization": f"Bearer {OPENAI_API_KEY}",
            "Content-Type": "application/json"
        }
        payload = {
            "model": "gpt-3.5-turbo",
            "messages": [
                {"role": "system", "content": get_clinical_system_prompt()},
                {"role": "user", "content": message}
            ],
            "temperature": 0.7,
            "max_tokens": 500
        }

        response = requests.post(url, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            return result['choices'][0]['message']['content']
        
        return fallback_response(message)

    except Exception as e:
        print(f"OpenAI API error: {e}")
        return fallback_response(message)

def call_claude(message):
    """Call Anthropic Claude API"""
    try:
        if not CLAUDE_API_KEY:
            return fallback_response(message)

        url = "https://api.anthropic.com/v1/messages"
        headers = {
            "x-api-key": CLAUDE_API_KEY,
            "anthropic-version": "2023-06-01",
            "content-type": "application/json"
        }
        payload = {
            "model": "claude-3-haiku-20240307",
            "max_tokens": 500,
            "system": get_clinical_system_prompt(),
            "messages": [
                {"role": "user", "content": message}
            ]
        }

        response = requests.post(url, headers=headers, json=payload, timeout=30)
        
        if response.status_code == 200:
            result = response.json()
            return result['content'][0]['text']
        
        return fallback_response(message)

    except Exception as e:
        print(f"Claude API error: {e}")
        return fallback_response(message)

def fallback_response(query):
    """Fallback clinical response when API is unavailable"""
    query_lower = query.lower()

    if 'malaria' in query_lower:
        return "For malaria treatment: Confirm diagnosis with RDT or blood smear. Use ACT (artemisinin-based combination therapy) as first-line for uncomplicated malaria. For severe malaria, use IV artesunate. Monitor for treatment response at day 3 and day 28. Follow-up for adverse effects and treatment failure indicators."

    if 'fever' in query_lower or 'temperature' in query_lower:
        return "Assess fever type and duration. Check for danger signs: lethargy, severe respiratory distress, convulsions, poor feeding. Take blood tests if available (malaria RDT, blood culture). Treat symptomatically with paracetamol. Consider antibiotics for bacterial infection. Refer if danger signs present."

    if 'pregnancy' in query_lower or 'pregnant' in query_lower:
        return "For pregnant patients: Monitor closely for complications. Use safe antimalarials (artemisinin after first trimester). Screen for hypertension and proteinuria. Ensure iron and folic acid supplementation. Provide tetanus prophylaxis. Refer for facility delivery. Track fetal movement and growth."

    if 'danger' in query_lower or 'emergency' in query_lower or 'urgent' in query_lower:
        return "DANGER SIGNS requiring immediate referral: Inability to drink/breastfeed, persistent vomiting, convulsions, lethargy/unconsciousness, severe respiratory distress, signs of shock, severe bleeding. Transfer to facility immediately. Provide first aid and monitor vitals during transport."

    return f"I can help with clinical guidance for {query}. Key steps: 1) Assess patient thoroughly 2) Check for danger signs 3) Refer if needed 4) Document findings. For specific guidance on malaria, fever, pregnancy, or emergencies, ask more specifically."

@app.route('/api/transcribe', methods=['POST'])
def transcribe():
    """Handle speech-to-text (requires audio file)"""
    try:
        if 'audio' not in request.files:
            return jsonify({'error': 'No audio file provided'}), 400

        audio_file = request.files['audio']
        
        # In production, integrate with speech-to-text service
        # For now, return placeholder
        return jsonify({
            'success': True,
            'message': 'Audio processing would go here',
            'text': 'Transcribed text would appear here'
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/synthesis', methods=['POST'])
def synthesis():
    """Handle text-to-speech"""
    try:
        data = request.json
        text = data.get('text', '')

        if not text:
            return jsonify({'error': 'Text is required'}), 400

        # In production, integrate with text-to-speech service
        return jsonify({
            'success': True,
            'message': f'Text "{text}" can be converted to speech',
            'audio_url': '/api/audio/placeholder.mp3'
        })

    except Exception as e:
        return jsonify({'error': str(e)}), 500

@app.route('/api/inworld/chat', methods=['POST'])
def inworld_chat():
    """Proxy Inworld AI requests for security"""
    try:
        data = request.json
        message = data.get('message', '')
        api_key = data.get('apiKey', '')
        workspace_id = data.get('workspaceId', '')
        character_id = data.get('characterId', '')

        if not all([message, api_key, workspace_id, character_id]):
            return jsonify({'error': 'Missing required fields'}), 400

        # Initialize session
        session_url = f"{INWORLD_API_BASE}/sessions:create"
        session_headers = {
            'Authorization': f'Bearer {api_key}',
            'Content-Type': 'application/json',
        }
        session_body = {
            'user': {'name': 'CHW'},
            'character': {
                'resourceName': f'workspaces/{workspace_id}/characters/{character_id}'
            }
        }

        session_response = requests.post(
            session_url,
            headers=session_headers,
            json=session_body,
            timeout=30
        )

        if session_response.status_code != 200:
            return jsonify({
                'error': f'Failed to initialize session: {session_response.status_code}',
                'details': session_response.text
            }), 500

        session_data = session_response.json()
        session_id = session_data.get('session', {}).get('sessionId', '')

        if not session_id:
            return jsonify({'error': 'No session ID received'}), 500

        # Send message
        message_url = f"{INWORLD_API_BASE}/sessions/{session_id}:sendText"
        message_response = requests.post(
            message_url,
            headers=session_headers,
            json={'text': message},
            timeout=30
        )

        if message_response.status_code != 200:
            return jsonify({
                'error': f'Failed to send message: {message_response.status_code}',
                'details': message_response.text
            }), 500

        response_data = message_response.json()
        
        # Extract response text
        response_text = 'No response from Inworld AI'
        
        if response_data.get('text', {}).get('text'):
            response_text = response_data['text']['text']
        elif response_data.get('packets'):
            for packet in response_data['packets']:
                if packet.get('text', {}).get('text'):
                    response_text = packet['text']['text']
                    break

        return jsonify({
            'message': response_text,
            'sessionId': session_id,
            'success': True
        })

    except requests.exceptions.Timeout:
        return jsonify({'error': 'Request timeout'}), 504
    except requests.exceptions.RequestException as e:
        return jsonify({'error': f'Request failed: {str(e)}'}), 500
    except Exception as e:
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5000)
