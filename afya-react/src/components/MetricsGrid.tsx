import { Users, TrendingUp, Activity, Clock } from 'lucide-react';
import { GradientCard } from './Card';

export function MetricsGrid() {
  return (
    <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
      <GradientCard
        title="Active Patients"
        value="124"
        icon={<Users size={24} />}
        gradient="bg-gradient-to-br from-primary-500 to-primary-700"
        subtext="↑ 12 this week"
      />
      <GradientCard
        title="Positive Outcomes"
        value="92%"
        icon={<TrendingUp size={24} />}
        gradient="bg-gradient-to-br from-success to-emerald-700"
        subtext="↑ 4% vs last month"
      />
      <GradientCard
        title="Health Checks"
        value="856"
        icon={<Activity size={24} />}
        gradient="bg-gradient-to-br from-accent-500 to-accent-700"
        subtext="328 pending review"
      />
      <GradientCard
        title="Avg Response"
        value="4m 32s"
        icon={<Clock size={24} />}
        gradient="bg-gradient-to-br from-warning to-orange-700"
        subtext="↓ 45s vs yesterday"
      />
    </div>
  );
}
