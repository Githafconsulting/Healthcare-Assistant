import { Bell, Settings, User, Menu, X } from 'lucide-react';
import { useState } from 'react';

export function Header() {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  return (
    <header className="sticky top-0 z-50 bg-white border-b border-gray-100">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex justify-between items-center h-16">
          {/* Logo */}
          <div className="flex items-center gap-2">
            <div className="w-10 h-10 bg-gradient-to-br from-primary-500 to-accent-600 rounded-xl flex items-center justify-center">
              <span className="text-white font-bold text-lg">A</span>
            </div>
            <span className="text-xl font-bold bg-gradient-to-r from-primary-600 to-accent-600 bg-clip-text text-transparent">
              Afya
            </span>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden md:flex items-center gap-8">
            <a href="#" className="text-gray-700 hover:text-primary-600 font-medium">Dashboard</a>
            <a href="#" className="text-gray-700 hover:text-primary-600 font-medium">Patients</a>
            <a href="#" className="text-gray-700 hover:text-primary-600 font-medium">Guidelines</a>
            <a href="#" className="text-gray-700 hover:text-primary-600 font-medium">Support</a>
          </nav>

          {/* Actions */}
          <div className="flex items-center gap-4">
            <button className="relative p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg transition">
              <Bell size={20} />
              <span className="absolute top-1 right-1 w-2 h-2 bg-danger rounded-full"></span>
            </button>
            <button className="p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg transition hidden sm:block">
              <Settings size={20} />
            </button>
            <button className="p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg transition hidden sm:block">
              <User size={20} />
            </button>

            {/* Mobile menu button */}
            <button 
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="md:hidden p-2 text-gray-600 hover:text-primary-600 hover:bg-gray-50 rounded-lg"
            >
              {mobileMenuOpen ? <X size={20} /> : <Menu size={20} />}
            </button>
          </div>
        </div>

        {/* Mobile menu */}
        {mobileMenuOpen && (
          <nav className="md:hidden pb-4 space-y-2">
            <a href="#" className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg">Dashboard</a>
            <a href="#" className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg">Patients</a>
            <a href="#" className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg">Guidelines</a>
            <a href="#" className="block px-4 py-2 text-gray-700 hover:bg-gray-50 rounded-lg">Support</a>
          </nav>
        )}
      </div>
    </header>
  );
}
