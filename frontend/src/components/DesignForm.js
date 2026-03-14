import React, { useState } from 'react';
import {
  FileText,
  Tag,
  AlignLeft,
  Cloud,
  Layers,
  Building2,
  Zap,
  Settings,
  Users,
  RefreshCw,
  Package,
  DollarSign,
  Sparkles
} from 'lucide-react';
import { AwsIcon, GcpIcon, AzureIcon, MultiCloudIcon } from './CloudIcons';

const cloudProviders = [
  { value: 'AWS', label: 'Amazon AWS', Icon: AwsIcon, iconClass: 'aws' },
  { value: 'GCP', label: 'Google Cloud', Icon: GcpIcon, iconClass: 'gcp' },
  { value: 'AZURE', label: 'Microsoft Azure', Icon: AzureIcon, iconClass: 'azure' },
  { value: 'MULTI_CLOUD', label: 'Multi-Cloud', Icon: MultiCloudIcon, iconClass: 'multi' },
];

const architectureStyles = [
  { value: 'MICROSERVICES', label: 'Microservices', iconClass: 'microservices', Icon: Layers },
  { value: 'MONOLITH', label: 'Monolith', iconClass: 'monolith', Icon: Building2 },
  { value: 'SERVERLESS', label: 'Serverless', iconClass: 'serverless', Icon: Zap },
];

function DesignForm({ onSubmit, loading }) {
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    preferences: {
      cloudProvider: 'AWS',
      architectureStyle: 'MICROSERVICES',
      expectedMonthlyActiveUsers: 100000,
      dataConsistency: 'EVENTUAL',
      includeTerraform: true,
      diagramFormat: 'MERMAID',
      includeCostEstimate: true,
    },
  });

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;

    if (name.startsWith('preferences.')) {
      const prefKey = name.replace('preferences.', '');
      setFormData((prev) => ({
        ...prev,
        preferences: {
          ...prev.preferences,
          [prefKey]: type === 'checkbox' ? checked : value,
        },
      }));
    } else {
      setFormData((prev) => ({
        ...prev,
        [name]: value,
      }));
    }
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    onSubmit(formData);
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Project Info */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <div className="card-header">
          <div className="card-icon purple">
            <FileText />
          </div>
          <div>
            <div className="card-title">Project Details</div>
            <div className="card-subtitle">Describe your application</div>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">
            <Tag size={16} />
            Project Title
          </label>
          <input
            type="text"
            className="form-input"
            name="title"
            value={formData.title}
            onChange={handleChange}
            placeholder="e.g., E-commerce Platform"
            required
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label className="form-label">
            <AlignLeft size={16} />
            Description
          </label>
          <textarea
            className="form-textarea"
            name="description"
            value={formData.description}
            onChange={handleChange}
            placeholder="Describe your application requirements, features, expected users, and any specific technologies..."
            required
            disabled={loading}
            rows={5}
          />
        </div>
      </div>

      {/* Cloud Provider */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <div className="card-header">
          <div className="card-icon blue">
            <Cloud />
          </div>
          <div>
            <div className="card-title">Cloud Provider</div>
            <div className="card-subtitle">Select your preferred cloud</div>
          </div>
        </div>

        <div className="option-grid">
          {cloudProviders.map((provider) => (
            <label key={provider.value} className="option-item">
              <input
                type="radio"
                name="preferences.cloudProvider"
                value={provider.value}
                checked={formData.preferences.cloudProvider === provider.value}
                onChange={handleChange}
                disabled={loading}
              />
              <div className="option-content">
                <div className={`option-icon ${provider.iconClass}`}>
                  <provider.Icon size={22} />
                </div>
                <span className="option-label">{provider.label}</span>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Architecture Style */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <div className="card-header">
          <div className="card-icon green">
            <Layers />
          </div>
          <div>
            <div className="card-title">Architecture Style</div>
            <div className="card-subtitle">Choose your architecture pattern</div>
          </div>
        </div>

        <div className="option-list">
          {architectureStyles.map((style) => (
            <label key={style.value} className="option-item">
              <input
                type="radio"
                name="preferences.architectureStyle"
                value={style.value}
                checked={formData.preferences.architectureStyle === style.value}
                onChange={handleChange}
                disabled={loading}
              />
              <div className="option-content">
                <div className={`option-icon ${style.iconClass}`}>
                  <style.Icon size={22} />
                </div>
                <span className="option-label">{style.label}</span>
              </div>
            </label>
          ))}
        </div>
      </div>

      {/* Scale */}
      <div className="card" style={{ marginBottom: '20px' }}>
        <div className="card-header">
          <div className="card-icon orange">
            <Settings />
          </div>
          <div>
            <div className="card-title">Scale & Options</div>
            <div className="card-subtitle">Configure additional settings</div>
          </div>
        </div>

        <div className="form-group">
          <label className="form-label">
            <Users size={16} />
            Expected Monthly Active Users
          </label>
          <input
            type="number"
            className="form-input"
            name="preferences.expectedMonthlyActiveUsers"
            value={formData.preferences.expectedMonthlyActiveUsers}
            onChange={handleChange}
            min="1000"
            step="10000"
            disabled={loading}
          />
        </div>

        <div className="form-group">
          <label className="form-label">
            <RefreshCw size={16} />
            Data Consistency
          </label>
          <select
            className="form-select"
            name="preferences.dataConsistency"
            value={formData.preferences.dataConsistency}
            onChange={handleChange}
            disabled={loading}
          >
            <option value="EVENTUAL">Eventual Consistency</option>
            <option value="STRONG">Strong Consistency</option>
          </select>
        </div>

        <div className="checkbox-row">
          <label className="checkbox-item">
            <input
              type="checkbox"
              name="preferences.includeTerraform"
              checked={formData.preferences.includeTerraform}
              onChange={handleChange}
              disabled={loading}
            />
            <div className="checkbox-content">
              <Package size={20} />
              <span>Terraform</span>
            </div>
          </label>

          <label className="checkbox-item">
            <input
              type="checkbox"
              name="preferences.includeCostEstimate"
              checked={formData.preferences.includeCostEstimate}
              onChange={handleChange}
              disabled={loading}
            />
            <div className="checkbox-content">
              <DollarSign size={20} />
              <span>Cost Estimate</span>
            </div>
          </label>
        </div>
      </div>

      {/* Submit */}
      <button
        type="submit"
        className="btn btn-primary"
        disabled={loading}
        style={{ width: '100%', padding: '18px' }}
      >
        {loading ? (
          <>
            <span className="spinner" style={{ width: '20px', height: '20px', borderWidth: '2px' }}></span>
            Generating...
          </>
        ) : (
          <>
            <Sparkles size={20} />
            Generate Architecture
          </>
        )}
      </button>
    </form>
  );
}

export default DesignForm;
