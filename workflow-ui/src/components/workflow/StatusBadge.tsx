import { Badge } from '../ui/badge';
import { cn } from '../../lib/utils';

const statusColors: Record<string, string> = {
  DRAFT: 'bg-gray-100 text-gray-700 border-gray-300',
  ACTIVE: 'bg-green-100 text-green-700 border-green-300',
  DEPRECATED: 'bg-amber-100 text-amber-700 border-amber-300',
  RUNNING: 'bg-blue-100 text-blue-700 border-blue-300',
  COMPLETED: 'bg-green-100 text-green-700 border-green-300',
  FAILED: 'bg-red-100 text-red-700 border-red-300',
  SUSPENDED: 'bg-yellow-100 text-yellow-700 border-yellow-300',
  CANCELLED: 'bg-gray-100 text-gray-500 border-gray-300',
  PENDING: 'bg-gray-100 text-gray-600 border-gray-300',
  WAITING_FOR_INPUT: 'bg-purple-100 text-purple-700 border-purple-300',
  SKIPPED: 'bg-gray-100 text-gray-500 border-gray-300',
};

export function StatusBadge({ status }: { status: string }) {
  return (
    <Badge
      variant="outline"
      className={cn('text-xs font-medium', statusColors[status] || 'bg-gray-100 text-gray-600')}
    >
      {status.replace(/_/g, ' ')}
    </Badge>
  );
}
