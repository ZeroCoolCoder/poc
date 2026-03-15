export type NodeType = 'START' | 'END' | 'AUTO' | 'WAIT_FOR_INPUT' | 'FORK' | 'JOIN';
export type DefinitionStatus = 'DRAFT' | 'ACTIVE' | 'DEPRECATED';
export type WorkflowStatus = 'RUNNING' | 'COMPLETED' | 'FAILED' | 'SUSPENDED' | 'CANCELLED';
export type NodeExecutionStatus = 'PENDING' | 'RUNNING' | 'COMPLETED' | 'FAILED' | 'WAITING_FOR_INPUT' | 'SKIPPED';

export interface NodeDefinitionResponse {
  id: number;
  nodeKey: string;
  name: string;
  description?: string;
  nodeType: NodeType;
  actionHandler?: string;
  configuration?: string;
  timeoutSeconds?: number;
  maxRetries?: number;
}

export interface TransitionDefinitionResponse {
  id: number;
  name?: string;
  sourceNodeKey: string;
  targetNodeKey: string;
  conditionExpression?: string;
  priority?: number;
}

export interface WorkflowDefinitionResponse {
  id: number;
  name: string;
  description?: string;
  version: number;
  status: DefinitionStatus;
  nodes: NodeDefinitionResponse[];
  transitions: TransitionDefinitionResponse[];
  createdAt: string;
  updatedAt?: string;
}

export interface WorkflowInstanceResponse {
  id: number;
  workflowDefinitionId: number;
  correlationId?: string;
  status: WorkflowStatus;
  currentNodeKey?: string;
  contextData?: string;
  createdAt: string;
  updatedAt?: string;
  completedAt?: string;
  createdBy?: string;
}

export interface NodeExecutionResponse {
  id: number;
  workflowInstanceId: number;
  nodeDefinitionId: number;
  nodeKey: string;
  status: NodeExecutionStatus;
  inputData?: string;
  outputData?: string;
  errorMessage?: string;
  retryCount?: number;
  startedAt?: string;
  completedAt?: string;
}

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface NodeDefinitionRequest {
  nodeKey: string;
  name: string;
  description?: string;
  nodeType: NodeType;
  actionHandler?: string;
  configuration?: string;
  timeoutSeconds?: number;
  maxRetries?: number;
}

export interface TransitionDefinitionRequest {
  name?: string;
  sourceNodeKey: string;
  targetNodeKey: string;
  conditionExpression?: string;
  priority?: number;
}

export interface CreateWorkflowDefinitionRequest {
  name: string;
  description?: string;
  nodes: NodeDefinitionRequest[];
  transitions: TransitionDefinitionRequest[];
}

export interface StartWorkflowRequest {
  workflowDefinitionId: number;
  correlationId?: string;
  initialContext?: Record<string, unknown>;
  createdBy?: string;
}

export interface SubmitActionRequest {
  workflowInstanceId: number;
  nodeKey?: string;
  action?: string;
  payload?: Record<string, unknown>;
  submittedBy?: string;
}
