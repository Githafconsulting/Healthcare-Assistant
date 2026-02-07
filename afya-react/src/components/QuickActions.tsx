import { Plus, Stethoscope, MessageSquare, FileText, AlertCircle } from 'lucide-react';

interface Action {
  icon: React.ReactNode;
  label: string;
  color: string;
  onClick: () => void;
}

export function QuickActions() {
  const actions: Action[] = [
    {
      icon: <Plus size={24} />,
      label: 'New Patient',
      color: 'from-primary-500 to-primary-600',
      onClick: () => console.log('New patient'),
    },
    {
      icon: <Stethoscope size={24} />,
      label: 'Quick Check',
      color: 'from-success to-emerald-600',
      onClick: () => console.log('Quick check'),
    },
    {
      icon: <MessageSquare size={24} />,
      label: 'Messaging',
      color: 'from-accent-500 to-accent-600',
      onClick: () => console.log('Messaging'),
    },
    {
      icon: <FileText size={24} />,
      label: 'Reports',
      color: 'from-warning to-orange-600',
      onClick: () => console.log('Reports'),
    },
    {
      icon: <AlertCircle size={24} />,
      label: 'Emergencies',
      color: 'from-danger to-red-600',
      onClick: () => console.log('Emergencies'),
    },
  ];

  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-5 gap-4">
      {actions.map((action, idx) => (
        <button
          key={idx}
          onClick={action.onClick}
          className="group flex flex-col items-center gap-3 p-4 rounded-2xl bg-white border border-gray-100 hover:border-primary-200 hover:bg-gray-50 transition-all duration-200"
        >
          <div className={`bg-gradient-to-br ${action.color} p-4 rounded-xl text-white group-hover:shadow-lg transition-all duration-200 transform group-hover:scale-110`}>
            {action.icon}
          </div>
          <span className="text-xs font-semibold text-gray-700 text-center leading-tight">
            {action.label}
          </span>
        </button>
      ))}
    </div>
  );
}
