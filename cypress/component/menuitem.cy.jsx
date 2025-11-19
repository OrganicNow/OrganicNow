// cypress/e2e/menuitem.cy.js
import { profileMenuItems, settingsMenuItems } from "../../frontend/src/component/menuitem.js";

describe("menuitem.js content test", () => {
  it("should contain correct profile menu items", () => {
    expect(profileMenuItems).to.have.length(4);
    expect(profileMenuItems[0]).to.deep.equal({ label: "Profile", icon: "pi pi-user" });
    expect(profileMenuItems[1]).to.deep.equal({ label: "Account Settings", icon: "pi pi-cog" });
    expect(profileMenuItems[2]).to.deep.equal({ separator: true });
    expect(profileMenuItems[3]).to.deep.equal({ label: "Logout", icon: "pi pi-sign-out" });
  });

  it("should contain correct settings menu items", () => {
    expect(settingsMenuItems).to.have.length(3);
    expect(settingsMenuItems[0]).to.deep.equal({ label: "Preferences", icon: "pi pi-sliders-h" });
    expect(settingsMenuItems[1]).to.deep.equal({ label: "Theme", icon: "pi pi-palette" });
    expect(settingsMenuItems[2]).to.deep.equal({ label: "Language", icon: "pi pi-globe" });
  });
});
