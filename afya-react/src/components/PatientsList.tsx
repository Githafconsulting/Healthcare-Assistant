import { ChevronRight, AlertCircle, CheckCircle, Clock } from 'lucide-react';
import { Card } from './Card';

interface Patient {
  id: string;
  name: string;
  location: string;
  status: 'critical' | 'warning' | 'stable' | 'pending';
  lastVisit: string;
  nextFollowUp: string;
}

export function PatientsList() {
  const patients: Patient[] = [
    {
      id: '1',
      name: 'Sarah Kipchoge',
      location: 'Nairobi, Kenya',
      status: 'critical',
      lastVisit: '2 days ago',
      nextFollowUp: 'Today',
    },
    {
      id: '2',
      name: 'James Omondi',
      location: 'Kisumu, Kenya',
      status: 'warning',
      lastVisit: '5 days ago',
      nextFollowUp: 'Tomorrow',
    },
    {
      id: '3',
      name: 'Mary Kamau',
      location: 'Mombasa, Kenya',
      status: 'stable',
      lastVisit: '1 day ago',
      nextFollowUp: '3 days',
    },
    {
      id: '4',
      name: 'David Mwangi',
      location: 'Nairobi, Kenya',
      status: 'pending',
      lastVisit: 'Not yet visited',
      nextFollowUp: 'Pending initial visit',
    },
  ];

  const statusConfig = {
    critical: { icon: AlertCircle, color: 'text-danger', bg: 'bg-red-50', label: 'Critical' },
    warning: { icon: AlertCircle, color: 'text-warning', bg: 'bg-orange-50', label: 'Warning' },
    stable: { icon: CheckCircle, color: 'text-success', bg: 'bg-green-50', label: 'Stable' },
    pending: { icon: Clock, color: 'text-primary-600', bg: 'bg-blue-50', label: 'Pending' },
  };

  return (
    <div className="space-y-3">
      {patients.map((patient) => {
        const config = statusConfig[patient.status];
        const StatusIcon = config.icon;
        return (
          <Card
            key={patient.id}
            className="cursor-pointer group hover:relative hover:shadow-elevated"
            onClick={() => console.log('View patient', patient.id)}
          >
            <div className="flex items-start justify-between">
              <div className="flex-1">
                <div className="flex items-center gap-3 mb-2">
                  <div className={`w-10 h-10 rounded-full ${config.bg} flex items-center justify-center`}>
                    <StatusIcon size={18} className={config.color} />
                  </div>
                  <div>
                    <h3 className="font-semibold text-gray-900">{patient.name}</h3>
                    <p className="text-xs text-gray-500">{patient.location}</p>
                  </div>
                </div>
                <div className="grid grid-cols-2 gap-3 mt-3 text-xs">
                  <div>
                    <p className="text-gray-500">Last Visit</p>
                    <p className="font-medium text-gray-900">{patient.lastVisit}</p>
                  </div>
                  <div>
                    <p className="text-gray-500">Next Follow-up</p>
                    <p className="font-medium text-gray-900">{patient.nextFollowUp}</p>
                  </div>
                </div>
              </div>
              <div className="flex flex-col items-end gap-2">
                <span className={`px-3 py-1 rounded-full text-xs font-semibold ${config.bg} ${config.color}`}>
                  {config.label}
                </span>
                <ChevronRight size={20} className="text-gray-400 group-hover:text-primary-600 transition" />
              </div>
            </div>
          </Card>
        );
      })}
    </div>
  );
}
