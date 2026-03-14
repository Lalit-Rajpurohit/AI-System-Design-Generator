import React, { useEffect, useRef, useState } from 'react';
import mermaid from 'mermaid';
import html2canvas from 'html2canvas';
import { Camera, Image, Copy, Code, AlertTriangle } from 'lucide-react';

// Initialize mermaid once at module level
let mermaidInitialized = false;
if (!mermaidInitialized) {
  mermaid.initialize({
    startOnLoad: false,
    theme: 'base',
    themeVariables: {
      primaryColor: '#8b5cf6',
      primaryTextColor: '#ffffff',
      primaryBorderColor: '#7c3aed',
      lineColor: '#71717a',
      secondaryColor: '#18181b',
      tertiaryColor: '#09090b',
      background: '#ffffff',
      mainBkg: '#8b5cf6',
      nodeBorder: '#7c3aed',
      clusterBkg: '#f4f4f5',
      clusterBorder: '#d4d4d8',
      titleColor: '#18181b',
      edgeLabelBackground: '#ffffff',
      nodeTextColor: '#ffffff',
    },
    flowchart: {
      useMaxWidth: true,
      htmlLabels: true,
      curve: 'basis',
      padding: 30,
      nodeSpacing: 70,
      rankSpacing: 100,
      wrappingWidth: 200,
    },
    securityLevel: 'loose',
    suppressErrorRendering: true,
  });
  mermaidInitialized = true;
}

// Service to emoji icon mapping
const SERVICE_ICONS = {
  // AWS
  's3': '🪣',
  'lambda': 'λ',
  'ec2': '🖥️',
  'ecs': '📦',
  'eks': '☸️',
  'rds': '🗄️',
  'dynamodb': '⚡',
  'aurora': '🌟',
  'elasticache': '💾',
  'redis': '💾',
  'sqs': '📬',
  'sns': '📢',
  'cloudfront': '🌐',
  'cdn': '🌐',
  'api gateway': '🚪',
  'api_gw': '🚪',
  'alb': '⚖️',
  'nlb': '⚖️',
  'elb': '⚖️',
  'load balancer': '⚖️',
  'cognito': '🔐',
  'iam': '🔑',
  'waf': '🛡️',
  'cloudwatch': '📊',
  'kinesis': '🌊',
  'msk': '📨',
  'kafka': '📨',
  'opensearch': '🔍',
  'elasticsearch': '🔍',

  // Azure
  'app service': '🖥️',
  'cosmos db': '🌍',
  'blob storage': '🪣',
  'aks': '☸️',
  'service bus': '🚌',
  'event hub': '📡',

  // GCP
  'cloud run': '🏃',
  'cloud functions': 'λ',
  'gke': '☸️',
  'cloud sql': '🗄️',
  'firestore': '🔥',
  'bigtable': '📊',
  'cloud storage': '🪣',
  'pubsub': '📢',
  'memorystore': '💾',

  // Generic
  'database': '🗄️',
  'db': '🗄️',
  'postgres': '🐘',
  'postgresql': '🐘',
  'mysql': '🐬',
  'mongodb': '🍃',
  'cache': '💾',
  'queue': '📬',
  'storage': '🪣',
  'server': '🖥️',
  'service': '⚙️',
  'gateway': '🚪',
  'api': '🔌',
  'auth': '🔐',
  'user': '👤',
  'users': '👥',
  'client': '📱',
  'mobile': '📱',
  'web': '🌐',
  'notification': '🔔',
  'analytics': '📈',
  'monitoring': '📊',
  'search': '🔍',
  'encoding': '🎬',
  'video': '🎬',
  'recommendation': '💡',
  'engine': '⚙️',
};

// Find icon for a label
function getIconForLabel(label) {
  if (!label) return null;
  const lower = label.toLowerCase();

  // Check each keyword
  for (const [keyword, icon] of Object.entries(SERVICE_ICONS)) {
    if (lower.includes(keyword)) {
      return icon;
    }
  }
  return null;
}

// Sanitize Mermaid diagram and add icons to labels
function sanitizeMermaidDiagram(diagram) {
  let sanitized = diagram;

  // Replace problematic characters inside node labels [...]
  // Also add icons to labels
  sanitized = sanitized.replace(/\[([^\]]+)\]/g, (match, content) => {
    // Replace parentheses with dashes or remove them
    let cleaned = content
      .replace(/\(/g, ' - ')
      .replace(/\)/g, '')
      .replace(/\s+/g, ' ')
      .trim();

    // Add icon if available
    const icon = getIconForLabel(cleaned);
    if (icon) {
      cleaned = `${icon} ${cleaned}`;
    }

    return `[${cleaned}]`;
  });

  // Fix any double dashes that might have been created
  sanitized = sanitized.replace(/\s+-\s+-\s+/g, ' - ');
  sanitized = sanitized.replace(/\s+-\s+$/gm, '');

  return sanitized;
}

function MermaidDiagram({ diagram, title }) {
  const containerRef = useRef(null);
  const [error, setError] = useState(null);
  const [rendered, setRendered] = useState(false);
  const [rawDiagram, setRawDiagram] = useState('');
  const [svgContent, setSvgContent] = useState('');

  useEffect(() => {
    let isMounted = true;

    const renderDiagram = async () => {
      if (!diagram) return;

      try {
        setError(null);
        setRendered(false);

        // Clean up the diagram string
        let cleanDiagram = diagram
          .replace(/\\n/g, '\n')
          .replace(/\\t/g, '\t')
          .replace(/\\"/g, '"')
          .trim();

        // Sanitize the diagram to fix common issues
        cleanDiagram = sanitizeMermaidDiagram(cleanDiagram);

        if (isMounted) {
          setRawDiagram(cleanDiagram);
        }

        // Generate unique ID
        const id = `mermaid-${Date.now()}`;

        // Create a temporary container for rendering
        // Use visibility:hidden instead of off-screen positioning
        // because getBBox() requires the element to be in normal document flow
        const tempContainer = document.createElement('div');
        tempContainer.id = `${id}-container`;
        tempContainer.style.visibility = 'hidden';
        tempContainer.style.position = 'absolute';
        tempContainer.style.width = '100%';
        tempContainer.style.height = '0';
        tempContainer.style.overflow = 'hidden';
        document.body.appendChild(tempContainer);

        try {
          // Render diagram - Mermaid v10 API
          const { svg } = await mermaid.render(id, cleanDiagram, tempContainer);

          if (isMounted) {
            setSvgContent(svg);
            setRendered(true);
            setError(null);
          }
        } finally {
          // Clean up the temporary container
          if (tempContainer.parentNode) {
            document.body.removeChild(tempContainer);
          }
          // Also clean up any leftover mermaid elements with the render id
          const leftover = document.getElementById(id);
          if (leftover && leftover.parentNode) {
            leftover.parentNode.removeChild(leftover);
          }
          const leftoverContainer = document.getElementById(`${id}-container`);
          if (leftoverContainer && leftoverContainer.parentNode) {
            leftoverContainer.parentNode.removeChild(leftoverContainer);
          }
        }
      } catch (err) {
        console.error('Mermaid render error:', err);
        if (isMounted) {
          setError(err.message || 'Failed to render diagram');
          // Show raw diagram as fallback
          const rawCode = diagram
            .replace(/\\n/g, '\n')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;');
          setSvgContent(`
            <div style="padding: 24px; background: #18181b; border-radius: 10px; color: #a1a1aa; font-family: monospace; font-size: 13px; white-space: pre-wrap; overflow-x: auto; line-height: 1.7;">
              ${rawCode}
            </div>
          `);
        }
      }
    };

    renderDiagram();

    return () => {
      isMounted = false;
    };
  }, [diagram]);

  const exportToPng = async () => {
    if (!containerRef.current) return;

    try {
      const canvas = await html2canvas(containerRef.current, {
        backgroundColor: '#ffffff',
        scale: 2,
        logging: false,
      });

      const link = document.createElement('a');
      link.download = `${(title || 'architecture-diagram').replace(/\s+/g, '-').toLowerCase()}.png`;
      link.href = canvas.toDataURL('image/png');
      link.click();
    } catch (err) {
      console.error('Export error:', err);
      alert('Failed to export diagram');
    }
  };

  const copySvg = () => {
    if (svgContent) {
      navigator.clipboard.writeText(svgContent)
        .then(() => alert('SVG copied to clipboard!'))
        .catch(() => alert('Failed to copy SVG'));
    }
  };

  const copyMermaid = () => {
    navigator.clipboard.writeText(rawDiagram)
      .then(() => alert('Mermaid code copied to clipboard!'))
      .catch(() => alert('Failed to copy'));
  };

  if (!diagram) {
    return (
      <div className="card" style={{ textAlign: 'center', padding: '60px 20px' }}>
        <div style={{ marginBottom: '16px', color: 'var(--text-muted)' }}>
          <Code size={48} strokeWidth={1.5} />
        </div>
        <p style={{ color: 'var(--text-muted)' }}>No diagram available</p>
      </div>
    );
  }

  return (
    <div>
      {error && (
        <div className="alert alert-error" style={{ marginBottom: '16px' }}>
          <span className="alert-icon">
            <AlertTriangle />
          </span>
          <div>
            <strong>Diagram Rendering Error</strong>
            <p style={{ fontSize: '13px', marginTop: '4px' }}>{error}</p>
          </div>
        </div>
      )}

      <div className="diagram-actions">
        <button className="btn btn-secondary btn-icon" onClick={exportToPng} title="Export as PNG" disabled={!rendered}>
          <Camera size={18} />
        </button>
        <button className="btn btn-secondary btn-icon" onClick={copySvg} title="Copy SVG" disabled={!rendered}>
          <Image size={18} />
        </button>
        <button className="btn btn-secondary btn-icon" onClick={copyMermaid} title="Copy Mermaid Code">
          <Copy size={18} />
        </button>
      </div>

      <div
        className="diagram-container"
        ref={containerRef}
        style={{
          background: '#ffffff',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '400px',
        }}
      >
        {!svgContent && !error && (
          <div style={{ textAlign: 'center', padding: '40px' }}>
            <div className="spinner"></div>
            <p style={{ marginTop: '16px', color: '#71717a' }}>Rendering diagram...</p>
          </div>
        )}
        {svgContent && (
          <div
            dangerouslySetInnerHTML={{ __html: svgContent }}
            style={{
              width: '100%',
              display: 'flex',
              justifyContent: 'center',
              padding: '20px',
            }}
          />
        )}
      </div>

      {/* Mermaid Source */}
      {rawDiagram && (
        <details style={{ marginTop: '18px' }}>
          <summary
            style={{
              cursor: 'pointer',
              color: 'var(--text-muted)',
              fontSize: '0.875rem',
              padding: '10px 0',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              fontWeight: 500,
            }}
          >
            <Code size={16} />
            View Mermaid Source Code
          </summary>
          <div className="code-block" style={{ marginTop: '10px' }}>
            <div className="code-header">
              <span className="code-title">diagram.mmd</span>
              <button className="btn btn-ghost" onClick={copyMermaid}>
                <Copy size={14} /> Copy
              </button>
            </div>
            <pre className="code-content" style={{ maxHeight: '220px', overflow: 'auto' }}>
              <code>{rawDiagram}</code>
            </pre>
          </div>
        </details>
      )}
    </div>
  );
}

export default MermaidDiagram;
