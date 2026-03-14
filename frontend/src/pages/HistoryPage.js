import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { designApi } from '../services/api';

function HistoryPage() {
  const [designs, setDesigns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  useEffect(() => {
    const fetchDesigns = async () => {
      try {
        setLoading(true);
        const data = await designApi.list({ page, size: 12 });
        setDesigns(data.designs || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        console.error('Failed to fetch designs:', err);
        setError(err.response?.data?.message || 'Failed to load designs');
      } finally {
        setLoading(false);
      }
    };

    fetchDesigns();
  }, [page]);

  if (loading && designs.length === 0) {
    return (
      <div className="card">
        <div className="loading">
          <div className="spinner"></div>
          <p style={{ marginTop: '20px' }}>Loading designs...</p>
        </div>
      </div>
    );
  }

  return (
    <div>
      <h1 style={{ marginBottom: '30px' }}>Design History</h1>

      {error && (
        <div className="error-message">{error}</div>
      )}

      {designs.length === 0 ? (
        <div className="card" style={{ textAlign: 'center' }}>
          <p style={{ color: '#a0a0a0', marginBottom: '20px' }}>
            No designs generated yet. Create your first design!
          </p>
          <Link to="/" className="btn btn-primary">
            Generate Design
          </Link>
        </div>
      ) : (
        <>
          <div className="grid">
            {designs.map((design) => (
              <Link
                key={design.id}
                to={`/design/${design.id}`}
                style={{ textDecoration: 'none', color: 'inherit' }}
              >
                <div
                  className="card"
                  style={{
                    cursor: 'pointer',
                    transition: 'transform 0.2s, border-color 0.2s',
                    height: '100%',
                  }}
                  onMouseOver={(e) => {
                    e.currentTarget.style.transform = 'translateY(-4px)';
                    e.currentTarget.style.borderColor = '#e94560';
                  }}
                  onMouseOut={(e) => {
                    e.currentTarget.style.transform = 'translateY(0)';
                    e.currentTarget.style.borderColor = '#2a4a6e';
                  }}
                >
                  <h3 style={{ marginBottom: '12px' }}>{design.title}</h3>

                  <div style={{ display: 'flex', gap: '8px', marginBottom: '12px', flexWrap: 'wrap' }}>
                    {design.cloudProvider && (
                      <span className={`tag tag-${design.cloudProvider.toLowerCase()}`}>
                        {design.cloudProvider}
                      </span>
                    )}
                    {design.architectureStyle && (
                      <span className={`tag tag-${design.architectureStyle.toLowerCase()}`}>
                        {design.architectureStyle}
                      </span>
                    )}
                    <span
                      className="tag"
                      style={{
                        background: design.status === 'COMPLETED' ? '#4caf50' :
                                   design.status === 'FAILED' ? '#f44336' : '#ff9800',
                        color: '#fff',
                      }}
                    >
                      {design.status}
                    </span>
                  </div>

                  <p style={{ color: '#a0a0a0', fontSize: '14px' }}>
                    {new Date(design.generatedAt).toLocaleString()}
                  </p>

                  {design.llmProvider && (
                    <p style={{ color: '#666', fontSize: '12px', marginTop: '8px' }}>
                      Provider: {design.llmProvider}
                    </p>
                  )}
                </div>
              </Link>
            ))}
          </div>

          {/* Pagination */}
          {totalPages > 1 && (
            <div style={{ display: 'flex', justifyContent: 'center', gap: '12px', marginTop: '30px' }}>
              <button
                className="btn btn-secondary"
                onClick={() => setPage((p) => Math.max(0, p - 1))}
                disabled={page === 0}
              >
                Previous
              </button>
              <span style={{ display: 'flex', alignItems: 'center', color: '#a0a0a0' }}>
                Page {page + 1} of {totalPages}
              </span>
              <button
                className="btn btn-secondary"
                onClick={() => setPage((p) => Math.min(totalPages - 1, p + 1))}
                disabled={page >= totalPages - 1}
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
}

export default HistoryPage;
