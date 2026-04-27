import type { ThemeConfig } from "antd";
import { theme } from "antd";

const PRIMARY = "#5d87ff";
const SUCCESS = "#13deb9";
const DANGER = "#fa896b";
const TEXT = "#2a3547";
const TEXT_MUTED = "#111827";
const BORDER = "#ebf1f6";
const BG_PAGE = "#f4f7fb";
const BG_CARD = "#ffffff";

const BG_PAGE_D = "#111c2d";
const BG_CARD_D = "#1c293e";
const BORDER_D = "#2a3f5c";

export function getDashboardTheme(mode: "light" | "dark"): ThemeConfig {
  const isDark = mode === "dark";
  if (isDark) {
    return {
      algorithm: theme.darkAlgorithm,
      token: {
        colorPrimary: "#6b92ff",
        colorSuccess: "#1ee0c0",
        colorError: "#ff9b85",
        colorWarning: "#ffc266",
        colorInfo: "#6b92ff",
        colorBgLayout: BG_PAGE_D,
        colorBgContainer: BG_CARD_D,
        colorBgElevated: "#243652",
        colorText: "#e8eef5",
        colorTextSecondary: "#8fa3bf",
        colorTextTertiary: "#6b7c94",
        colorBorder: BORDER_D,
        colorBorderSecondary: BORDER_D,
        colorSplit: BORDER_D,
        borderRadius: 10,
        borderRadiusLG: 12,
        fontFamily: `'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif`,
        boxShadow: "0 2px 12px rgba(0, 0, 0, 0.2)",
        boxShadowSecondary: "0 2px 8px rgba(0, 0, 0, 0.18)",
      },
      components: {
        Layout: {
          bodyBg: BG_PAGE_D,
          headerBg: BG_CARD_D,
          footerBg: BG_CARD_D,
          siderBg: BG_CARD_D,
          triggerBg: "#243652",
        },
        Menu: {
          itemBg: "transparent",
          subMenuItemBg: "transparent",
          itemColor: "#8fa3bf",
          itemHoverColor: "#e8eef5",
          itemHoverBg: "rgba(107, 146, 255, 0.12)",
          itemActiveBg: "rgba(107, 146, 255, 0.2)",
          itemSelectedColor: "#ffffff",
          itemSelectedBg: "#6b92ff",
          itemMarginInline: 8,
          itemMarginBlock: 4,
          iconSize: 18,
          fontSize: 14,
          itemBorderRadius: 8,
        },
        Card: {
          borderRadiusLG: 12,
        },
        Table: {
          borderColor: BORDER_D,
        },
        Input: {
          activeBorderColor: "#6b92ff",
          hoverBorderColor: "#6b92ff",
        },
      },
    };
  }
  return {
    algorithm: theme.defaultAlgorithm,
    token: {
      colorPrimary: PRIMARY,
      colorSuccess: SUCCESS,
      colorError: DANGER,
      colorInfo: PRIMARY,
      colorWarning: "#ffb74d",
      colorBgLayout: BG_PAGE,
      colorBgContainer: BG_CARD,
      colorBgElevated: BG_CARD,
      colorText: TEXT,
      colorTextSecondary: TEXT_MUTED,
      colorTextTertiary: TEXT_MUTED,
      colorBorder: BORDER,
      colorBorderSecondary: BORDER,
      colorSplit: BORDER,
      borderRadius: 10,
      borderRadiusLG: 12,
      fontFamily: `'Inter', system-ui, -apple-system, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif`,
      boxShadowSecondary: "0 2px 12px rgba(42, 53, 71, 0.06)",
    },
    components: {
      Layout: {
        bodyBg: BG_PAGE,
        headerBg: BG_CARD,
        footerBg: BG_CARD,
        siderBg: BG_CARD,
        triggerBg: BG_CARD,
      },
      Menu: {
        itemBg: "transparent",
        subMenuItemBg: "transparent",
        itemColor: TEXT_MUTED,
        itemHoverColor: TEXT,
        itemHoverBg: "rgba(93, 135, 255, 0.08)",
        itemActiveBg: "rgba(93, 135, 255, 0.12)",
        itemSelectedColor: "#ffffff",
        itemSelectedBg: PRIMARY,
        itemMarginInline: 8,
        itemMarginBlock: 4,
        iconSize: 18,
        fontSize: 14,
        itemBorderRadius: 8,
      },
      Card: {
        borderRadiusLG: 12,
      },
      Table: {
        borderColor: BORDER,
        headerBg: "#f8fafc",
      },
    },
  };
}

export const dashboardThemeColors = {
  primary: PRIMARY,
  success: SUCCESS,
  danger: DANGER,
  text: TEXT,
  textMuted: TEXT_MUTED,
  border: BORDER,
  bgPage: BG_PAGE,
  bgPageDark: BG_PAGE_D,
};
