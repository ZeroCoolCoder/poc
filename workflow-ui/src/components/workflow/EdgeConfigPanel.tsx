import { useCallback } from 'react';
import { Input } from '../ui/input';
import { Label } from '../ui/label';
import { Textarea } from '../ui/textarea';
import { Button } from '../ui/button';
import { Separator } from '../ui/separator';
import { X } from 'lucide-react';

interface EdgeData {
  name?: string;
  conditionExpression?: string;
  priority?: number;
}

interface EdgeConfigPanelProps {
  edgeData: EdgeData;
  sourceLabel: string;
  targetLabel: string;
  onUpdate: (data: Partial<EdgeData>) => void;
  onClose: () => void;
  onDelete: () => void;
}

export function EdgeConfigPanel({
  edgeData,
  sourceLabel,
  targetLabel,
  onUpdate,
  onClose,
  onDelete,
}: EdgeConfigPanelProps) {
  const handleChange = useCallback(
    (field: keyof EdgeData, value: string | number | undefined) => {
      onUpdate({ [field]: value });
    },
    [onUpdate]
  );

  return (
    <div className="w-80 bg-white border-l border-gray-200 p-4 overflow-y-auto">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-sm font-semibold text-gray-900">Transition Config</h3>
        <Button variant="ghost" size="icon" onClick={onClose} className="h-7 w-7">
          <X className="h-4 w-4" />
        </Button>
      </div>

      <div className="text-xs text-gray-500 mb-4">
        {sourceLabel} &rarr; {targetLabel}
      </div>

      <div className="space-y-4">
        <div>
          <Label htmlFor="name" className="text-xs">Transition Name</Label>
          <Input
            id="name"
            value={edgeData.name || ''}
            onChange={(e) => handleChange('name', e.target.value)}
            className="mt-1 h-8 text-sm"
            placeholder="e.g. approve-path"
          />
        </div>

        <div>
          <Label htmlFor="conditionExpression" className="text-xs">
            Condition (SpEL Expression)
          </Label>
          <Textarea
            id="conditionExpression"
            value={edgeData.conditionExpression || ''}
            onChange={(e) => handleChange('conditionExpression', e.target.value)}
            className="mt-1 text-sm font-mono"
            rows={3}
            placeholder="#decision == 'approve'"
          />
          <p className="text-xs text-gray-400 mt-1">
            Leave empty for default/unconditional transition
          </p>
        </div>

        <div>
          <Label htmlFor="priority" className="text-xs">Priority</Label>
          <Input
            id="priority"
            type="number"
            min={0}
            value={edgeData.priority ?? ''}
            onChange={(e) =>
              handleChange('priority', e.target.value ? parseInt(e.target.value) : undefined)
            }
            className="mt-1 h-8 text-sm"
            placeholder="0 (lowest first)"
          />
        </div>

        <Separator />

        <Button variant="destructive" size="sm" onClick={onDelete} className="w-full">
          Delete Transition
        </Button>
      </div>
    </div>
  );
}
