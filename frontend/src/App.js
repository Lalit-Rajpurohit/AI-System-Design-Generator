import React, { useState } from 'react';
import { Zap, Boxes, AlertTriangle } from 'lucide-react';
import DesignForm from './components/DesignForm';
import DesignResult from './components/DesignResult';
import { designApi } from './services/api';
import './index.css';

function App() {
  const [design, setDesign] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (formData) => {
    setLoading(true);
    setError(null);
    setDesign(null);

    try {
      const response = await designApi.generate(formData);
      setDesign(response);
    } catch (err) {
      console.error('Generation failed:', err);
      setError(
        err.response?.data?.message ||
        err.response?.data?.error ||
        'Failed to generate design. Please try again.'
      );
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="app-layout">
      {/* Left Sidebar */}
      <aside className="sidebar">
        <div className="sidebar-header">
          <div className="logo">
            <div className="logo-icon">
              <Zap />
            </div>
            <div>
              <div className="logo-text">AI Architect</div>
              <div className="logo-subtitle">System Design Generator</div>
            </div>
          </div>
        </div>
        <div className="sidebar-content">
          <DesignForm onSubmit={handleSubmit} loading={loading} />
        </div>
      </aside>

      {/* Main Content */}
      <main className="main-content">
        {error && (
          <div className="result-panel">
            <div className="alert alert-error fade-in">
              <span className="alert-icon">
                <AlertTriangle />
              </span>
              <div>
                <strong>Error:</strong> {error}
              </div>
            </div>
          </div>
        )}

        {loading && (
          <div className="loading-container">
            <div className="spinner"></div>
            <div className="loading-text">Generating your architecture design...</div>
            <div className="loading-subtext">
              Our AI is analyzing your requirements and creating a comprehensive system design
            </div>
          </div>
        )}

        {!loading && !design && !error && (
          <div className="empty-state">
            <div className="empty-icon">
              <Boxes />
            </div>
            <h2 className="empty-title">Ready to Design</h2>
            <p className="empty-text">
              Describe your application on the left panel and click Generate to create your system architecture.
            </p>
          </div>
        )}

        {design && !loading && (
          <DesignResult design={design} />
        )}
      </main>
    </div>
  );
}

export default App;
