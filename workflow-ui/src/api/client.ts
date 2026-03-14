import type {
  ApiResponse,
  CreateWorkflowDefinitionRequest,
  NodeExecutionResponse,
  StartWorkflowRequest,
  SubmitActionRequest,
  WorkflowDefinitionResponse,
  WorkflowInstanceResponse,
  WorkflowStatus,
} from '../types/workflow';

const API_BASE = import.meta.env.VITE_API_URL || 'http://localhost:8080';

async function request<T>(path: string, options?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...options?.headers,
    },
    ...options,
  });

  if (!response.ok) {
    const error = await response.json().catch(() => ({ message: response.statusText }));
    throw new Error(error.message || `Request failed: ${response.status}`);
  }

  return response.json();
}

export const workflowApi = {
  // Definitions
  getDefinitions: () =>
    request<ApiResponse<WorkflowDefinitionResponse[]>>('/api/v1/workflow-definitions'),

  getDefinition: (id: number) =>
    request<ApiResponse<WorkflowDefinitionResponse>>(`/api/v1/workflow-definitions/${id}`),

  getActiveDefinitions: () =>
    request<ApiResponse<WorkflowDefinitionResponse[]>>('/api/v1/workflow-definitions/active'),

  createDefinition: (data: CreateWorkflowDefinitionRequest) =>
    request<ApiResponse<WorkflowDefinitionResponse>>('/api/v1/workflow-definitions', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  activateDefinition: (id: number) =>
    request<ApiResponse<WorkflowDefinitionResponse>>(`/api/v1/workflow-definitions/${id}/activate`, {
      method: 'PUT',
    }),

  deprecateDefinition: (id: number) =>
    request<ApiResponse<WorkflowDefinitionResponse>>(`/api/v1/workflow-definitions/${id}/deprecate`, {
      method: 'PUT',
    }),

  // Instances
  startWorkflow: (data: StartWorkflowRequest) =>
    request<ApiResponse<WorkflowInstanceResponse>>('/api/v1/workflow-instances/start', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  submitAction: (data: SubmitActionRequest) =>
    request<ApiResponse<WorkflowInstanceResponse>>('/api/v1/workflow-instances/action', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getInstance: (id: number) =>
    request<ApiResponse<WorkflowInstanceResponse>>(`/api/v1/workflow-instances/${id}`),

  getInstancesByDefinition: (definitionId: number) =>
    request<ApiResponse<WorkflowInstanceResponse[]>>(`/api/v1/workflow-instances/by-definition/${definitionId}`),

  getInstancesByStatus: (status: WorkflowStatus) =>
    request<ApiResponse<WorkflowInstanceResponse[]>>(`/api/v1/workflow-instances/by-status?status=${status}`),

  getNodeExecutions: (instanceId: number) =>
    request<ApiResponse<NodeExecutionResponse[]>>(`/api/v1/workflow-instances/${instanceId}/executions`),

  cancelWorkflow: (id: number) =>
    request<ApiResponse<WorkflowInstanceResponse>>(`/api/v1/workflow-instances/${id}/cancel`, {
      method: 'PUT',
    }),

  suspendWorkflow: (id: number) =>
    request<ApiResponse<WorkflowInstanceResponse>>(`/api/v1/workflow-instances/${id}/suspend`, {
      method: 'PUT',
    }),

  resumeWorkflow: (id: number) =>
    request<ApiResponse<WorkflowInstanceResponse>>(`/api/v1/workflow-instances/${id}/resume`, {
      method: 'PUT',
    }),
};
