package vn.iotstar.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for StyleValue utilities
 * Maps StyleValue IDs to names and color information
 * @author BadmintonMarketplace
 * @since 2025-10-27
 */
public class StyleValueHelper {
    
    /**
     * Get color hex code based on color name
     * This provides default colors for common Vietnamese color names
     */
    public static String getColorHex(String colorName) {
        if (colorName == null) return "#cccccc";
        
        Map<String, String> colorMap = new HashMap<>();
        colorMap.put("Đỏ", "#ff4444");
        colorMap.put("Xanh", "#4444ff");
        colorMap.put("Xanh dương", "#4444ff");
        colorMap.put("Xanh lá", "#44ff44");
        colorMap.put("Đen", "#000000");
        colorMap.put("Trắng", "#ffffff");
        colorMap.put("Xám", "#808080");
        colorMap.put("Vàng", "#ffdd00");
        colorMap.put("Hồng", "#ff69b4");
        colorMap.put("Cam", "#ff8800");
        colorMap.put("Tím", "#9944ff");
        colorMap.put("Nâu", "#8B4513");
        
        return colorMap.getOrDefault(colorName, "#cccccc");
    }
    
    /**
     * Get color gradient for frontend display
     */
    public static String getColorGradient(String colorName) {
        if (colorName == null) return "linear-gradient(135deg, #cccccc, #999999)";
        
        String hex = getColorHex(colorName);
        String darkHex = hex;
        
        // Create darker version for gradient
        switch (colorName) {
            case "Đỏ":
                darkHex = "#cc0000";
                break;
            case "Xanh":
            case "Xanh dương":
                darkHex = "#0000cc";
                break;
            case "Xanh lá":
                darkHex = "#00cc00";
                break;
            case "Đen":
                darkHex = "#000000";
                break;
            case "Trắng":
                darkHex = "#e0e0e0";
                break;
            case "Xám":
                darkHex = "#666666";
                break;
            case "Vàng":
                darkHex = "#ccaa00";
                break;
            case "Hồng":
                darkHex = "#ff1493";
                break;
            case "Cam":
                darkHex = "#cc6600";
                break;
            case "Tím":
                darkHex = "#6600cc";
                break;
            case "Nâu":
                darkHex = "#654321";
                break;
            default:
                darkHex = "#999999";
        }
        
        return "linear-gradient(135deg, " + hex + ", " + darkHex + ")";
    }
    
    /**
     * Check if a StyleValue represents a color
     * based on common color-related Style names
     */
    public static boolean isColorStyleValue(String styleName) {
        if (styleName == null) return false;
        String lower = styleName.toLowerCase();
        return lower.contains("màu") || lower.contains("color");
    }
}
