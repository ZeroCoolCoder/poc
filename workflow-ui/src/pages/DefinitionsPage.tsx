import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { workflowApi } from '../api/client';
import type { WorkflowDefinitionResponse } from '../types/workflow';
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
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuTrigger,
} from '../components/ui/dropdown-menu';
import { Plus, MoreVertical, Eye, CheckCircle, XCircle } from 'lucide-react';

export function DefinitionsPage() {
  const [definitions, setDefinitions] = useState<WorkflowDefinitionResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const navigate = useNavigate();

  const loadDefinitions = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await workflowApi.getDefinitions();
      setDefinitions(response.data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load definitions');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadDefinitions();
  }, [loadDefinitions]);

  const handleActivate = async (id: number) => {
    try {
      await workflowApi.activateDefinition(id);
      loadDefinitions();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to activate');
    }
  };

  const handleDeprecate = async (id: number) => {
    try {
      await workflowApi.deprecateDefinition(id);
      loadDefinitions();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to deprecate');
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Workflow Definitions</h1>
          <p className="text-sm text-gray-500 mt-1">
            Create and manage your workflow graph definitions
          </p>
        </div>
        <Button onClick={() => navigate('/definitions/new')} className="gap-2">
          <Plus className="h-4 w-4" />
          New Workflow
        </Button>
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
          <CardTitle>All Definitions</CardTitle>
          <CardDescription>
            {definitions.length} workflow definition{definitions.length !== 1 ? 's' : ''}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loading ? (
            <div className="text-center py-8 text-gray-500">Loading...</div>
          ) : definitions.length === 0 ? (
            <div className="text-center py-12">
              <p className="text-gray-500 mb-4">No workflow definitions yet</p>
              <Button variant="outline" onClick={() => navigate('/definitions/new')}>
                Create your first workflow
              </Button>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Name</TableHead>
                  <TableHead>Version</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Nodes</TableHead>
                  <TableHead>Created</TableHead>
                  <TableHead className="w-12"></TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {definitions.map((def) => (
                  <TableRow key={def.id} className="cursor-pointer hover:bg-gray-50">
                    <TableCell>
                      <Link
                        to={`/definitions/${def.id}`}
                        className="font-medium text-indigo-600 hover:text-indigo-800"
                      >
                        {def.name}
                      </Link>
                      {def.description && (
                        <p className="text-xs text-gray-400 mt-0.5">{def.description}</p>
                      )}
                    </TableCell>
                    <TableCell className="text-sm">v{def.version}</TableCell>
                    <TableCell>
                      <StatusBadge status={def.status} />
                    </TableCell>
                    <TableCell className="text-sm text-gray-600">
                      {def.nodes?.length || 0}
                    </TableCell>
                    <TableCell className="text-sm text-gray-500">
                      {new Date(def.createdAt).toLocaleDateString()}
                    </TableCell>
                    <TableCell>
                      <DropdownMenu>
                        <DropdownMenuTrigger asChild>
                          <Button variant="ghost" size="icon" className="h-8 w-8">
                            <MoreVertical className="h-4 w-4" />
                          </Button>
                        </DropdownMenuTrigger>
                        <DropdownMenuContent align="end">
                          <DropdownMenuItem onClick={() => navigate(`/definitions/${def.id}`)}>
                            <Eye className="h-4 w-4 mr-2" />
                            View / Edit
                          </DropdownMenuItem>
                          {def.status === 'DRAFT' && (
                            <DropdownMenuItem onClick={() => handleActivate(def.id)}>
                              <CheckCircle className="h-4 w-4 mr-2" />
                              Activate
                            </DropdownMenuItem>
                          )}
                          {def.status === 'ACTIVE' && (
                            <DropdownMenuItem onClick={() => handleDeprecate(def.id)}>
                              <XCircle className="h-4 w-4 mr-2" />
                              Deprecate
                            </DropdownMenuItem>
                          )}
                        </DropdownMenuContent>
                      </DropdownMenu>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  );
}
