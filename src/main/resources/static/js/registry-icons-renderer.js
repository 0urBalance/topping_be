/**
 * RegistryIconsRenderer
 * Generates SVG icons programmatically from JSON structure data
 * Falls back to static HTML if JSON loading fails
 */
class RegistryIconsRenderer {
  constructor(containerSelector = '.registry-icons') {
    this.container = document.querySelector(containerSelector);
    this.config = null;
  }

  async init() {
    try {
      // Fetch icon configuration
      const response = await fetch('/js/home/registry-icons-config.json');
      if (!response.ok) throw new Error('Failed to load config');
      this.config = await response.json();

      // Apply jellybean background
      if (this.config.background) {
        await this.applyJellybeanBackground();
      }

      // Load and render all icons
      await this.renderAllIcons();
      this.attachEventListeners();
    } catch (error) {
      console.error('Registry icons renderer failed:', error);
      // Static HTML remains as fallback
    }
  }

  async renderAllIcons() {
    if (!this.config || !this.container) return;

    // Clear container
    this.container.innerHTML = '';

    // Render each icon
    for (const iconConfig of this.config.icons) {
      const iconElement = await this.createIconElement(iconConfig);
      if (iconElement) {
        this.container.appendChild(iconElement);
      }
    }
  }

  async createIconElement(iconConfig) {
    const div = document.createElement('div');
    div.className = `registry-icon ${iconConfig.className}`;
    div.setAttribute('role', 'button');
    div.setAttribute('tabindex', '0');
    div.setAttribute('aria-label', iconConfig.label);
    div.setAttribute('data-icon-id', iconConfig.id);

    // Apply inline styles from config
    if (iconConfig.styling) {
      Object.entries(iconConfig.styling).forEach(([key, value]) => {
        div.style[key] = value;
      });
    }

    // Generate SVG from structure file
    const svg = await this.generateSVGFromStructure(iconConfig);
    if (svg) {
      div.innerHTML = svg;
    }

    return div;
  }

  async generateSVGFromStructure(iconConfig) {
    try {
      if (!iconConfig.structureFile) return null;

      const response = await fetch(iconConfig.structureFile);
      if (!response.ok) throw new Error(`Failed to load ${iconConfig.structureFile}`);

      const structure = await response.json();
      return this.createSVGFromData(structure);
    } catch (error) {
      console.error(`Error generating SVG for ${iconConfig.id}:`, error);
      return null;
    }
  }

  async applyJellybeanBackground() {
    try {
      const bgConfig = this.config.background;
      if (!bgConfig || !bgConfig.structureFiles) return;

      // Load both structure files
      const [bgResponse, fgResponse] = await Promise.all([
        fetch(bgConfig.structureFiles[0]),
        fetch(bgConfig.structureFiles[1])
      ]);

      const bgStructure = await bgResponse.json();
      const fgStructure = await fgResponse.json();

      // Extract background color
      const bgColor = bgStructure.structure.styles?.bg || 'rgb(135, 96, 79)';
      const patternCount = fgStructure.summary?.instances || 6;

      // Create SVG pattern
      const svgPattern = this.createJellybeanPattern(bgColor, patternCount);

      // Apply to target element
      const targetElement = document.querySelector(bgConfig.targetSelector);
      if (targetElement) {
        targetElement.style.background = `${bgColor} url('data:image/svg+xml;base64,${btoa(svgPattern)}') repeat`;
        targetElement.style.backgroundSize = '200px 200px';
      }
    } catch (error) {
      console.error('Failed to apply jellybean background:', error);
    }
  }

  createJellybeanPattern(bgColor, patternCount) {
    // Create SVG pattern with dots
    return `<svg xmlns="http://www.w3.org/2000/svg" width="200" height="200">
      <defs>
        <pattern id="jellybeanPattern" x="0" y="0" width="40" height="40" patternUnits="userSpaceOnUse">
          <circle cx="20" cy="20" r="3" fill="white" opacity="0.3"/>
        </pattern>
      </defs>
      <rect width="200" height="200" fill="url(#jellybeanPattern)"/>
    </svg>`;
  }

  createSVGFromData(structure) {
    const data = structure.structure;
    const componentName = structure.metadata?.componentName || data.name;

    // componentName에 따라 커스텀 SVG 생성
    switch(componentName) {
      case 'aryeong':
        return this.createAryeongSVG();
      case 'raman':
        return this.createRamanSVG();
      case 'cake':
        return this.createCakeSVG();
      case 'coffee':
        return this.createCoffeeSVG();
      case 't-short':
        return this.createTShortSVG();
      default:
        return this.createDefaultSVG();
    }
  }

  createAryeongSVG() {
    return `<svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
      <rect x="2" y="10" width="8" height="16" rx="2" fill="currentColor" opacity="0.8"/>
      <rect x="3" y="11" width="6" height="14" rx="1" fill="currentColor"/>
      <rect x="10" y="14" width="16" height="8" rx="1" fill="currentColor"/>
      <line x1="10" y1="16" x2="26" y2="16" stroke="currentColor" stroke-width="0.5" opacity="0.3"/>
      <line x1="10" y1="20" x2="26" y2="20" stroke="currentColor" stroke-width="0.5" opacity="0.3"/>
      <rect x="26" y="10" width="8" height="16" rx="2" fill="currentColor" opacity="0.8"/>
      <rect x="27" y="11" width="6" height="14" rx="1" fill="currentColor"/>
    </svg>`;
  }

  createRamanSVG() {
    return `<svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
      <ellipse cx="18" cy="28" rx="14" ry="3" fill="currentColor" opacity="0.3"/>
      <path d="M 6 20 Q 6 16 8 16 L 28 16 Q 30 16 30 20 L 30 26 Q 30 30 26 30 L 10 30 Q 6 30 6 26 Z"
            fill="currentColor" opacity="0.6"/>
      <path d="M 10 18 Q 12 22 10 25" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.9"/>
      <path d="M 14 17 Q 16 21 14 26" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.9"/>
      <path d="M 18 18 Q 20 20 18 24" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.9"/>
      <path d="M 22 17 Q 24 22 22 26" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.9"/>
      <path d="M 26 18 Q 24 21 26 25" stroke="currentColor" stroke-width="1.5" fill="none" opacity="0.9"/>
      <line x1="28" y1="10" x2="32" y2="22" stroke="currentColor" stroke-width="1" opacity="0.7"/>
      <line x1="30" y1="10" x2="34" y2="22" stroke="currentColor" stroke-width="1" opacity="0.7"/>
    </svg>`;
  }

  createCakeSVG() {
    return `<svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
      <ellipse cx="18" cy="30" rx="12" ry="2" fill="currentColor" opacity="0.2"/>
      <rect x="8" y="22" width="20" height="8" rx="1" fill="currentColor" opacity="0.7"/>
      <line x1="10" y1="24" x2="26" y2="24" stroke="white" stroke-width="0.5" opacity="0.3"/>
      <rect x="10" y="16" width="16" height="6" rx="1" fill="currentColor" opacity="0.8"/>
      <rect x="12" y="11" width="12" height="5" rx="1" fill="currentColor"/>
      <circle cx="15" cy="9" r="2" fill="#DC143C"/>
      <circle cx="21" cy="9" r="2" fill="#DC143C"/>
      <rect x="17" y="6" width="2" height="5" fill="currentColor" opacity="0.5"/>
      <ellipse cx="18" cy="5" rx="1.5" ry="2" fill="#FFD700"/>
    </svg>`;
  }

  createCoffeeSVG() {
    return `<svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
      <ellipse cx="18" cy="30" rx="10" ry="2" fill="currentColor" opacity="0.2"/>
      <path d="M 10 14 L 12 28 Q 12 30 14 30 L 22 30 Q 24 30 24 28 L 26 14 Z"
            fill="currentColor" opacity="0.8"/>
      <path d="M 11 16 L 13 27 Q 13 28 14 28 L 22 28 Q 23 28 23 27 L 25 16 Z"
            fill="currentColor"/>
      <ellipse cx="18" cy="14" rx="8" ry="2" fill="currentColor" opacity="0.5"/>
      <path d="M 26 18 Q 30 18 30 22 Q 30 26 26 26"
            stroke="currentColor" stroke-width="2" fill="none" opacity="0.8"/>
      <path d="M 14 10 Q 13 8 14 6" stroke="currentColor" stroke-width="1" fill="none" opacity="0.4"/>
      <path d="M 18 11 Q 17 9 18 7" stroke="currentColor" stroke-width="1" fill="none" opacity="0.4"/>
      <path d="M 22 10 Q 21 8 22 6" stroke="currentColor" stroke-width="1" fill="none" opacity="0.4"/>
    </svg>`;
  }

  createTShortSVG() {
    return `<svg viewBox="0 0 36 36" width="36" height="36" aria-hidden="true">
      <rect x="10" y="14" width="16" height="18" rx="2" fill="currentColor" opacity="0.8"/>
      <path d="M 15 14 Q 18 16 21 14" fill="none" stroke="currentColor" stroke-width="1"/>
      <ellipse cx="18" cy="15" rx="3" ry="2" fill="currentColor" opacity="0.3"/>
      <path d="M 10 14 L 6 18 L 6 22 L 10 20 Z" fill="currentColor" opacity="0.7"/>
      <path d="M 26 14 L 30 18 L 30 22 L 26 20 Z" fill="currentColor" opacity="0.7"/>
      <line x1="12" y1="18" x2="24" y2="18" stroke="currentColor" stroke-width="0.5" opacity="0.2"/>
      <line x1="12" y1="22" x2="24" y2="22" stroke="currentColor" stroke-width="0.5" opacity="0.2"/>
    </svg>`;
  }

  createDefaultSVG() {
    // Fallback for unknown items
    const iconSize = 36;
    return `<svg viewBox="0 0 ${iconSize} ${iconSize}" width="${iconSize}" height="${iconSize}" aria-hidden="true">
      <rect x="6" y="6" width="24" height="24" rx="4" fill="currentColor" opacity="0.5"/>
    </svg>`;
  }

  attachEventListeners() {
    this.container.querySelectorAll('[data-icon-id]').forEach(icon => {
      // Keyboard navigation
      icon.addEventListener('keydown', (e) => {
        if (e.key === 'Enter' || e.key === ' ') {
          e.preventDefault();
          this.handleIconClick(icon);
        }
      });

      // Click handler
      icon.addEventListener('click', () => this.handleIconClick(icon));
    });
  }

  handleIconClick(iconElement) {
    const iconId = iconElement.getAttribute('data-icon-id');
    console.log('Clicked icon:', iconId);
    // Future: Add navigation or filter logic
  }
}

// Initialize on DOM ready
document.addEventListener('DOMContentLoaded', async () => {
  const renderer = new RegistryIconsRenderer();
  await renderer.init();
});
