import { X, Key, Save } from 'lucide-react';
import { useState, useEffect } from 'react';
import { getInworldClient, InworldSettings } from '../utils/inworld';

interface InworldSettingsModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (settings: InworldSettings) => void;
}

export function InworldSettingsModal({ isOpen, onClose, onSave }: InworldSettingsModalProps) {
  const [apiKey, setApiKey] = useState('');
  const [workspaceId, setWorkspaceId] = useState('');
  const [characterId, setCharacterId] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    if (isOpen) {
      const client = getInworldClient();
      const settings = client.getCredentials();
      if (settings) {
        setApiKey(settings.apiKey);
        setWorkspaceId(settings.workspaceId);
        setCharacterId(settings.characterId);
      }
      setError('');
      setSuccess(false);
    }
  }, [isOpen]);

  const handleSave = () => {
    if (!apiKey.trim() || !workspaceId.trim() || !characterId.trim()) {
      setError('All fields are required');
      return;
    }

    const settings: InworldSettings = {
      apiKey,
      workspaceId,
      characterId,
    };

    try {
      const client = getInworldClient();
      client.setCredentials(settings);
      setSuccess(true);
      onSave(settings);
      setTimeout(() => {
        onClose();
      }, 1500);
    } catch (err) {
      setError('Failed to save settings');
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50">
      <div className="bg-white rounded-2xl p-6 w-full max-w-md shadow-elevated">
        {/* Header */}
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary-100 rounded-lg">
              <Key size={20} className="text-primary-600" />
            </div>
            <h2 className="text-2xl font-bold text-gray-900">Inworld AI Settings</h2>
          </div>
          <button
            onClick={onClose}
            className="p-2 hover:bg-gray-100 rounded-lg transition"
          >
            <X size={20} />
          </button>
        </div>

        {/* Form */}
        <div className="space-y-4 mb-6">
          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              API Key
            </label>
            <input
              type="password"
              value={apiKey}
              onChange={(e) => setApiKey(e.target.value)}
              placeholder="sk_inworld_..."
              className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
            <p className="text-xs text-gray-500 mt-1">
              Get from{' '}
              <a
                href="https://studio.inworld.ai"
                target="_blank"
                rel="noopener noreferrer"
                className="text-primary-600 hover:underline"
              >
                studio.inworld.ai
              </a>
            </p>
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Workspace ID
            </label>
            <input
              type="text"
              value={workspaceId}
              onChange={(e) => setWorkspaceId(e.target.value)}
              placeholder="workspace-123..."
              className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>

          <div>
            <label className="block text-sm font-semibold text-gray-700 mb-2">
              Character ID
            </label>
            <input
              type="text"
              value={characterId}
              onChange={(e) => setCharacterId(e.target.value)}
              placeholder="character-456..."
              className="w-full px-4 py-2 border border-gray-200 rounded-xl focus:outline-none focus:ring-2 focus:ring-primary-500"
            />
          </div>
        </div>

        {/* Error */}
        {error && (
          <div className="p-3 bg-red-50 border border-red-200 rounded-lg mb-4">
            <p className="text-sm text-danger">{error}</p>
          </div>
        )}

        {/* Success */}
        {success && (
          <div className="p-3 bg-green-50 border border-green-200 rounded-lg mb-4">
            <p className="text-sm text-success">Settings saved successfully!</p>
          </div>
        )}

        {/* Info */}
        <div className="p-4 bg-blue-50 rounded-lg mb-6">
          <p className="text-xs text-primary-700">
            <strong>ℹ️ Note:</strong> Your API key is stored securely in your browser's local storage.
            For production, use a backend proxy.
          </p>
        </div>

        {/* Actions */}
        <div className="flex gap-3">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-2 border border-gray-200 text-gray-700 rounded-xl hover:bg-gray-50 font-semibold transition"
          >
            Cancel
          </button>
          <button
            onClick={handleSave}
            className="flex-1 px-4 py-2 bg-primary-600 text-white rounded-xl hover:bg-primary-700 font-semibold transition flex items-center justify-center gap-2"
          >
            <Save size={18} />
            Save Settings
          </button>
        </div>
      </div>
    </div>
  );
}
