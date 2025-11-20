// src/component/test/menuItems.test.js
import { describe, it, expect } from "vitest";
import { profileMenuItems, settingsMenuItems } from "../menuitem";

describe("menuItems config", () => {
  it("ควรมี profileMenuItems ที่ประกอบด้วยเมนู Logout หนึ่งรายการ", () => {
    expect(Array.isArray(profileMenuItems)).toBe(true);
    expect(profileMenuItems).toHaveLength(1);

    const item = profileMenuItems[0];
    expect(item).toEqual({
      label: "Logout",
      icon: "pi pi-sign-out",
    });
  });

  it("ควรมี settingsMenuItems สามรายการ: Preferences, Theme, Language", () => {
    expect(Array.isArray(settingsMenuItems)).toBe(true);
    expect(settingsMenuItems).toHaveLength(3);

    expect(settingsMenuItems[0]).toEqual({
      label: "Preferences",
      icon: "pi pi-sliders-h",
    });

    expect(settingsMenuItems[1]).toEqual({
      label: "Theme",
      icon: "pi pi-palette",
    });

    expect(settingsMenuItems[2]).toEqual({
      label: "Language",
      icon: "pi pi-globe",
    });
  });
});
