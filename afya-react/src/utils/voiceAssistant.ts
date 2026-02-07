// Voice Assistant API integration utilities
// Supports multiple free and paid AI services

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}

export interface ChatResponse {
  message: string;
  context?: string;
  suggestions?: string[];
}

// Option 1: Using Hugging Face Inference API (Free tier available)
export async function callHuggingFaceAPI(message: string, apiKey?: string): Promise<string> {
  try {
    const response = await fetch(
      'https://api-inference.huggingface.co/models/microsoft/DialoGPT-medium',
      {
        headers: {
          Authorization: `Bearer ${apiKey || process.env.REACT_APP_HF_TOKEN}`,
        },
        method: 'POST',
        body: JSON.stringify({ inputs: message }),
      }
    );
    
    const result = await response.json();
    return result[0]?.generated_text || 'Unable to generate response';
  } catch (error) {
    console.error('HuggingFace API error:', error);
    throw error;
  }
}

// Option 2: Using OpenAI API (Paid but has free trial credits)
export async function callOpenAIAPI(
  message: string,
  apiKey?: string,
  context: string = 'clinical-decision-support'
): Promise<string> {
  try {
    const systemPrompt = getSystemPrompt(context);

    const response = await fetch('https://api.openai.com/v1/chat/completions', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${apiKey || process.env.REACT_APP_OPENAI_API_KEY}`,
      },
      body: JSON.stringify({
        model: 'gpt-3.5-turbo',
        messages: [
          {
            role: 'system',
            content: systemPrompt,
          },
          {
            role: 'user',
            content: message,
          },
        ],
        temperature: 0.7,
        max_tokens: 500,
      }),
    });

    const data = await response.json();
    if (data.error) throw new Error(data.error.message);
    
    return data.choices[0]?.message?.content || 'Unable to generate response';
  } catch (error) {
    console.error('OpenAI API error:', error);
    throw error;
  }
}

// Option 3: Using Anthropic Claude (Paid but powerful)
export async function callClaudeAPI(
  message: string,
  apiKey?: string,
  context: string = 'clinical-decision-support'
): Promise<string> {
  try {
    const systemPrompt = getSystemPrompt(context);

    const response = await fetch('https://api.anthropic.com/v1/messages', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'x-api-key': apiKey || process.env.REACT_APP_CLAUDE_API_KEY || '',
        'anthropic-version': '2023-06-01',
      },
      body: JSON.stringify({
        model: 'claude-3-haiku-20240307',
        max_tokens: 500,
        system: systemPrompt,
        messages: [
          {
            role: 'user',
            content: message,
          },
        ],
      }),
    });

    const data = await response.json();
    if (data.error) throw new Error(data.error.message);
    
    return data.content[0]?.text || 'Unable to generate response';
  } catch (error) {
    console.error('Claude API error:', error);
    throw error;
  }
}

// Option 4: Local backend endpoint
export async function callLocalBackendAPI(
  message: string,
  context: string = 'clinical-decision-support'
): Promise<string> {
  try {
    const response = await fetch('http://localhost:8000/api/chat', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        message,
        context,
      }),
    });

    if (!response.ok) throw new Error(`Backend error: ${response.status}`);
    
    const data = await response.json();
    return data.message || 'Unable to generate response';
  } catch (error) {
    console.error('Local backend API error:', error);
    throw error;
  }
}

function getSystemPrompt(context: string): string {
  const prompts: Record<string, string> = {
    'clinical-decision-support': `You are Afya, a clinical decision support AI assistant for Community Health Workers (CHWs) in resource-limited settings. You provide evidence-based clinical guidance following WHO and local guidelines. 

Your role:
- Provide concise, actionable clinical guidance
- Suggest when to refer patients to health facilities
- Help with treatment decisions based on available resources
- Emphasize danger signs and emergency protocols
- Consider maternal and child health priorities
- Support malaria, fever, respiratory, and nutritional disorders management
- Always prioritize patient safety

Guidelines:
- Use simple, clear language suitable for frontline health workers
- Include specific treatment dosages when relevant
- Identify danger signs that require immediate referral
- Suggest follow-up schedules
- Consider resource constraints in low-income settings'`,

    'patient-interaction': `You are Afya, a friendly health communication assistant. You help patients understand their health conditions, medications, and follow-up care in simple language. Be empathetic, clear, and encouraging. Always recommend speaking with their healthcare provider for personalized advice.`,

    'clinical-documentation': `You are Afya, a medical scribe assistant. Help format and structure clinical notes, extract key information from conversations, and suggest follow-up actions. Maintain clinical accuracy and completeness.`,
  };

  return prompts[context] || prompts['clinical-decision-support'];
}

// Speech-to-text using Web Speech API (Free, built-in)
export function startVoiceRecognition(
  onResult: (transcript: string) => void,
  onError?: (error: string) => void
): { stop: () => void; isSupported: boolean } {
  const SpeechRecognition = window.webkitSpeechRecognition || (window as any).SpeechRecognition;

  if (!SpeechRecognition) {
    onError?.('Speech Recognition not supported');
    return { stop: () => {}, isSupported: false };
  }

  const recognition = new SpeechRecognition();
  recognition.continuous = false;
  recognition.lang = 'en-US';

  recognition.onresult = (event: any) => {
    let transcript = '';
    for (let i = event.resultIndex; i < event.results.length; i++) {
      transcript += event.results[i][0].transcript;
    }
    onResult(transcript);
  };

  recognition.onerror = (event: any) => {
    onError?.(event.error);
  };

  recognition.start();

  return {
    stop: () => recognition.stop(),
    isSupported: true,
  };
}

// Text-to-speech using Web Speech API (Free, built-in)
export function speakText(text: string, options?: { rate?: number; pitch?: number }): void {
  const synth = window.speechSynthesis;

  if (!synth) {
    console.error('Speech Synthesis not supported');
    return;
  }

  synth.cancel();

  const utterance = new SpeechSynthesisUtterance(text);
  utterance.rate = options?.rate || 1;
  utterance.pitch = options?.pitch || 1;

  synth.speak(utterance);
}

// Stop speaking
export function stopSpeaking(): void {
  window.speechSynthesis?.cancel();
}
