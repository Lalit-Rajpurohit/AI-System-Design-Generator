import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { designApi } from '../services/api';
import DesignForm from '../components/DesignForm';

function HomePage() {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  const handleSubmit = async (formData) => {
    setLoading(true);
    setError(null);

    try {
      const response = await designApi.generate(formData);
      navigate(`/design/${response.id}`);
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
    <div>
      <div style={{ textAlign: 'center', marginBottom: '40px' }}>
        <h1>AI System Designer</h1>
        <p style={{ color: '#a0a0a0', fontSize: '1.1rem', marginTop: '10px' }}>
          Describe your application and get a complete system architecture design
        </p>
      </div>

      {error && (
        <div className="error-message">
          <strong>Error:</strong> {error}
        </div>
      )}

      <DesignForm onSubmit={handleSubmit} loading={loading} />

      {loading && (
        <div className="card" style={{ marginTop: '20px' }}>
          <div className="loading">
            <div className="spinner"></div>
            <p style={{ marginTop: '20px', color: '#a0a0a0' }}>
              Generating your system design...
            </p>
            <p style={{ marginTop: '10px', color: '#666', fontSize: '14px' }}>
              This may take 10-30 seconds depending on complexity
            </p>
          </div>
        </div>
      )}
    </div>
  );
}

export default HomePage;
