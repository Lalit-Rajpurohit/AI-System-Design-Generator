import React, { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { designApi } from '../services/api';
import MermaidDiagram from '../components/MermaidDiagram';

function DesignPage() {
  const { id } = useParams();
  const [design, setDesign] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState('overview');

  useEffect(() => {
    const fetchDesign = async () => {
      try {
        setLoading(true);
        const data = await designApi.getById(id);
        setDesign(data);
      } catch (err) {
        console.error('Failed to fetch design:', err);
        setError(err.response?.data?.message || 'Failed to load design');
      } finally {
        setLoading(false);
      }
    };

    fetchDesign();
  }, [id]);

  if (loading) {
    return (
      <div className="card">
        <div className="loading">
          <div className="spinner"></div>
          <p style={{ marginTop: '20px' }}>Loading design...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="card">
        <div className="error-message">{error}</div>
        <Link to="/" className="btn btn-primary">Back to Home</Link>
      </div>
    );
  }

  if (!design) {
    return (
      <div className="card">
        <p>Design not found</p>
        <Link to="/" className="btn btn-primary">Back to Home</Link>
      </div>
    );
  }

  const tabs = [
    { id: 'overview', label: 'Overview' },
    { id: 'diagram', label: 'Diagram' },
    { id: 'services', label: 'Services' },
    { id: 'techstack', label: 'Tech Stack' },
    { id: 'terraform', label: 'Terraform' },
  ];

  return (
    <div>
      <div style={{ marginBottom: '30px' }}>
        <Link to="/" style={{ color: '#a0a0a0', fontSize: '14px' }}>&larr; Back to Generator</Link>
        <h1 style={{ marginTop: '10px' }}>{design.title}</h1>
        <div style={{ display: 'flex', gap: '12px', marginTop: '10px' }}>
          {design.infrastructure?.cloud && (
            <span className={`tag tag-${design.infrastructure.cloud.toLowerCase()}`}>
              {design.infrastructure.cloud}
            </span>
          )}
          <span style={{ color: '#a0a0a0', fontSize: '14px' }}>
            Generated: {new Date(design.generatedAt).toLocaleString()}
          </span>
          {design.tokensUsed && (
            <span style={{ color: '#666', fontSize: '14px' }}>
              Tokens: {design.tokensUsed}
            </span>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div style={{ display: 'flex', gap: '8px', marginBottom: '20px', borderBottom: '2px solid #2a4a6e', paddingBottom: '0' }}>
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            style={{
              padding: '12px 24px',
              background: activeTab === tab.id ? '#e94560' : 'transparent',
              border: 'none',
              borderRadius: '8px 8px 0 0',
              color: activeTab === tab.id ? '#fff' : '#a0a0a0',
              cursor: 'pointer',
              fontWeight: 600,
              marginBottom: '-2px',
            }}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="card">
          <h2 style={{ marginBottom: '20px' }}>Architecture Overview</h2>
          <div style={{ whiteSpace: 'pre-wrap', lineHeight: '1.8' }}>
            {design.architectureText || 'No architecture description available.'}
          </div>

          {design.costEstimate && (
            <div style={{ marginTop: '30px', padding: '20px', background: 'rgba(233, 69, 96, 0.1)', borderRadius: '8px' }}>
              <h3>Cost Estimate</h3>
              <p style={{ fontSize: '1.5rem', fontWeight: '600', color: '#e94560', marginTop: '10px' }}>
                {design.costEstimate.monthly} / month
              </p>
              {design.costEstimate.breakdown && (
                <p style={{ marginTop: '10px', color: '#a0a0a0' }}>
                  {design.costEstimate.breakdown}
                </p>
              )}
            </div>
          )}
        </div>
      )}

      {activeTab === 'diagram' && (
        <MermaidDiagram
          diagram={design.infrastructure?.diagramMermaid}
          title={design.title}
        />
      )}

      {activeTab === 'services' && (
        <div className="card">
          <h2 style={{ marginBottom: '20px' }}>Services</h2>
          {design.services && design.services.length > 0 ? (
            <div className="grid">
              {design.services.map((service, index) => (
                <div
                  key={index}
                  style={{
                    padding: '20px',
                    background: 'rgba(255, 255, 255, 0.05)',
                    borderRadius: '8px',
                    border: '1px solid #2a4a6e',
                  }}
                >
                  <h4 style={{ color: '#e94560' }}>{service.name}</h4>
                  <p style={{ marginTop: '8px', color: '#a0a0a0' }}>{service.description}</p>
                  {service.technology && (
                    <p style={{ marginTop: '8px', fontSize: '14px' }}>
                      <strong>Tech:</strong> {service.technology}
                    </p>
                  )}
                  {service.dependencies && service.dependencies.length > 0 && (
                    <p style={{ marginTop: '8px', fontSize: '14px' }}>
                      <strong>Depends on:</strong> {service.dependencies.join(', ')}
                    </p>
                  )}
                </div>
              ))}
            </div>
          ) : (
            <p style={{ color: '#a0a0a0' }}>No services defined</p>
          )}
        </div>
      )}

      {activeTab === 'techstack' && (
        <div className="card">
          <h2 style={{ marginBottom: '20px' }}>Technology Stack</h2>
          {design.techStack ? (
            <div className="grid" style={{ gridTemplateColumns: 'repeat(auto-fill, minmax(200px, 1fr))' }}>
              {Object.entries(design.techStack)
                .filter(([_, value]) => value && value !== 'null')
                .map(([key, value]) => (
                  <div
                    key={key}
                    style={{
                      padding: '16px',
                      background: 'rgba(255, 255, 255, 0.05)',
                      borderRadius: '8px',
                      textAlign: 'center',
                    }}
                  >
                    <p style={{ color: '#a0a0a0', fontSize: '12px', textTransform: 'uppercase' }}>
                      {key}
                    </p>
                    <p style={{ fontSize: '1.1rem', fontWeight: '600', marginTop: '8px' }}>
                      {typeof value === 'object' ? JSON.stringify(value) : value}
                    </p>
                  </div>
                ))}
            </div>
          ) : (
            <p style={{ color: '#a0a0a0' }}>No tech stack defined</p>
          )}

          {design.infrastructure?.components && (
            <div style={{ marginTop: '30px' }}>
              <h3>Infrastructure Components</h3>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '8px', marginTop: '16px' }}>
                {design.infrastructure.components.map((comp, i) => (
                  <span
                    key={i}
                    style={{
                      padding: '8px 16px',
                      background: '#2a4a6e',
                      borderRadius: '20px',
                      fontSize: '14px',
                    }}
                  >
                    {comp}
                  </span>
                ))}
              </div>
            </div>
          )}
        </div>
      )}

      {activeTab === 'terraform' && (
        <div className="card">
          <h2 style={{ marginBottom: '20px' }}>Terraform Snippet</h2>
          {design.terraformSnippet ? (
            <>
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '10px' }}>
                <button
                  className="btn btn-secondary"
                  style={{ padding: '8px 16px', fontSize: '14px' }}
                  onClick={() => {
                    navigator.clipboard.writeText(design.terraformSnippet)
                      .then(() => alert('Copied to clipboard'))
                      .catch(() => alert('Failed to copy'));
                  }}
                >
                  Copy Code
                </button>
              </div>
              <pre style={{ maxHeight: '600px', overflow: 'auto' }}>
                <code>{design.terraformSnippet}</code>
              </pre>
            </>
          ) : (
            <p style={{ color: '#a0a0a0' }}>No Terraform snippet available</p>
          )}
        </div>
      )}
    </div>
  );
}

export default DesignPage;
