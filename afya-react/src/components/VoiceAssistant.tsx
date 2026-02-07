import { Mic, Send, MessageCircle, Volume2, Copy, Settings } from 'lucide-react';
import { useState, useRef, useEffect } from 'react';
import { getInworldClient, createInworldClient } from '../utils/inworld';
import { InworldSettingsModal } from './InworldSettingsModal';

interface Message {
  id: string;
  role: 'user' | 'assistant';
  text: string;
  timestamp: Date;
  isAudio?: boolean;
}

export function VoiceAssistant() {
  const [messages, setMessages] = useState<Message[]>([]);
  const [input, setInput] = useState('');
  const [isListening, setIsListening] = useState(false);
  const [showSettings, setShowSettings] = useState(false);
  const [useInworld, setUseInworld] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const [isSpeaking, setIsSpeaking] = useState(false);
  const recognitionRef = useRef<any>(null);
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const synthRef = useRef<Spee and check Inworld config
  useEffect(() => {
    const SpeechRecognition = window.webkitSpeechRecognition || (window as any).SpeechRecognition;
    if (SpeechRecognition) {
      recognitionRef.current = new SpeechRecognition();
      recognitionRef.current.continuous = false;
      recognitionRef.current.interimResults = true;
      recognitionRef.current.lang = 'en-US';

      recognitionRef.current.onstart = () => setIsListening(true);
      recognitionRef.current.onend = () => setIsListening(false);

      recognitionRef.current.onresult = (event: any) => {
        let interim = '';
        for (let i = event.resultIndex; i < event.results.length; i++) {
          const transcript = event.results[i][0].transcript;
          if (event.results[i].isFinal) {
            setInput((prev) => prev + transcript);
          } else {
            interim += transcript;
          }
        }
        if (interim) setInput((prev) => prev.split(' ').slice(0, -1).join(' ') + ' ' + interim);
      };
    }

    synthRef.current = window.speechSynthesis;

    // Check if Inworld is configured
    const client = getInworldClient();
    if (client.isConfigured()) {
      setUseInworld(true);
    }

    synthRef.current = window.speechSynthesis;
  }, []);

  // Auto-scroll to bottom
  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const toggleListening = () => {
    if (!recognitionRef.current) {
      alert('Speech Recognition not supported in your browser. Use Chrome, Edge, or Safari.');
      return;
    }

    if (isListening) {
      recognitionRef.current.stop();
    } else {
      recognitionRef.current.start();
    }
  };

  const sendMessage = async () => {
    if (!input.trim()) return;

    const userInput = input;
    setInput('');
    setIsLoading(true);

    try {
      const client = getInworldClient();
      let responseText = '';

      if (useInworld && client.isConfigured()) {
        // Use Inworld AI
        try {
          responseText = await client.sendMessage(userInput);
        } catch (error) {
          console.error('Inworld error, falling back:', error);
          responseText = clinicalResponseGenerator(userInput);
        }
      } else {
        // Use fallback clinical response
        responseText = clinicalResponseGenerator(userInput);
      }

      const assistantMessage: Message = {
        id: (Date.now() + 1).toString(),
        role: 'assistant',
        text: responseText,
        timestamp: new Date(),
      };

      setMessages((prev) => [...prev, assistantMessage]);
      
      // Auto-speak response
      speakText(assistantMessage.text);
    } catch (error) {
      console.error('Error:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSettingsSave = (settings: any) => {
    createInworldClient(settings);
    setUseInworld(true);   id: (Date.now() + 2).toString(),
        role: 'assistant',
        text: 'Sorry, I couldn\'t connect to the AI service. Make sure the backend is running on http://localhost:5000',
        timestamp: new Date(),
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  const speakText = (text: string) => {
    if (!synthRef.current) return;

    // Cancel any ongoing speech
    synthRef.current.cancel();
    setIsSpeaking(true);

    const utterance = new SpeechSynthesisUtterance(text);
    utterance.rate = 1;
    utterance.pitch = 1;
    utterance.onend = () => setIsSpeaking(false);

    synthRef.current.speak(utterance);
  };

  const handleKeyPress = (e: React.KeyboardEvent) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      sendMessage();justify-between">
          <div className="flex items-center gap-3">
            <div className="p-3 bg-gradient-to-br from-primary-500 to-accent-600 rounded-xl text-white">
              <MessageCircle size={24} />
            </div>
            <div>
              <h1 className="text-2xl font-bold text-gray-900">Afya Voice Assistant</h1>
              <p className="text-sm text-gray-600">
                {useInworld ? '🧠 Powered by Inworld AI' : 'Clinical support & guidance'}
              </p>
            </div>
          </div>
          <button
            onClick={() => setShowSettings(true)}
            className={`p-3 rounded-xl transition flex items-center gap-2 font-medium ${
              useInworld
                ? 'bg-green-100 text-success hover:bg-green-200'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
            title="Configure Inworld AI"
          >
            <Settings size={20} />
            {useInworld ? 'Configured' : 'Configure AI'}
          </buttonsName="bg-white border-b border-gray-200 px-6 py-4">
        <div className="flex items-center gap-3">
          <div className="p-3 bg-gradient-to-br from-primary-500 to-accent-600 rounded-xl text-white">
            <MessageCircle size={24} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-gray-900">Afya Voice Assistant</h1>
            <p className="text-sm text-gray-600">AI-powered clinical support & guidance</p>
          </div>
        </div>
      </div>

      {/* Messages Area */}
      <div className="flex-1 overflow-y-auto p-6 space-y-4">
        {messages.length === 0 && (
          <div className="h-full flex flex-col items-center justify-center text-center">
            <div className="p-6 bg-white rounded-2xl shadow-card mb-4">
              <Mic size={48} className="text-primary-500 mx-auto mb-4" />
              <h2 className="text-2xl font-bold text-gray-900 mb-2">Start a Conversation</h2>
              <p className="text-gray-600 max-w-md">
                Click the microphone or type to ask clinical questions, get treatment guidance, or discuss patient cases.
              </p>
            </div>
          </div>
        )}

        {messages.map((msg) => (
          <div
            key={msg.id}
            className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'} gap-3`}
          >
            {msg.role === 'assistant' && (
              <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center flex-shrink-0">
                <MessageCircle size={16} className="text-primary-600" />
              </div>
            )}

            <div
              className={`max-w-xs lg:max-w-md px-4 py-3 rounded-2xl ${
                msg.role === 'user'
                  ? 'bg-primary-600 text-white rounded-br-none'
                  : 'bg-white text-gray-900 shadow-card rounded-bl-none'
              }`}
            >
              <p className="text-sm leading-relaxed">{msg.text}</p>
              <p className={`text-xs mt-1 ${msg.role === 'user' ? 'text-primary-100' : 'text-gray-500'}`}>
                {msg.timestamp.toLocaleTimeString()}
              </p>
            </div>

            {msg.role === 'assistant' && (
              <div className="flex gap-2 items-start pt-1">
                <button
                  onClick={() => speakText(msg.text)}
                  disabled={isSpeaking}
                  className="p-2 hover:bg-white rounded-lg text-gray-600 hover:text-primary-600 transition"
                  title="Speak message"
                >
                  <Volume2 size={16} />
                </button>
                <button
                  onClick={() => navigator.clipboard.writeText(msg.text)}
                  className="p-2 hover:bg-white rounded-lg text-gray-600 hover:text-primary-600 transition"
                  title="Copy message"
                >
                  <Copy size={16} />
                </button>
              </div>
            )}
          </div>
        ))}

        {isLoading && (
          <div className="flex gap-3">
            <div className="w-8 h-8 rounded-full bg-primary-100 flex items-center justify-center flex-shrink-0">
              <MessageCircle size={16} className="text-primary-600" />
            </div>
            <div className="bg-white rounded-2xl rounded-bl-none p-4 shadow-card">
              <div className="flex gap-2">
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce"></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.2s' }}></div>
                <div className="w-2 h-2 bg-gray-400 rounded-full animate-bounce" style={{ animationDelay: '0.4s' }}></div>
              </div>
            </div>
          </div>
        )}

        <div ref={messagesEndRef} />
      </div>

      {/* Input Area */}
      <div className="bg-white border-t border-gray-200 p-6">
        <div className="flex gap-3">
          <button
            onClick={toggleListening}
            disabled={isLoading}
            className={`p-3 rounded-xl flex-shrink-0 transition ${
              isListening
                ? 'bg-danger text-white shadow-lg animate-pulse'
                : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
            title={isListening ? 'Stop listening' : 'Start listening'}
          >
            <Mic size={20} />
          </button>

          <textarea
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyPress={handleKeyPress}
            placeholder="Type or speak your question..."
            disabled={isLoading}
            rows={3}
            className="flex-1 p-3 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500 resize-none"
          />


      {/* Inworld Settings Modal */}
      <InworldSettingsModal
        isOpen={showSettings}
        onClose={() => setShowSettings(false)}
        onSave={handleSettingsSave}
      />
          <button
            onClick={sendMessage}
            disabled={!input.trim() || isLoading}
            className="p-3 rounded-xl bg-primary-600 text-white hover:bg-primary-700 disabled:opacity-50 disabled:cursor-not-allowed transition flex-shrink-0"
            title="Send message"
          >
            <Send size={20} />
          </button>
        </div>

        {isListening && (
          <p className="text-sm text-danger mt-2 flex items-center gap-2">
            <span className="w-2 h-2 bg-danger rounded-full animate-pulse"></span>
            Listening...
          </p>
        )}

        {isSpeaking && (
          <p className="text-sm text-primary-600 mt-2 flex items-center gap-2">
            <span className="w-2 h-2 bg-primary-600 rounded-full animate-pulse"></span>
            Speaking...
          </p>
        )}
      </div>
    </div>
  );
}

// Fallback clinical response generator for demo
function clinicalResponseGenerator(query: string): string {
  const lowerQuery = query.toLowerCase();

  if (lowerQuery.includes('malaria')) {
    return 'For malaria treatment, first confirm diagnosis with RDT or blood smear. Use ACT (Artemisinin-based combination therapy) as first-line in uncomplicated malaria. For severe malaria, use IV artesunate. Ensure patient follow-up after 3 days and 28 days. Monitor for resistance patterns in your region.';
  }
  if (lowerQuery.includes('fever') || lowerQuery.includes('temperature')) {
    return 'High fever requires assessment for danger signs. Check for severe malaria indicators: cerebral malaria signs, severe anemia, renal failure. Rule out meningitis, typhoid, and other infections. Manage symptoms with paracetamol and investigate underlying cause. Cool compress and increased fluids can help.';
  }
  if (lowerQuery.includes('pregnancy') || lowerQuery.includes('pregnant')) {
    return 'For pregnant patients with malaria, use quinine or artemisinin in second/third trimester. First trimester requires careful consideration. Prevent preterm labor and check fetal viability. Ensure iron supplementation, tetanus prophylaxis, and monitor for complications like eclampsia. Refer high-risk cases to facility.';
  }
  if (lowerQuery.includes('danger sign') || lowerQuery.includes('emergency')) {
    return 'Immediate referral indicators: inability to drink/breastfeed, persistent vomiting, convulsions, lethargy/coma, severe respiratory distress, signs of shock. Act quickly - these are life-threatening. Provide first aid while arranging transport. Document carefully for handover.';
  }

  return `I can help with clinical guidance. Based on your question about "${query}", I would recommend: 1. Assess the patient thoroughly, 2. Check for danger signs, 3. Refer if needed, 4. Document all findings. For specific guidance, ask about malaria, pregnancy complications, fever, or emergency signs.`;
}
