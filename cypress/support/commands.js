// Command สำหรับ login ที่ใช้ structure จริง
Cypress.Commands.add('login', (username = 'superadmin', password = 'admin123') => {
  cy.session([username, password], () => {
    cy.visit('/login');
    cy.get('input[type="text"]').type(username);
    cy.get('input[type="password"]').type(password, { log: false });
    cy.get('button[type="submit"]').click();
    cy.url({ timeout: 10000 }).should('include', '/dashboard');
  });
});

// Command สำหรับ login แล้วไปที่หน้าเฉพาะ
Cypress.Commands.add('loginAndVisit', (path = '/dashboard', username = 'superadmin', password = 'admin123') => {
  cy.login(username, password);
  cy.visit(path);
});