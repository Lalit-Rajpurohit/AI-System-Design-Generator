import React, { useState } from 'react';
import {
  FileText,
  BarChart3,
  Puzzle,
  Wrench,
  Package,
  Clock,
  Target,
  Bot,
  DollarSign,
  Building,
  TrendingUp,
  Shield,
  Server,
  Database,
  Lock,
  Mail,
  CreditCard,
  Search,
  ShoppingCart,
  Box,
  Cpu,
  HardDrive,
  MessageSquare,
  BarChart,
  FolderOpen,
  Network,
  Layout,
  Code,
  Gauge,
  Copy
} from 'lucide-react';
import MermaidDiagram from './MermaidDiagram';

function DesignResult({ design }) {
  const [activeTab, setActiveTab] = useState('overview');

  const tabs = [
    { id: 'overview', label: 'Overview', Icon: FileText },
    { id: 'diagram', label: 'Diagram', Icon: BarChart3 },
    { id: 'services', label: 'Services', Icon: Puzzle },
    { id: 'techstack', label: 'Tech Stack', Icon: Wrench },
    { id: 'terraform', label: 'Terraform', Icon: Package },
  ];

  const formatDate = (dateString) => {
    if (!dateString) return '';
    return new Date(dateString).toLocaleString();
  };

  return (
    <div className="result-panel fade-in">
      {/* Header */}
      <div className="result-header">
        <h1 className="result-title">{design.title}</h1>
        <div className="result-meta">
          {design.infrastructure?.cloud && (
            <span className={`tag tag-${design.infrastructure.cloud.toLowerCase()}`}>
              {design.infrastructure.cloud}
            </span>
          )}
          <span className={`tag tag-${design.status?.toLowerCase() || 'completed'}`}>
            {design.status || 'COMPLETED'}
          </span>
          <span className="result-meta-item">
            <Clock size={16} />
            {formatDate(design.generatedAt)}
          </span>
          {design.tokensUsed && (
            <span className="result-meta-item">
              <Target size={16} />
              {design.tokensUsed} tokens
            </span>
          )}
          {design.llmProvider && (
            <span className="result-meta-item">
              <Bot size={16} />
              {design.llmProvider}
            </span>
          )}
        </div>
      </div>

      {/* Tabs */}
      <div className="tabs">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            className={`tab ${activeTab === tab.id ? 'active' : ''}`}
            onClick={() => setActiveTab(tab.id)}
          >
            <tab.Icon /> {tab.label}
          </button>
        ))}
      </div>

      {/* Tab Content */}
      {activeTab === 'overview' && (
        <div className="fade-in">
          {/* Architecture Overview */}
          <div className="section">
            <div className="section-header">
              <div className="section-icon card-icon purple">
                <FileText />
              </div>
              <h2 className="section-title">Architecture Overview</h2>
            </div>
            <div className="card">
              <div style={{ whiteSpace: 'pre-wrap', lineHeight: '1.8', color: 'var(--text-secondary)' }}>
                {design.architectureText || 'No architecture description available.'}
              </div>
            </div>
          </div>

          {/* Cost Estimate */}
          {design.costEstimate && (
            <div className="section">
              <div className="section-header">
                <div className="section-icon card-icon green">
                  <DollarSign />
                </div>
                <h2 className="section-title">Cost Estimate</h2>
              </div>
              <div className="cost-card">
                <div className="cost-amount">{design.costEstimate.monthly}</div>
                <div className="cost-period">per month</div>
                {design.costEstimate.breakdown && (
                  <p style={{ marginTop: '16px', color: 'var(--text-secondary)', fontSize: '14px' }}>
                    {design.costEstimate.breakdown}
                  </p>
                )}
                {design.costEstimate.assumptions && (
                  <p style={{ marginTop: '8px', color: 'var(--text-muted)', fontSize: '13px' }}>
                    <em>Assumptions: {design.costEstimate.assumptions}</em>
                  </p>
                )}
              </div>
            </div>
          )}

          {/* Infrastructure Components */}
          {design.infrastructure?.components && design.infrastructure.components.length > 0 && (
            <div className="section">
              <div className="section-header">
                <div className="section-icon card-icon blue">
                  <Building />
                </div>
                <h2 className="section-title">Infrastructure Components</h2>
              </div>
              <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
                {design.infrastructure.components.map((comp, i) => (
                  <span
                    key={i}
                    style={{
                      padding: '10px 18px',
                      background: 'var(--bg-tertiary)',
                      border: '1px solid var(--border-primary)',
                      borderRadius: '100px',
                      fontSize: '0.875rem',
                      color: 'var(--text-primary)',
                      fontWeight: 500,
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

      {activeTab === 'diagram' && (
        <div className="fade-in">
          <div className="section">
            <div className="section-header">
              <div className="section-icon card-icon cyan">
                <BarChart3 />
              </div>
              <h2 className="section-title">Architecture Diagram</h2>
            </div>
            <MermaidDiagram
              diagram={design.infrastructure?.diagramMermaid}
              title={design.title}
            />
          </div>
        </div>
      )}

      {activeTab === 'services' && (
        <div className="fade-in">
          <div className="section">
            <div className="section-header">
              <div className="section-icon card-icon purple">
                <Puzzle />
              </div>
              <h2 className="section-title">Services ({design.services?.length || 0})</h2>
            </div>
            {design.services && design.services.length > 0 ? (
              <div className="services-grid">
                {design.services.map((service, index) => (
                  <div key={index} className="service-card">
                    <div className="service-header">
                      <div className="service-icon">
                        {getServiceIcon(service.name)}
                      </div>
                      <div>
                        <div className="service-name">{service.name}</div>
                        {service.technology && (
                          <div className="service-tech">{service.technology}</div>
                        )}
                      </div>
                    </div>
                    <p className="service-description">{service.description}</p>
                    {service.dependencies && service.dependencies.length > 0 && (
                      <div className="service-deps">
                        <strong>Dependencies:</strong> {service.dependencies.join(', ')}
                      </div>
                    )}
                  </div>
                ))}
              </div>
            ) : (
              <div className="card" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                No services defined
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'techstack' && (
        <div className="fade-in">
          <div className="section">
            <div className="section-header">
              <div className="section-icon card-icon orange">
                <Wrench />
              </div>
              <h2 className="section-title">Technology Stack</h2>
            </div>
            {design.techStack ? (
              <div className="tech-grid">
                {Object.entries(design.techStack)
                  .filter(([key, value]) => value && value !== 'null' && key !== 'additional')
                  .map(([key, value]) => (
                    <div key={key} className="tech-item">
                      <div className="tech-label">{formatKey(key)}</div>
                      <div className="tech-value">
                        {getTechIcon(key)} {typeof value === 'object' ? JSON.stringify(value) : value}
                      </div>
                    </div>
                  ))}
              </div>
            ) : (
              <div className="card" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                No tech stack defined
              </div>
            )}

            {/* Scaling Strategy */}
            {design.infrastructure?.scalingStrategy && (
              <div style={{ marginTop: '36px' }}>
                <div className="section-header">
                  <div className="section-icon card-icon green">
                    <TrendingUp />
                  </div>
                  <h2 className="section-title">Scaling Strategy</h2>
                </div>
                <div className="card">
                  <p style={{ marginBottom: '12px' }}>
                    <strong style={{ color: 'var(--accent-primary)', fontSize: '1.1rem' }}>
                      {design.infrastructure.scalingStrategy.type}
                    </strong>
                  </p>
                  <p style={{ color: 'var(--text-secondary)', lineHeight: '1.7' }}>
                    {design.infrastructure.scalingStrategy.description}
                  </p>
                  {design.infrastructure.scalingStrategy.keyMetrics && (
                    <div style={{ marginTop: '18px' }}>
                      <strong style={{ fontSize: '0.82rem', color: 'var(--text-muted)', textTransform: 'uppercase', letterSpacing: '0.05em' }}>Key Metrics</strong>
                      <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px', marginTop: '10px' }}>
                        {design.infrastructure.scalingStrategy.keyMetrics.map((metric, i) => (
                          <span
                            key={i}
                            style={{
                              padding: '6px 14px',
                              background: 'rgba(139, 92, 246, 0.1)',
                              border: '1px solid rgba(139, 92, 246, 0.2)',
                              borderRadius: '100px',
                              fontSize: '0.82rem',
                              color: 'var(--accent-secondary)',
                              fontWeight: 500,
                            }}
                          >
                            {metric}
                          </span>
                        ))}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            )}

            {/* Security Recommendations */}
            {design.infrastructure?.securityRecommendations && design.infrastructure.securityRecommendations.length > 0 && (
              <div style={{ marginTop: '36px' }}>
                <div className="section-header">
                  <div className="section-icon card-icon pink">
                    <Shield />
                  </div>
                  <h2 className="section-title">Security Recommendations</h2>
                </div>
                <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                  {design.infrastructure.securityRecommendations.map((rec, i) => (
                    <div
                      key={i}
                      className="card"
                      style={{ padding: '20px' }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginBottom: '12px' }}>
                        <span
                          className="tag"
                          style={{
                            background: rec.priority === 'HIGH' || rec.priority === 'CRITICAL'
                              ? 'rgba(239, 68, 68, 0.15)'
                              : rec.priority === 'MEDIUM'
                              ? 'rgba(234, 179, 8, 0.15)'
                              : 'rgba(34, 197, 94, 0.15)',
                            color: rec.priority === 'HIGH' || rec.priority === 'CRITICAL'
                              ? '#f87171'
                              : rec.priority === 'MEDIUM'
                              ? '#facc15'
                              : '#4ade80',
                            border: `1px solid ${rec.priority === 'HIGH' || rec.priority === 'CRITICAL'
                              ? 'rgba(239, 68, 68, 0.25)'
                              : rec.priority === 'MEDIUM'
                              ? 'rgba(234, 179, 8, 0.25)'
                              : 'rgba(34, 197, 94, 0.25)'}`,
                          }}
                        >
                          {rec.priority}
                        </span>
                        <strong style={{ color: 'var(--text-primary)' }}>{rec.category}</strong>
                      </div>
                      <p style={{ color: 'var(--text-secondary)', fontSize: '0.95rem', lineHeight: '1.65' }}>
                        {rec.recommendation}
                      </p>
                    </div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      )}

      {activeTab === 'terraform' && (
        <div className="fade-in">
          <div className="section">
            <div className="section-header">
              <div className="section-icon card-icon blue">
                <Package />
              </div>
              <h2 className="section-title">Terraform Configuration</h2>
            </div>
            {design.terraformSnippet ? (
              <div className="code-block">
                <div className="code-header">
                  <span className="code-title">main.tf</span>
                  <button
                    className="btn btn-ghost"
                    onClick={() => {
                      navigator.clipboard.writeText(design.terraformSnippet);
                      alert('Copied to clipboard!');
                    }}
                  >
                    <Copy size={16} /> Copy
                  </button>
                </div>
                <pre className="code-content">
                  <code>{design.terraformSnippet}</code>
                </pre>
              </div>
            ) : (
              <div className="card" style={{ textAlign: 'center', color: 'var(--text-muted)' }}>
                No Terraform snippet available. Enable "Include Terraform" option when generating.
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}

// Helper functions
function getServiceIcon(name) {
  const nameLower = name.toLowerCase();
  if (nameLower.includes('gateway') || nameLower.includes('api')) return <Network />;
  if (nameLower.includes('auth') || nameLower.includes('user')) return <Lock />;
  if (nameLower.includes('database') || nameLower.includes('db')) return <Database />;
  if (nameLower.includes('cache')) return <HardDrive />;
  if (nameLower.includes('queue') || nameLower.includes('message')) return <MessageSquare />;
  if (nameLower.includes('search')) return <Search />;
  if (nameLower.includes('notification') || nameLower.includes('email')) return <Mail />;
  if (nameLower.includes('payment')) return <CreditCard />;
  if (nameLower.includes('analytics')) return <BarChart />;
  if (nameLower.includes('storage') || nameLower.includes('file')) return <FolderOpen />;
  if (nameLower.includes('order')) return <ShoppingCart />;
  if (nameLower.includes('product') || nameLower.includes('catalog')) return <Box />;
  if (nameLower.includes('recommendation')) return <Target />;
  return <Cpu />;
}

function getTechIcon(key) {
  const icons = {
    frontend: <Layout size={18} />,
    backend: <Server size={18} />,
    database: <Database size={18} />,
    cache: <HardDrive size={18} />,
    messaging: <MessageSquare size={18} />,
    search: <Search size={18} />,
    monitoring: <Gauge size={18} />,
  };
  return icons[key] || <Code size={18} />;
}

function formatKey(key) {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/^./, (str) => str.toUpperCase())
    .trim();
}

export default DesignResult;
