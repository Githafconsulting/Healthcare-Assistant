import { useState } from 'react'
import { Header } from './components/Header'
import { Sidebar } from './components/Sidebar'
import { MetricsGrid } from './components/MetricsGrid'
import { QuickActions } from './components/QuickActions'
import { PatientsList } from './components/PatientsList'
import { ClinicalGuidance } from './components/ClinicalGuidance'
import { Card } from './components/Card'
import { TrendingUp, AlertCircle, Users, Zap } from 'lucide-react'
import './App.css'

function App() {
  const [activeTab, setActiveTab] = useState('dashboard')

  return (
    <div className="flex h-screen bg-gray-50">
      <Sidebar activeTab={activeTab} onTabChange={setActiveTab} />
      
      <div className="flex-1 flex flex-col overflow-hidden">
        <Header />
        
        <main className="flex-1 overflow-auto">
          <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
            {/* Dashboard View */}
            {activeTab === 'dashboard' && (
              <div className="space-y-8">
                {/* Welcome Section */}
                <div>
                  <h1 className="text-3xl font-bold text-gray-900">Welcome back, Dr. Kariuki</h1>
                  <p className="text-gray-600 mt-1">Here's your patient overview for today</p>
                </div>

                {/* Metrics Grid */}
                <MetricsGrid />

                {/* Quick Actions */}
                <section>
                  <h2 className="text-xl font-bold text-gray-900 mb-4">Quick Actions</h2>
                  <QuickActions />
                </section>

                {/* Main Content Grid */}
                <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
                  {/* Left Column - Recent Patients */}
                  <div className="lg:col-span-2 space-y-6">
                    <section>
                      <div className="flex items-center justify-between mb-4">
                        <h2 className="text-xl font-bold text-gray-900">My Patients</h2>
                        <button className="text-primary-600 hover:text-primary-700 font-semibold text-sm">
                          View All
                        </button>
                      </div>
                      <PatientsList />
                    </section>

                    {/* Clinical Guidance */}
                    <section>
                      <div className="flex items-center justify-between mb-4">
                        <h2 className="text-xl font-bold text-gray-900">Clinical Resources</h2>
                        <button className="text-primary-600 hover:text-primary-700 font-semibold text-sm">
                          Browse All
                        </button>
                      </div>
                      <ClinicalGuidance />
                    </section>
                  </div>

                  {/* Right Column - Sidebar Cards */}
                  <div className="space-y-6">
                    {/* Today's Summary */}
                    <Card>
                      <div className="flex items-start gap-3 mb-4">
                        <div className="p-3 bg-green-50 rounded-lg">
                          <TrendingUp className="text-success" size={20} />
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900">Today's Summary</h3>
                          <p className="text-xs text-gray-500">Feb 7, 2026</p>
                        </div>
                      </div>
                      <div className="space-y-3">
                        <div>
                          <p className="text-xs text-gray-600">Consultations</p>
                          <p className="text-2xl font-bold text-gray-900">8</p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-600">Referrals</p>
                          <p className="text-2xl font-bold text-gray-900">2</p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-600">Follow-ups Due</p>
                          <p className="text-2xl font-bold text-gray-900">12</p>
                        </div>
                      </div>
                    </Card>

                    {/* Urgent Alerts */}
                    <Card>
                      <div className="flex items-start gap-3 mb-4">
                        <div className="p-3 bg-red-50 rounded-lg">
                          <AlertCircle className="text-danger" size={20} />
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900">Urgent Alerts</h3>
                          <p className="text-xs text-gray-500">Action required</p>
                        </div>
                      </div>
                      <div className="space-y-2">
                        <div className="p-3 bg-red-50 rounded-lg border border-red-100">
                          <p className="text-xs font-semibold text-danger">Critical: Sarah K.</p>
                          <p className="text-xs text-gray-600">High fever & convulsions</p>
                        </div>
                        <div className="p-3 bg-orange-50 rounded-lg border border-orange-100">
                          <p className="text-xs font-semibold text-warning">Warning: James O.</p>
                          <p className="text-xs text-gray-600">Severe malaria symptoms</p>
                        </div>
                      </div>
                    </Card>

                    {/* Team Stats */}
                    <Card>
                      <div className="flex items-start gap-3 mb-4">
                        <div className="p-3 bg-blue-50 rounded-lg">
                          <Users className="text-primary-600" size={20} />
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900">Team Impact</h3>
                          <p className="text-xs text-gray-500">This month</p>
                        </div>
                      </div>
                      <div className="space-y-3">
                        <div>
                          <p className="text-xs text-gray-600">Patients Treated</p>
                          <p className="text-2xl font-bold text-gray-900">312</p>
                        </div>
                        <div>
                          <p className="text-xs text-gray-600">Success Rate</p>
                          <p className="text-2xl font-bold text-gray-900">94%</p>
                        </div>
                      </div>
                    </Card>

                    {/* Quick Tip */}
                    <Card className="bg-gradient-to-br from-accent-50 to-primary-50 border border-primary-100">
                      <div className="flex items-start gap-3">
                        <div className="p-2 bg-primary-100 rounded-lg text-primary-600">
                          <Zap size={18} />
                        </div>
                        <div>
                          <h3 className="font-semibold text-gray-900 text-sm">Pro Tip</h3>
                          <p className="text-xs text-gray-700 mt-1">
                            Use voice input to quickly log patient visits and generate structured notes.
                          </p>
                        </div>
                      </div>
                    </Card>
                  </div>
                </div>
              </div>
            )}

            {/* Patients View */}
            {activeTab === 'patients' && (
              <div className="space-y-6">
                <h1 className="text-3xl font-bold text-gray-900">My Patients</h1>
                <PatientsList />
              </div>
            )}

            {/* Guidance View */}
            {activeTab === 'guidance' && (
              <div className="space-y-6">
                <h1 className="text-3xl font-bold text-gray-900">Clinical Guidance</h1>
                <ClinicalGuidance />
              </div>
            )}

            {/* Reports View */}
            {activeTab === 'reports' && (
              <div className="space-y-6">
                <h1 className="text-3xl font-bold text-gray-900">Reports & Analytics</h1>
                <Card className="p-12 text-center">
                  <BarChart3Icon size={48} className="mx-auto text-gray-300 mb-4" />
                  <p className="text-gray-600">Reports coming soon</p>
                </Card>
              </div>
            )}
          </div>
        </main>
      </div>
    </div>
  )
}

function BarChart3Icon(props: any) {
  return <TrendingUp {...props} />
}

export default App
