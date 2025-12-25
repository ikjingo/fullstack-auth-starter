import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from 'path'

export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    // 코드 스플리팅 설정
    rollupOptions: {
      output: {
        manualChunks: {
          // React 코어 라이브러리
          'vendor-react': ['react', 'react-dom', 'react-router-dom'],
          // UI 라이브러리 (Radix UI)
          'vendor-ui': [
            '@radix-ui/react-dialog',
            '@radix-ui/react-dropdown-menu',
            '@radix-ui/react-tabs',
            '@radix-ui/react-checkbox',
            '@radix-ui/react-label',
            '@radix-ui/react-slot',
          ],
          // 상태 관리 & 데이터 페칭
          'vendor-state': ['zustand', '@tanstack/react-query'],
          // 폼 관련
          'vendor-form': ['react-hook-form', '@hookform/resolvers', 'zod'],
          // 유틸리티
          'vendor-utils': ['clsx', 'tailwind-merge', 'class-variance-authority'],
          // 아이콘 라이브러리
          'vendor-icons': ['lucide-react'],
        },
      },
    },
    // 청크 크기 경고 임계값 (KB)
    chunkSizeWarningLimit: 500,
  },
  server: {
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
