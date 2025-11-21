export const pageSize = 12;
// export const apiPath = "http://localhost:8080";
// export const apiPath = import.meta.env?.VITE_API_URL || "http://localhost:8080";

// dev ใช้ .env.development หรือไม่มี env ก็ fallback เป็น localhost
const DEV_FALLBACK = "http://localhost:8080";
// เปลี่ยนจาก ngrok URL เป็น empty string หรือ "/api"
const PROD_FALLBACK = "/api"; // หรือ "/api"

export const apiPath =
    import.meta.env?.VITE_API_URL ||
    (import.meta.env?.MODE === "production" ? PROD_FALLBACK : DEV_FALLBACK);