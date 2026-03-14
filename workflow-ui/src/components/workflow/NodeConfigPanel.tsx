import { useCallback } from 'react';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Button } from '../ui/button';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../ui/select';
import { Separator } from '../ui/separator';
import { X } from 'lucide-react';
import type { NodeType } from '../../types/workflow';

interface NodeData {
  nodeKey: string;
  label: string;
  description?: string;
  nodeType: NodeType;
  actionHandler?: string;
  configuration?: string;
  timeoutSeconds?: number;
  maxRetries?: number;
}

interface NodeConfigPanelProps {
  nodeData: NodeData;
  onUpdate: (data: Partial<NodeData>) => void;
  onClose: () => void;
  onDelete: () => void;
}

const nodeTypes: NodeType[] = ['START', 'END', 'AUTO', 'WAIT_FOR_INPUT', 'FORK', 'JOIN'];
const actionHandlers = ['log', 'dataTransform', 'httpCall'];

export function NodeConfigPanel({ nodeData, onUpdate, onClose, onDelete }: NodeConfigPanelProps) {
  const handleChange = useCallback(
    (field: keyof NodeData, value: string | number | undefined) => {
      onUpdate({ [field]: value });
    },
    [onUpdate]
  );

  return (
    <div className="w-80 bg-white border-l border-gray-200 p-4 overflow-y-auto">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-gray-900">Node Configuration</h3>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-7 w-7">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="space-y-4">
        <div>
          <Label htmlFor="nodeKey" className="text-xs">Node Key</Label>
          <Input
            id="nodeKey"
            value={nodeData.nodeKey}
            onChange={(e) => handleChange('nodeKey', e.target.value)}
            className="mt-1 h-8 text-sm"
          />
        </div>

        <div>
          <Label htmlFor="label" className="text-xs">Display Name</Label>
          <Input
            id="label"
            value={nodeData.label}
            onChange={(e) => handleChange('label', e.target.value)}
            className="mt-1 h-8 text-sm"
          />
        </div>

        <div>
          <Label htmlFor="nodeType" className="text-xs">Node Type</Label>
          <Select
            value={nodeData.nodeType}
            onValueChange={(v) => handleChange('nodeType', v)}
          >
            <SelectTrigger className="mt-1 h-8 text-sm">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              {nodeTypes.map((t) => (
                <SelectItem key={t} value={t}>{t}</SelectItem>
              ))}
            </SelectContent>
          </Select>
        </div>

        <div>
          <Label htmlFor="description" className="text-xs">Description</Label>
          <Textarea
            id="description"
            value={nodeData.description || ''}
            onChange={(e) => handleChange('description', e.target.value)}
            className="mt-1 text-sm"
            rows={2}
          />
        </div>

        <Separator />

        {(nodeData.nodeType === 'AUTO') && (
          <>
            <div>
              <Label htmlFor="actionHandler" className="text-xs">Action Handler</Label>
              <Select
                value={nodeData.actionHandler || ''}
                onValueChange={(v) => handleChange('actionHandler', v)}
              >
                <SelectTrigger className="mt-1 h-8 text-sm">
                  <SelectValue placeholder="Select handler..." />
                </SelectTrigger>
                <SelectContent>
                  {actionHandlers.map((h) => (
                    <SelectItem key={h} value={h}>{h}</SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>

            <div>
              <Label htmlFor="configuration" className="text-xs">Configuration (JSON)</Label>
              <Textarea
                id="configuration"
                value={nodeData.configuration || ''}
                onChange={(e) => handleChange('configuration', e.target.value)}
                className="mt-1 text-sm font-mono"
                rows={4}
                placeholder='{"key": "value"}'
              />
            </div>

            <div>
              <Label htmlFor="maxRetries" className="text-xs">Max Retries</Label>
              <Input
                id="maxRetries"
                type="number"
                min={0}
                value={nodeData.maxRetries ?? ''}
                onChange={(e) =>
                  handleChange('maxRetries', e.target.value ? parseInt(e.target.value) : undefined)
                }
                className="mt-1 h-8 text-sm"
              />
            </div>
          </>
        )}

        <div>
          <Label htmlFor="timeoutSeconds" className="text-xs">Timeout (seconds)</Label>
          <Input
            id="timeoutSeconds"
            type="number"
            min={0}
            value={nodeData.timeoutSeconds ?? ''}
            onChange={(e) =>
              handleChange('timeoutSeconds', e.target.value ? parseInt(e.target.value) : undefined)
            }
            className="mt-1 h-8 text-sm"
          />
        </div>

        <Separator />

        <Button variant="destructive" size="sm" onClick={onDelete} className="w-full">
          Delete Node
        </Button>
      </div>
    </div>
  );
}
