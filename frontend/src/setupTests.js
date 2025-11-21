// setupTests.js
import { vi } from 'vitest';
import '@testing-library/jest-dom';

// Mock window methods
Object.defineProperty(window, 'scrollTo', {
  value: vi.fn(),
  writable: true
});

// Mock URL methods
global.URL.createObjectURL = vi.fn();
global.URL.revokeObjectURL = vi.fn();

// Mock console methods
console.error = vi.fn();
console.warn = vi.fn();

// Mock Bootstrap
global.bootstrap = {
  Modal: {
    getInstance: vi.fn(() => ({
      show: vi.fn(),
      hide: vi.fn(),
      dispose: vi.fn()
    })),
    prototype: {
      hide: vi.fn()
    }
  }
};

// Mock document methods
document.querySelectorAll = vi.fn(() => []);
document.body.classList = {
  remove: vi.fn()
};
document.body.style = {};

// Mock IntersectionObserver
global.IntersectionObserver = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));

// Mock ResizeObserver
global.ResizeObserver = vi.fn(() => ({
  observe: vi.fn(),
  unobserve: vi.fn(),
  disconnect: vi.fn(),
}));