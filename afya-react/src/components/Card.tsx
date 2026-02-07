import { ReactNode } from 'react';

interface CardProps {
  children: ReactNode;
  className?: string;
  onClick?: () => void;
}

export function Card({ children, className = '', onClick }: CardProps) {
  return (
    <div 
      onClick={onClick}
      className={`bg-white rounded-2xl p-6 shadow-card hover:shadow-elevated transition-shadow ${onClick ? 'cursor-pointer' : ''} ${className}`}
    >
      {children}
    </div>
  );
}

interface GradientCardProps {
  title: string;
  value: string | number;
  icon: ReactNode;
  gradient: string;
  subtext?: string;
}

export function GradientCard({ title, value, icon, gradient, subtext }: GradientCardProps) {
  return (
    <div className={`${gradient} rounded-2xl p-6 text-white shadow-elevated overflow-hidden relative`}>
      <div className="absolute top-0 right-0 w-32 h-32 opacity-10" style={{ background: 'radial-gradient(circle, white 0%, transparent 70%)' }}></div>
      
      <div className="flex justify-between items-start relative z-10">
        <div>
          <p className="text-white/80 text-sm font-medium mb-2">{title}</p>
          <p className="text-3xl font-bold mb-1">{value}</p>
          {subtext && <p className="text-white/70 text-xs">{subtext}</p>}
        </div>
        <div className="p-3 bg-white/20 rounded-xl">{icon}</div>
      </div>
    </div>
  );
}
