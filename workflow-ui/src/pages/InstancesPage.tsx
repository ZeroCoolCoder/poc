import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { workflowApi } from '../api/client';
import type { WorkflowDefinitionResponse, WorkflowInstanceResponse, WorkflowStatus } from '../types/workflow';
import { StatusBadge } from '../components/workflow/StatusBadge';
import { Button } from '../components/ui/button';
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '../components/ui/card';
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '../components/ui/table';
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '../components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '../components/ui/tabs';
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Textarea } from '../components/ui/textarea';
import { Plus, RefreshCw } from 'lucide-react';

const statusFilters: (WorkflowStatus | 'ALL')[] = ['ALL', 'RUNNING', 'COMPLETED', 'FAILED', 'SUSPENDED', 'CANCELLED'];

export function InstancesPage() {
  const [instances, setInstances] = useState<WorkflowInstanceResponse[]>([]);
  const [definitions, setDefinitions] = useState<WorkflowDefinitionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [activeTab, setActiveTab] = useState<string>('ALL');
  const [showStartDialog, setShowStartDialog] = useState(false);
  const [selectedDefId, setSelectedDefId] = useState<string>('');
  const [correlationId, setCorrelationId] = useState('');
  const [initialContext, setInitialContext] = useState('');
  const [createdBy, setCreatedBy] = useState('');
  const [starting, setStarting] = useState(false);
  const navigate = useNavigate();

  const loadInstances = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      if (activeTab === 'ALL') {
        const runningRes = await workflowApi.getInstancesByStatus('RUNNING');
        const completedRes = await workflowApi.getInstancesByStatus('COMPLETED');
        const failedRes = await workflowApi.getInstancesByStatus('FAILED');
        const suspendedRes = await workflowApi.getInstancesByStatus('SUSPENDED');
        const cancelledRes = await workflowApi.getInstancesByStatus('CANCELLED');
        setInstances([
          ...runningRes.data,
          ...completedRes.data,
          ...failedRes.data,
          ...suspendedRes.data,
          ...cancelledRes.data,
        ].sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()));
      } else {
        const res = await workflowApi.getInstancesByStatus(activeTab as WorkflowStatus);
        setInstances(res.data);
      }
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load instances');
    } finally {
      setLoading(false);
    }
  }, [activeTab]);

  const loadDefinitions = useCallback(async () => {
    try {
      const res = await workflowApi.getActiveDefinitions();
      setDefinitions(res.data);
    } catch {
      // silently fail for definitions load
    }
  }, []);

  useEffect(() => {
    loadInstances();
    loadDefinitions();
  }, [loadInstances, loadDefinitions]);

  const handleStart = async () => {
    if (!selectedDefId) return;
    setStarting(true);
    setError(null);
    try {
      let context: Record<string, unknown> | undefined;
      if (initialContext.trim()) {
        context = JSON.parse(initialContext);
      }
      const res = await workflowApi.startWorkflow({
        workflowDefinitionId: parseInt(selectedDefId),
        correlationId: correlationId || undefined,
        initialContext: context,
        createdBy: createdBy || undefined,
      });
      setShowStartDialog(false);
      setSelectedDefId('');
      setCorrelationId('');
      setInitialContext('');
      setCreatedBy('');
      navigate(`/instances/${res.data.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to start workflow');
    } finally {
      setStarting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Workflow Instances</h1>
          <p className="text-sm text-gray-500 mt-1">Monitor and manage running workflows</p>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={loadInstances} className="gap-2">
            <RefreshCw className="h-4 w-4" />
            Refresh
          </Button>
          <Button onClick={() => setShowStartDialog(true)} className="gap-2">
            <Plus className="h-4 w-4" />
            Start Workflow
          </Button>
        </div>
      </div>

      {error && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="pt-4">
            <p className="text-sm text-red-600">{error}</p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Instances</CardTitle>
          <CardDescription>
            {instances.length} workflow instance{instances.length !== 1 ? 's' : ''}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={activeTab} onValueChange={setActiveTab}>
            <TabsList className="mb-4">
              {statusFilters.map((s) => (
                <TabsTrigger key={s} value={s} className="text-xs">
                  {s}
                </TabsTrigger>
              ))}
            </TabsList>

            {statusFilters.map((s) => (
              <TabsContent key={s} value={s}>
                {loading ? (
                  <div className="text-center py-8 text-gray-500">Loading...</div>
                ) : instances.length === 0 ? (
                  <div className="text-center py-8 text-gray-500">No instances found</div>
                ) : (
                  <Table>
                    <TableHeader>
                      <TableRow>
                        <TableHead>ID</TableHead>
                        <TableHead>Definition</TableHead>
                        <TableHead>Status</TableHead>
                        <TableHead>Current Node</TableHead>
                        <TableHead>Created By</TableHead>
                        <TableHead>Created</TableHead>
                      </TableRow>
                    </TableHeader>
                    <TableBody>
                      {instances.map((inst) => (
                        <TableRow key={inst.id} className="cursor-pointer hover:bg-gray-50">
                          <TableCell>
                            <Link
                              to={`/instances/${inst.id}`}
                              className="font-medium text-indigo-600 hover:text-indigo-800"
                            >
                              #{inst.id}
                            </Link>
                          </TableCell>
                          <TableCell className="text-sm text-gray-600">
                            {inst.workflowDefinitionId}
                          </TableCell>
                          <TableCell>
                            <StatusBadge status={inst.status} />
                          </TableCell>
                          <TableCell className="text-sm text-gray-600">
                            {inst.currentNodeKey || '-'}
                          </TableCell>
                          <TableCell className="text-sm text-gray-500">
                            {inst.createdBy || '-'}
                          </TableCell>
                          <TableCell className="text-sm text-gray-500">
                            {new Date(inst.createdAt).toLocaleString()}
                          </TableCell>
                        </TableRow>
                      ))}
                    </TableBody>
                  </Table>
                )}
              </TabsContent>
            ))}
          </Tabs>
        </CardContent>
      </Card>

      <Dialog open={showStartDialog} onOpenChange={setShowStartDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Start New Workflow</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label>Workflow Definition</Label>
              <Select value={selectedDefId} onValueChange={setSelectedDefId}>
                <SelectTrigger className="mt-1">
                  <SelectValue placeholder="Select a workflow..." />
                </SelectTrigger>
                <SelectContent>
                  {definitions.map((d) => (
                    <SelectItem key={d.id} value={String(d.id)}>
                      {d.name} (v{d.version})
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label htmlFor="corrId">Correlation ID (optional)</Label>
              <Input
                id="corrId"
                value={correlationId}
                onChange={(e) => setCorrelationId(e.target.value)}
                className="mt-1"
                placeholder="e.g. ORDER-12345"
              />
            </div>
            <div>
              <Label htmlFor="createdByField">Created By (optional)</Label>
              <Input
                id="createdByField"
                value={createdBy}
                onChange={(e) => setCreatedBy(e.target.value)}
                className="mt-1"
                placeholder="e.g. admin"
              />
            </div>
            <div>
              <Label htmlFor="initCtx">Initial Context (JSON, optional)</Label>
              <Textarea
                id="initCtx"
                value={initialContext}
                onChange={(e) => setInitialContext(e.target.value)}
                className="mt-1 font-mono text-sm"
                rows={4}
                placeholder='{"key": "value"}'
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowStartDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleStart} disabled={!selectedDefId || starting}>
              {starting ? 'Starting...' : 'Start Workflow'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
