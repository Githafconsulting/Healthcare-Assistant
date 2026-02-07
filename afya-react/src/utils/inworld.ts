/**
 * Inworld AI Integration for Voice Assistant
 * Provides conversational AI with intelligent context awareness
 */

export interface InworldMessage {
  text: string;
  characterName: string;
  interactionId: string;
}

export interface InworldSettings {
  apiKey: string;
  workspaceId: string;
  characterId: string;
}

export interface InworldSessionRequest {
  user: {
    name: string;
  };
  character: {
    resourceName: string;
  };
}

export interface InworldResponse {
  routing: {
    source: {
      name: string;
    };
  };
  text?: {
    text: string;
  };
  packets?: Array<{
    text?: {
      text: string;
    };
  }>;
}

class InworldAIClient {
  private apiKey: string = '';
  private workspaceId: string = '';
  private characterId: string = '';
  private sessionId: string = '';
  private baseUrl: string = 'https://api.inworld.ai/v1';

  constructor(settings?: InworldSettings) {
    if (settings) {
      this.setCredentials(settings);
    } else {
      this.loadCredentialsFromStorage();
    }
  }

  setCredentials(settings: InworldSettings) {
    this.apiKey = settings.apiKey;
    this.workspaceId = settings.workspaceId;
    this.characterId = settings.characterId;
    this.saveCredentialsToStorage(settings);
  }

  getCredentials(): InworldSettings | null {
    if (!this.apiKey || !this.workspaceId || !this.characterId) {
      return null;
    }
    return {
      apiKey: this.apiKey,
      workspaceId: this.workspaceId,
      characterId: this.characterId,
    };
  }

  isConfigured(): boolean {
    return !!(this.apiKey && this.workspaceId && this.characterId);
  }

  private saveCredentialsToStorage(settings: InworldSettings) {
    try {
      localStorage.setItem('afya_inworld_settings', JSON.stringify(settings));
    } catch (e) {
      console.error('Failed to save Inworld settings', e);
    }
  }

  private loadCredentialsFromStorage() {
    try {
      const stored = localStorage.getItem('afya_inworld_settings');
      if (stored) {
        const settings = JSON.parse(stored) as InworldSettings;
        this.apiKey = settings.apiKey || '';
        this.workspaceId = settings.workspaceId || '';
        this.characterId = settings.characterId || '';
      }
    } catch (e) {
      console.error('Failed to load Inworld settings', e);
    }
  }

  clearCredentials() {
    this.apiKey = '';
    this.workspaceId = '';
    this.characterId = '';
    try {
      localStorage.removeItem('afya_inworld_settings');
    } catch (e) {
      console.error('Failed to clear Inworld settings', e);
    }
  }

  /**
   * Initialize a session with Inworld AI
   */
  async initializeSession(userName: string = 'CHW'): Promise<void> {
    if (!this.isConfigured()) {
      throw new Error('Inworld API credentials not configured');
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/sessions:create`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${this.apiKey}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            user: { name: userName },
            character: {
              resourceName: `workspaces/${this.workspaceId}/characters/${this.characterId}`,
            },
          } as InworldSessionRequest),
        }
      );

      if (!response.ok) {
        throw new Error(`Failed to initialize Inworld session: ${response.statusText}`);
      }

      const data = await response.json();
      this.sessionId = data.session?.sessionId || '';
    } catch (error) {
      console.error('Inworld session initialization error:', error);
      throw error;
    }
  }

  /**
   * Send a message to Inworld AI and get a response
   */
  async sendMessage(text: string): Promise<string> {
    if (!this.isConfigured()) {
      throw new Error('Inworld API credentials not configured');
    }

    if (!this.sessionId) {
      await this.initializeSession();
    }

    try {
      const response = await fetch(
        `${this.baseUrl}/sessions/${this.sessionId}:sendText`,
        {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${this.apiKey}`,
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            text: text,
          }),
        }
      );

      if (!response.ok) {
        throw new Error(`Failed to send message to Inworld: ${response.statusText}`);
      }

      const data = await response.json();

      // Extract text from response
      if (data.routing?.source?.name === 'INTERACTION_END') {
        return 'Conversation ended';
      }

      if (data.text?.text) {
        return data.text.text;
      }

      // Handle packet-based responses
      if (data.packets && data.packets.length > 0) {
        const textPacket = data.packets.find((p: any) => p.text?.text);
        if (textPacket) {
          return textPacket.text.text;
        }
      }

      return 'No response from AI';
    } catch (error) {
      console.error('Inworld message error:', error);
      throw error;
    }
  }

  /**
   * Proxy through backend for security (recommended for production)
   */
  async sendMessageViaBackend(
    text: string,
    backendUrl: string = 'http://localhost:5000/api'
  ): Promise<string> {
    try {
      const response = await fetch(`${backendUrl}/inworld/chat`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          message: text,
          apiKey: this.apiKey,
          workspaceId: this.workspaceId,
          characterId: this.characterId,
        }),
      });

      if (!response.ok) {
        throw new Error(`Backend error: ${response.statusText}`);
      }

      const data = await response.json();
      return data.message || 'No response from AI';
    } catch (error) {
      console.error('Backend Inworld error:', error);
      throw error;
    }
  }

  getSessionId(): string {
    return this.sessionId;
  }
}

// Singleton instance
let inworld: InworldAIClient | null = null;

export function getInworldClient(): InworldAIClient {
  if (!inworld) {
    inworld = new InworldAIClient();
  }
  return inworld;
}

export function createInworldClient(settings: InworldSettings): InworldAIClient {
  inworld = new InworldAIClient(settings);
  return inworld;
}

export function resetInworldClient(): void {
  inworld = null;
}

export default InworldAIClient;
