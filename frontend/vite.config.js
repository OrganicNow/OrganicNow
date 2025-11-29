import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// ใช้ proxy เฉพาะตอน dev เท่านั้น
export default defineConfig({
    plugins: [react()],
    base: '/organicnow-frontend/',                 
    build: {
        outDir: 'dist',          
        assetsDir: 'assets',     
    },
    server: {
        proxy: {
            '/api': {
                target: 'http://localhost:8080', // ใช้เฉพาะตอน dev
                changeOrigin: true,
            },
        },
    },
})


// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'
//
// // // https://vite.dev/config/
// // export default defineConfig({
// //   plugins: [react()],
// // })
// export default defineConfig({
//     plugins: [react()],
//     server: {
//         proxy: {
//             '/api': {
//                 target: 'http://localhost:8080',
//                 changeOrigin: true,
//             },
//         },
//     },
// })
