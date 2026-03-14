import { memo } from 'react';
import { Handle, Position, type NodeProps } from '@xyflow/react';
import { cn } from '../../lib/utils';
import {
  Play,
  Square,
  Cog,
  Clock,
  GitFork,
  Merge,
} from 'lucide-react';
import type { NodeType } from '../../types/workflow';

const nodeTypeConfig: Record<NodeType, { icon: typeof Play; color: string; bg: string; border: string }> = {
  START: { icon: Play, color: 'text-green-600', bg: 'bg-green-50', border: 'border-green-300' },
  END: { icon: Square, color: 'text-red-600', bg: 'bg-red-50', border: 'border-red-300' },
  AUTO: { icon: Cog, color: 'text-blue-600', bg: 'bg-blue-50', border: 'border-blue-300' },
  WAIT_FOR_INPUT: { icon: Clock, color: 'text-purple-600', bg: 'bg-purple-50', border: 'border-purple-300' },
  FORK: { icon: GitFork, color: 'text-amber-600', bg: 'bg-amber-50', border: 'border-amber-300' },
  JOIN: { icon: Merge, color: 'text-amber-600', bg: 'bg-amber-50', border: 'border-amber-300' },
};

interface WorkflowNodeData {
  label: string;
  nodeType: NodeType;
  actionHandler?: string;
  [key: string]: unknown;
}

function WorkflowNodeComponent({ data, selected }: NodeProps & { data: WorkflowNodeData }) {
  const config = nodeTypeConfig[data.nodeType] || nodeTypeConfig.AUTO;
  const Icon = config.icon;

  return (
    <div
      className={cn(
        'px-4 py-3 rounded-lg border-2 shadow-sm min-w-40 transition-shadow',
        config.bg,
        config.border,
        selected && 'ring-2 ring-indigo-400 shadow-md'
      )}
    >
      {data.nodeType !== 'START' && (
        <Handle
          type="target"
          position={Position.Top}
          className="!bg-gray-400 !w-3 !h-3 !border-2 !border-white"
        />
      )}

      <div className="flex items-center gap-2">
        <Icon className={cn('h-4 w-4', config.color)} />
        <div>
          <div className="text-sm font-medium text-gray-900">{data.label}</div>
          <div className="text-xs text-gray-500">{data.nodeType}</div>
          {data.actionHandler && (
            <div className="text-xs text-gray-400 mt-0.5">
              handler: {data.actionHandler}
            </div>
          )}
        </div>
      </div>

      {data.nodeType !== 'END' && (
        <Handle
          type="source"
          position={Position.Bottom}
          className="!bg-gray-400 !w-3 !h-3 !border-2 !border-white"
        />
      )}
    </div>
  );
}

export const WorkflowNode = memo(WorkflowNodeComponent);
