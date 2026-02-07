import { BookOpen, Brain, Zap } from 'lucide-react';
import { Card } from './Card';

interface Guidance {
  id: string;
  title: string;
  description: string;
  icon: React.ReactNode;
  color: string;
  items: string[];
}

export function ClinicalGuidance() {
  const guidances: Guidance[] = [
    {
      id: '1',
      title: 'Malaria Management',
      description: 'Latest protocols and treatment guidelines',
      icon: <BookOpen size={24} />,
      color: 'from-primary-500 to-primary-600',
      items: ['ACT therapy', 'Severe malaria protocols', 'Pregnancy considerations'],
    },
    {
      id: '2',
      title: 'Decision Support',
      description: 'AI-powered clinical recommendations',
      icon: <Brain size={24} />,
      color: 'from-accent-500 to-accent-600',
      items: ['Symptom analysis', 'Risk assessment', 'Treatment suggestions'],
    },
    {
      id: '3',
      title: 'Emergency Signs',
      description: 'Danger signs and emergency protocols',
      icon: <Zap size={24} />,
      color: 'from-warning to-orange-600',
      items: ['Critical indicators', 'Referral protocols', 'First aid steps'],
    },
  ];

  return (
    <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
      {guidances.map((guidance) => (
        <Card
          key={guidance.id}
          className="cursor-pointer group hover:shadow-elevated"
          onClick={() => console.log('Open guidance', guidance.id)}
        >
          <div className="flex items-start justify-between mb-4">
            <div className={`bg-gradient-to-br ${guidance.color} p-4 rounded-xl text-white group-hover:shadow-lg transition-all transform group-hover:scale-110`}>
              {guidance.icon}
            </div>
            <div className="px-3 py-1 bg-primary-50 text-primary-600 text-xs font-semibold rounded-full">
              New
            </div>
          </div>
          
          <h3 className="text-lg font-bold text-gray-900 mb-1">{guidance.title}</h3>
          <p className="text-sm text-gray-600 mb-4">{guidance.description}</p>
          
          <div className="space-y-2 border-t pt-4">
            {guidance.items.map((item, idx) => (
              <div key={idx} className="flex items-center gap-2">
                <div className="w-2 h-2 bg-primary-500 rounded-full"></div>
                <span className="text-sm text-gray-700">{item}</span>
              </div>
            ))}
          </div>
        </Card>
      ))}
    </div>
  );
}
