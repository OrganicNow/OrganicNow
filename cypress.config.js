const { defineConfig } = require("cypress");

module.exports = defineConfig({
  video: false,
  retries: { runMode: 1, openMode: 0 },

  env: {
    adminUsername: "superadmin",
    adminPassword: "admin123"
  },

  e2e: {
    baseUrl: "http://localhost:5173",
    specPattern: [
      "cypress/e2e/**/*.cy.{js,jsx,ts,tsx}",
      "frontend/cypress/e2e/**/*.cy.{js,jsx,ts,tsx}",
    ],
    testIsolation: false,
    chromeWebSecurity: false,

    setupNodeEvents(on, config) {
      return config;
    },
  },
});