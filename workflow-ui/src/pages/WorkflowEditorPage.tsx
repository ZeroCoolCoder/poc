import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  addEdge,
  useNodesState,
  useEdgesState,
  type Connection,
  type Node,
  type Edge,
  BackgroundVariant,
  Panel,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';
import { workflowApi } from '../api/client';
import type {
  CreateWorkflowDefinitionRequest,
  NodeType,
  WorkflowDefinitionResponse,
} from '../types/workflow';
import { WorkflowNode } from '../components/workflow/WorkflowNode';
import { NodeConfigPanel } from '../components/workflow/NodeConfigPanel';
import { EdgeConfigPanel } from '../components/workflow/EdgeConfigPanel';
import { StatusBadge } from '../components/workflow/StatusBadge';
import { Button } from '../components/ui/button';
import { Input } from '../components/ui/input';
import { Textarea } from '../components/ui/textarea';
import { Label } from '../components/ui/label';
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from '../components/ui/dialog';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '../components/ui/select';
import { Card, CardContent, CardHeader } from '../components/ui/card';
import { Save, ArrowLeft, Plus } from 'lucide-react';

interface NodeData {
  label: string;
  nodeKey: string;
  nodeType: NodeType;
  description?: string;
  actionHandler?: string;
  configuration?: string;
  timeoutSeconds?: number;
  maxRetries?: number;
  [key: string]: unknown;
}

interface EdgeData {
  name?: string;
  conditionExpression?: string;
  priority?: number;
  [key: string]: unknown;
}

const nodeTypes = { workflowNode: WorkflowNode };

const nodeTypeOptions: { value: NodeType; label: string }[] = [
  { value: 'START', label: 'Start' },
  { value: 'END', label: 'End' },
  { value: 'AUTO', label: 'Auto Execute' },
  { value: 'WAIT_FOR_INPUT', label: 'Wait for Input' },
  { value: 'FORK', label: 'Fork (Parallel)' },
  { value: 'JOIN', label: 'Join (Merge)' },
];

export function WorkflowEditorPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const isNew = !id;

  const [workflowName, setWorkflowName] = useState('');
  const [workflowDescription, setWorkflowDescription] = useState('');
  const [definition, setDefinition] = useState<WorkflowDefinitionResponse | null>(null);
  const [nodes, setNodes, onNodesChange] = useNodesState<Node<NodeData>>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge<EdgeData>>([]);
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [selectedEdgeId, setSelectedEdgeId] = useState<string | null>(null);
  const [showAddNode, setShowAddNode] = useState(false);
  const [newNodeType, setNewNodeType] = useState<NodeType>('AUTO');
  const [newNodeName, setNewNodeName] = useState('');
  const [newNodeKey, setNewNodeKey] = useState('');
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id) {
      workflowApi.getDefinition(parseInt(id)).then((res) => {
        const def = res.data;
        setDefinition(def);
        setWorkflowName(def.name);
        setWorkflowDescription(def.description || '');

        const loadedNodes: Node<NodeData>[] = def.nodes.map((n, i) => ({
          id: n.nodeKey,
          type: 'workflowNode',
          position: { x: 250, y: i * 150 },
          data: {
            label: n.name,
            nodeKey: n.nodeKey,
            nodeType: n.nodeType,
            description: n.description,
            actionHandler: n.actionHandler,
            configuration: n.configuration,
            timeoutSeconds: n.timeoutSeconds,
            maxRetries: n.maxRetries,
          },
        }));

        const loadedEdges: Edge<EdgeData>[] = def.transitions.map((t, i) => ({
          id: `e-${t.sourceNodeKey}-${t.targetNodeKey}-${i}`,
          source: t.sourceNodeKey,
          target: t.targetNodeKey,
          animated: !!t.conditionExpression,
          label: t.name || t.conditionExpression || '',
          data: {
            name: t.name,
            conditionExpression: t.conditionExpression,
            priority: t.priority,
          },
        }));

        setNodes(loadedNodes);
        setEdges(loadedEdges);
      }).catch((err) => {
        setError(err instanceof Error ? err.message : 'Failed to load definition');
      });
    }
  }, [id, setNodes, setEdges]);

  const onConnect = useCallback(
    (connection: Connection) => {
      setEdges((eds) =>
        addEdge(
          {
            ...connection,
            animated: false,
            data: { name: '', conditionExpression: '', priority: 0 },
          },
          eds
        )
      );
    },
    [setEdges]
  );

  const handleAddNode = useCallback(() => {
    if (!newNodeName || !newNodeKey) return;

    const newNode: Node<NodeData> = {
      id: newNodeKey,
      type: 'workflowNode',
      position: {
        x: Math.random() * 400 + 100,
        y: Math.random() * 400 + 100,
      },
      data: {
        label: newNodeName,
        nodeKey: newNodeKey,
        nodeType: newNodeType,
      },
    };

    setNodes((nds) => [...nds, newNode]);
    setShowAddNode(false);
    setNewNodeName('');
    setNewNodeKey('');
    setNewNodeType('AUTO');
  }, [newNodeName, newNodeKey, newNodeType, setNodes]);

  const handleNodeClick = useCallback((_: React.MouseEvent, node: Node) => {
    setSelectedNodeId(node.id);
    setSelectedEdgeId(null);
  }, []);

  const handleEdgeClick = useCallback((_: React.MouseEvent, edge: Edge) => {
    setSelectedEdgeId(edge.id);
    setSelectedNodeId(null);
  }, []);

  const handlePaneClick = useCallback(() => {
    setSelectedNodeId(null);
    setSelectedEdgeId(null);
  }, []);

  const selectedNode = useMemo(
    () => nodes.find((n) => n.id === selectedNodeId),
    [nodes, selectedNodeId]
  );

  const selectedEdge = useMemo(
    () => edges.find((e) => e.id === selectedEdgeId),
    [edges, selectedEdgeId]
  );

  const updateNodeData = useCallback(
    (updates: Partial<NodeData>) => {
      if (!selectedNodeId) return;
      setNodes((nds) =>
        nds.map((n) =>
          n.id === selectedNodeId
            ? { ...n, data: { ...n.data, ...updates } }
            : n
        )
      );
    },
    [selectedNodeId, setNodes]
  );

  const deleteNode = useCallback(() => {
    if (!selectedNodeId) return;
    setNodes((nds) => nds.filter((n) => n.id !== selectedNodeId));
    setEdges((eds) =>
      eds.filter((e) => e.source !== selectedNodeId && e.target !== selectedNodeId)
    );
    setSelectedNodeId(null);
  }, [selectedNodeId, setNodes, setEdges]);

  const updateEdgeData = useCallback(
    (updates: Partial<EdgeData>) => {
      if (!selectedEdgeId) return;
      setEdges((eds) =>
        eds.map((e) =>
          e.id === selectedEdgeId
            ? {
                ...e,
                data: { ...e.data, ...updates },
                animated: !!(updates.conditionExpression ?? e.data?.conditionExpression),
                label: updates.name ?? e.data?.name ?? updates.conditionExpression ?? e.data?.conditionExpression ?? '',
              }
            : e
        )
      );
    },
    [selectedEdgeId, setEdges]
  );

  const deleteEdge = useCallback(() => {
    if (!selectedEdgeId) return;
    setEdges((eds) => eds.filter((e) => e.id !== selectedEdgeId));
    setSelectedEdgeId(null);
  }, [selectedEdgeId, setEdges]);

  const handleSave = useCallback(async () => {
    if (!workflowName) {
      setError('Workflow name is required');
      return;
    }

    setSaving(true);
    setError(null);

    try {
      const request: CreateWorkflowDefinitionRequest = {
        name: workflowName,
        description: workflowDescription || undefined,
        nodes: nodes.map((n) => ({
          nodeKey: n.data.nodeKey,
          name: n.data.label,
          description: n.data.description,
          nodeType: n.data.nodeType,
          actionHandler: n.data.actionHandler,
          configuration: n.data.configuration,
          timeoutSeconds: n.data.timeoutSeconds,
          maxRetries: n.data.maxRetries,
        })),
        transitions: edges.map((e) => ({
          name: e.data?.name,
          sourceNodeKey: e.source,
          targetNodeKey: e.target,
          conditionExpression: e.data?.conditionExpression,
          priority: e.data?.priority,
        })),
      };

      const response = await workflowApi.createDefinition(request);
      navigate(`/definitions/${response.data.id}`);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to save workflow');
    } finally {
      setSaving(false);
    }
  }, [workflowName, workflowDescription, nodes, edges, navigate]);

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-4">
          <Button variant="ghost" size="icon" onClick={() => navigate('/definitions')}>
            <ArrowLeft className="h-4 w-4" />
          </Button>
          <div>
            <h1 className="text-xl font-bold text-gray-900">
              {isNew ? 'New Workflow' : workflowName}
            </h1>
            {definition && (
              <div className="flex items-center gap-2 mt-1">
                <span className="text-xs text-gray-500">v{definition.version}</span>
                <StatusBadge status={definition.status} />
              </div>
            )}
          </div>
        </div>
        <div className="flex items-center gap-2">
          <Button variant="outline" onClick={() => setShowAddNode(true)} className="gap-2">
            <Plus className="h-4 w-4" />
            Add Node
          </Button>
          {(isNew || definition?.status === 'DRAFT') && (
            <Button onClick={handleSave} disabled={saving} className="gap-2">
              <Save className="h-4 w-4" />
              {saving ? 'Saving...' : 'Save'}
            </Button>
          )}
        </div>
      </div>

      {error && (
        <Card className="border-red-200 bg-red-50">
          <CardContent className="py-3">
            <p className="text-sm text-red-600">{error}</p>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader className="py-3">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="wfName" className="text-xs">Workflow Name</Label>
              <Input
                id="wfName"
                value={workflowName}
                onChange={(e) => setWorkflowName(e.target.value)}
                className="mt-1 h-8 text-sm"
                placeholder="e.g. approval-workflow"
                disabled={!isNew && definition?.status !== 'DRAFT'}
              />
            </div>
            <div>
              <Label htmlFor="wfDesc" className="text-xs">Description</Label>
              <Textarea
                id="wfDesc"
                value={workflowDescription}
                onChange={(e) => setWorkflowDescription(e.target.value)}
                className="mt-1 text-sm"
                rows={1}
                placeholder="Describe this workflow..."
                disabled={!isNew && definition?.status !== 'DRAFT'}
              />
            </div>
          </div>
        </CardHeader>
      </Card>

      <div className="flex" style={{ height: 'calc(100vh - 320px)' }}>
        <Card className="flex-1 overflow-hidden">
          <CardContent className="p-0 h-full">
            <ReactFlow
              nodes={nodes}
              edges={edges}
              onNodesChange={onNodesChange}
              onEdgesChange={onEdgesChange}
              onConnect={onConnect}
              onNodeClick={handleNodeClick}
              onEdgeClick={handleEdgeClick}
              onPaneClick={handlePaneClick}
              nodeTypes={nodeTypes}
              fitView
              className="bg-gray-50"
            >
              <Background variant={BackgroundVariant.Dots} gap={20} size={1} />
              <Controls />
              <MiniMap
                nodeStrokeWidth={3}
                className="bg-white"
              />
              <Panel position="bottom-center">
                <div className="bg-white/90 backdrop-blur rounded-lg px-4 py-2 shadow-sm border text-xs text-gray-500">
                  Drag to reposition nodes &bull; Connect nodes by dragging from handles &bull; Click to configure
                </div>
              </Panel>
            </ReactFlow>
          </CardContent>
        </Card>

        {selectedNode && (
          <NodeConfigPanel
            nodeData={selectedNode.data}
            onUpdate={updateNodeData}
            onClose={() => setSelectedNodeId(null)}
            onDelete={deleteNode}
          />
        )}

        {selectedEdge && (
          <EdgeConfigPanel
            edgeData={selectedEdge.data || {}}
            sourceLabel={nodes.find((n) => n.id === selectedEdge.source)?.data.label || selectedEdge.source}
            targetLabel={nodes.find((n) => n.id === selectedEdge.target)?.data.label || selectedEdge.target}
            onUpdate={updateEdgeData}
            onClose={() => setSelectedEdgeId(null)}
            onDelete={deleteEdge}
          />
        )}
      </div>

      <Dialog open={showAddNode} onOpenChange={setShowAddNode}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Add Node</DialogTitle>
          </DialogHeader>
          <div className="space-y-4 py-4">
            <div>
              <Label htmlFor="addNodeKey">Node Key</Label>
              <Input
                id="addNodeKey"
                value={newNodeKey}
                onChange={(e) => setNewNodeKey(e.target.value)}
                className="mt-1"
                placeholder="e.g. review-step"
              />
            </div>
            <div>
              <Label htmlFor="addNodeName">Display Name</Label>
              <Input
                id="addNodeName"
                value={newNodeName}
                onChange={(e) => setNewNodeName(e.target.value)}
                className="mt-1"
                placeholder="e.g. Review Step"
              />
            </div>
            <div>
              <Label htmlFor="addNodeType">Node Type</Label>
              <Select value={newNodeType} onValueChange={(v) => setNewNodeType(v as NodeType)}>
                <SelectTrigger className="mt-1">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  {nodeTypeOptions.map((opt) => (
                    <SelectItem key={opt.value} value={opt.value}>
                      {opt.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setShowAddNode(false)}>
              Cancel
            </Button>
            <Button onClick={handleAddNode} disabled={!newNodeKey || !newNodeName}>
              Add Node
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
