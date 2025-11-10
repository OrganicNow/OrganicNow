import React, { useEffect, useRef } from 'react';
import QRCode from 'qrcode';

const QRCodeGenerator = ({ 
  value, 
  size = 150, 
  className = '',
  errorMessage = 'Unable to generate QR code'
}) => {
  const canvasRef = useRef(null);

  useEffect(() => {
    if (value && canvasRef.current) {
      QRCode.toCanvas(canvasRef.current, value, {
        width: size,
        height: size,
        color: {
          dark: '#000000',
          light: '#FFFFFF'
        },
        margin: 2,
        errorCorrectionLevel: 'M'
      })
      .catch(err => {
        console.error('QR Code generation failed:', err);
      });
    }
  }, [value, size]);

  if (!value) {
    return (
      <div 
        className={`d-flex flex-column align-items-center justify-content-center ${className}`}
        style={{
          width: size,
          height: size,
          backgroundColor: '#f8f9fa',
          border: '2px dashed #dee2e6',
          borderRadius: '8px'
        }}
      >
        <i className="bi bi-exclamation-triangle" style={{fontSize: '2rem', color: '#ffc107'}}></i>
        <small className="text-muted mt-2 text-center">{errorMessage}</small>
      </div>
    );
  }

  return (
    <div className={`qr-code-wrapper ${className}`}>
      <canvas 
        ref={canvasRef}
        className="border rounded"
        style={{ maxWidth: '100%', height: 'auto' }}
      />
    </div>
  );
};

export default QRCodeGenerator;