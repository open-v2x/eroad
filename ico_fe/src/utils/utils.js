

/**
 * 动态计算 label-width
 * @param {number} baseWidthPx - 基础 px 宽度，例如 120
 * @returns {string} - 动态计算后的宽度，单位为 px
 */
export function getDynamicLabelWidth(baseWidthPx = 120) {
  const viewportBase = 1920; // 基础视口宽度，一般以设计稿宽度为准
  const actualViewportWidth = document.documentElement.clientWidth;

  const scaleFactor = actualViewportWidth / viewportBase;
  return `${baseWidthPx * scaleFactor}px`;
}
