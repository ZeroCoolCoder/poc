import { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { workflowApi } from '../api/client';
import type { WorkflowInstanceResponse, NodeExecutionResponse } from '../types/workflow';
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
import { Input } from '../components/ui/input';
import { Label } from '../components/ui/label';
import { Textarea } from '../components/ui/textarea';
import { Separator } from '../components/ui/separator';
import {
  ArrowLeft,
  RefreshCw,
  Pause,
  Play,
  XCircle,
  Send,
} from 'lucide-react';

export function InstanceDetailPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const instanceId = parseInt(id || '0');

  const [instance, setInstance] = useState<WorkflowInstanceResponse | null>(null);
  const [executions, setExecutions] = useState<NodeExecutionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showActionDialog, setShowActionDialog] = useState(false);
  const [actionNodeKey, setActionNodeKey] = useState<string | undefined>(undefined);
  const [actionName, setActionName] = useState('');
  const [actionPayload, setActionPayload] = useState('');
  const [submittedBy, setSubmittedBy] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadData = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const [instRes, execRes] = await Promise.all([
        workflowApi.getInstance(instanceId),
        workflowApi.getNodeExecutions(instanceId),
      ]);
      setInstance(instRes.data);
      setExecutions(execRes.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load instance');
    } finally {
      setLoading(false);
    }
  }, [instanceId]);

  useEffect(() => {
    loadData();
  }, [loadData]);

  const handleAction = async (action: 'cancel' | 'suspend' | 'resume') => {
    try {
      setError(null);
      switch (action) {
        case 'cancel':
          await workflowApi.cancelWorkflow(instanceId);
          break;
        case 'suspend':
          await workflowApi.suspendWorkflow(instanceId);
          break;
        case 'resume':
          await workflowApi.resumeWorkflow(instanceId);
          break;
      }
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : `Failed to ${action} workflow`);
    }
  };

  const openActionDialog = (nodeKey?: string) => {
    setActionNodeKey(nodeKey);
    setShowActionDialog(true);
  };

  const handleSubmitAction = async () => {
    setSubmitting(true);
    setError(null);
    try {
      let payload: Record<string, unknown> | undefined;
      if (actionPayload.trim()) {
        payload = JSON.parse(actionPayload);
      }
      await workflowApi.submitAction({
        workflowInstanceId: instanceId,
        nodeKey: actionNodeKey,
        action: actionName || undefined,
        payload,
        submittedBy: submittedBy || undefined,
      });
      setShowActionDialog(false);
      setActionNodeKey(undefined);
      setActionName('');
      setActionPayload('');
      setSubmittedBy('');
      loadData();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to submit action');
    } finally {
      setSubmitting(false);
    }
  };

  const waitingNodes = executions.filter(
    (exec) => exec.status === 'WAITING_FOR_INPUT'
  );

  const formatJson = (json: string | undefined) => {
    if (!json) return '-';
    try {
      return JSON.stringify(JSON.parse(json), null, 2);
    } catch {
      return json;
    }
  };

  if (loading) {
    return <div className="text-center py-12 text-gray-500">Loading...</div>;
  }

  if (!instance) {
    return <div className="text-center py-12 text-gray-500">Instance not found</div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/instances')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              Instance #{instance.id}
            </h1>
            <div className="flex items-center gap-2 mt-1">
              <StatusBadge status={instance.status} />
              {instance.correlationId && (
                <span className="text-xs text-gray-500">
                  Correlation: {instance.correlationId}
                </span>
              )}
            </div>
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" size="sm" onClick={loadData} className="gap-1">
            <RefreshCw className="h-3.5 w-3.5" />
            Refresh
          </Button>
          {instance.status === 'RUNNING' && (
            <>
              <Button
                variant="outline"
                size="sm"
                onClick={() => openActionDialog()}
                className="gap-1"
              >
                <Send className="h-3.5 w-3.5" />
                Submit Action
              </Button>
              <Button
                variant="outline"
                size="sm"
                onClick={() => handleAction('suspend')}
                className="gap-1"
              >
                <Pause className="h-3.5 w-3.5" />
                Suspend
              </Button>
              <Button
                variant="destructive"
                size="sm"
                onClick={() => handleAction('cancel')}
                className="gap-1"
              >
                <XCircle className="h-3.5 w-3.5" />
                Cancel
              </Button>
            </>
          )}
          {instance.status === 'SUSPENDED' && (
            <Button
              variant="outline"
              size="sm"
              onClick={() => handleAction('resume')}
              className="gap-1"
            >
              <Play className="h-3.5 w-3.5" />
              Resume
            </Button>
          )}
        </div>
      </div>

      {error && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="pt-4">
            <p className="text-sm text-red-600">{error}</p>
          </CardContent>
        </Card>
      )}

      <div className="grid grid-cols-3 gap-4">
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Current Node</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-lg font-semibold">{instance.currentNodeKey || 'N/A'}</p>
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Created</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm">{new Date(instance.createdAt).toLocaleString()}</p>
            {instance.createdBy && (
              <p className="text-xs text-gray-500">by {instance.createdBy}</p>
            )}
          </CardContent>
        </Card>
        <Card>
          <CardHeader className="pb-2">
            <CardTitle className="text-sm text-gray-500">Duration</CardTitle>
          </CardHeader>
          <CardContent>
            <p className="text-sm">
              {instance.completedAt
                ? `${Math.round(
                    (new Date(instance.completedAt).getTime() - new Date(instance.createdAt).getTime()) / 1000
                  )}s`
                : 'In progress'}
            </p>
          </CardContent>
        </Card>
      </div>

      {waitingNodes.length > 0 && (
        <Card className="border-amber-200 bg-amber-50">
          <CardHeader>
            <CardTitle className="text-amber-800">Pending Approvals</CardTitle>
            <CardDescription className="text-amber-600">
              {waitingNodes.length} node{waitingNodes.length !== 1 ? 's' : ''} waiting for input
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap gap-3">
              {waitingNodes.map((exec) => (
                <Button
                  key={exec.id}
                  variant="outline"
                  className="border-amber-300 bg-white hover:bg-amber-100 gap-2"
                  onClick={() => openActionDialog(exec.nodeKey)}
                >
                  <Send className="h-4 w-4 text-amber-600" />
                  <span className="font-medium">{exec.nodeKey}</span>
                </Button>
              ))}
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Context Data</CardTitle>
          <CardDescription>Current workflow context (shared state)</CardDescription>
        </CardHeader>
        <CardContent>
          <pre className="bg-gray-50 rounded-lg p-4 text-xs font-mono overflow-auto max-h-48 border">
            {formatJson(instance.contextData)}
          </pre>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Execution History</CardTitle>
          <CardDescription>
            {executions.length} node execution{executions.length !== 1 ? 's' : ''}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {executions.length === 0 ? (
            <p className="text-center py-4 text-gray-500 text-sm">No executions yet</p>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Node</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Retries</TableHead>
                  <TableHead>Started</TableHead>
                  <TableHead>Completed</TableHead>
                  <TableHead>Error</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {executions.map((exec) => (
                  <TableRow key={exec.id}>
                    <TableCell className="font-medium text-sm">{exec.nodeKey}</TableCell>
                    <TableCell>
                      <StatusBadge status={exec.status} />
                    </TableCell>
                    <TableCell className="text-sm text-gray-600">
                      {exec.retryCount || 0}
                    </TableCell>
                    <TableCell className="text-xs text-gray-500">
                      {exec.startedAt ? new Date(exec.startedAt).toLocaleTimeString() : '-'}
                    </TableCell>
                    <TableCell className="text-xs text-gray-500">
                      {exec.completedAt ? new Date(exec.completedAt).toLocaleTimeString() : '-'}
                    </TableCell>
                    <TableCell className="text-xs text-red-500 max-w-48 truncate">
                      {exec.errorMessage || '-'}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>

      <Separator />

      <Dialog open={showActionDialog} onOpenChange={setShowActionDialog}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>
              Submit External Action
              {actionNodeKey && (
                <span className="text-sm font-normal text-gray-500 ml-2">
                  (node: {actionNodeKey})
                </span>
              )}
            </DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="actionNameField">Action Name (optional)</Label>
              <Input
                id="actionNameField"
                value={actionName}
                onChange={(e) => setActionName(e.target.value)}
                className="mt-1"
                placeholder="e.g. approve, reject"
              />
            </div>
            <div>
              <Label htmlFor="submittedByField">Submitted By (optional)</Label>
              <Input
                id="submittedByField"
                value={submittedBy}
                onChange={(e) => setSubmittedBy(e.target.value)}
                className="mt-1"
                placeholder="e.g. john.doe"
              />
            </div>
            <div>
              <Label htmlFor="payloadField">Payload (JSON, optional)</Label>
              <Textarea
                id="payloadField"
                value={actionPayload}
                onChange={(e) => setActionPayload(e.target.value)}
                className="mt-1 font-mono text-sm"
                rows={4}
                placeholder='{"decision": "approve", "comment": "Looks good"}'
              />
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowActionDialog(false)}>
              Cancel
            </Button>
            <Button onClick={handleSubmitAction} disabled={submitting}>
              {submitting ? 'Submitting...' : 'Submit Action'}
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
