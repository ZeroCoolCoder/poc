import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/workflow/Layout';
import { DefinitionsPage } from './pages/DefinitionsPage';
import { WorkflowEditorPage } from './pages/WorkflowEditorPage';
import { InstancesPage } from './pages/InstancesPage';
import { InstanceDetailPage } from './pages/InstanceDetailPage';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route element={<Layout />}>
          <Route path="/" element={<Navigate to="/definitions" replace />} />
          <Route path="/definitions" element={<DefinitionsPage />} />
          <Route path="/definitions/new" element={<WorkflowEditorPage />} />
          <Route path="/definitions/:id" element={<WorkflowEditorPage />} />
          <Route path="/instances" element={<InstancesPage />} />
          <Route path="/instances/:id" element={<InstanceDetailPage />} />
        </Route>
      </Routes>
    </BrowserRouter>
  );
}

export default App;
