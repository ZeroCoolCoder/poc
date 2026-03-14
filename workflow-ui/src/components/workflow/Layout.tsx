import { Outlet, Link, useLocation } from 'react-router-dom';
import { GitBranch, LayoutDashboard, Play } from 'lucide-react';
import { cn } from '../../lib/utils';

const navItems = [
  { path: '/definitions', label: 'Definitions', icon: GitBranch },
  { path: '/instances', label: 'Instances', icon: Play },
];

export function Layout() {
  const location = useLocation();

  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-white border-b border-gray-200 shadow-sm">
        <div className="max-w-screen-2xl mx-auto px-6 py-3 flex items-center justify-between">
          <Link to="/" className="flex items-center gap-2">
            <LayoutDashboard className="h-6 w-6 text-indigo-600" />
            <span className="text-xl font-bold text-gray-900">Workflow Engine</span>
          </Link>
          <nav className="flex items-center gap-1">
            {navItems.map((item) => {
              const Icon = item.icon;
              const isActive = location.pathname.startsWith(item.path);
              return (
                <Link
                  key={item.path}
                  to={item.path}
                  className={cn(
                    'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-medium transition-colors',
                    isActive
                      ? 'bg-indigo-50 text-indigo-700'
                      : 'text-gray-600 hover:text-gray-900 hover:bg-gray-100'
                  )}
                >
                  <Icon className="h-4 w-4" />
                  {item.label}
                </Link>
              );
            })}
          </nav>
        </div>
      </header>
      <main className="max-w-screen-2xl mx-auto px-6 py-6">
        <Outlet />
      </main>
    </div>
  );
}
