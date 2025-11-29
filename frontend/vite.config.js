// vite.config.js
import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// กำหนด base path แบบมีเงื่อนไข (ใช้เฉพาะตอน build)
const isProd = process.env.NODE_ENV === 'production'

export default defineConfig({
    plugins: [react()],
    // ใช้ base ที่เป็น subpath เฉพาะตอน Build เท่านั้น
    base: isProd ? '/organicnow-frontend/' : '/',
    build: {
        outDir: 'dist',
        assetsDir: 'assets',
    },
    server: {
        // ใช้ proxy เหมือนเดิม เพราะตอน Dev ใช้ base: '/'
        proxy: {
            '/api': {
                target: 'https://muict.app/organicnow-backend',
                changeOrigin: true,
            },
        },
    },
})

//import { defineConfig } from 'vite'
//import react from '@vitejs/plugin-react'

// ใช้ proxy เฉพาะตอน dev เท่านั้น
//export default defineConfig({
    //plugins: [react()],
    //base: '/',                 // ✅ สำคัญ: ให้ index.html อ้าง /assets/... เสมอ
    //build: {
        //outDir: 'dist',          // ค่ามาตรฐาน
        //assetsDir: 'assets',     // ให้ไฟล์ JS/CSS ลงโฟลเดอร์ /assets
    //},
    //server: {
        //proxy: {
            //'/api': {
                //target: 'http://localhost:8080', // ใช้เฉพาะตอน dev
                //changeOrigin: true,
            //},
        //},
    //},
//})
