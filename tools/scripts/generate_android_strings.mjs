import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const projectRoot = path.resolve(__dirname, '..');

const enModule = await import(path.join(projectRoot, 'src/locales/en.ts'));
const twModule = await import(path.join(projectRoot, 'src/locales/zh-TW.ts'));
const cnModule = await import(path.join(projectRoot, 'src/locales/zh-CN.ts'));

const en = enModule.default;
const tw = twModule.default;
const cn = cnModule.default;

/**
 * Sanitize TypeScript translation keys into valid Android resource names.
 * AAPT2 identifier rules:
 * - Must match [a-zA-Z_][a-zA-Z0-9_]*
 * - Non-ASCII (e.g. animal emojis) mapped to u<codepoint> (e.g. animal.🐱 -> animal_u1f431)
 * - Dots and hyphens replaced with underscores
 * - Keys starting with a digit (e.g. 404.title) prepended with '_' (_404_title)
 */
function sanitizeKey(k) {
  let s = k;
  // Handle non-ascii characters (emojis)
  s = s.replace(/[^\x00-\x7F]+/g, (match) => {
    const cps = [...match].map(c => c.codePointAt(0).toString(16));
    return 'u' + cps.join('_');
  });
  // Replace . and - with _
  s = s.replace(/[\.\-]/g, '_');
  // If starts with digit, prepend _
  if (/^[0-9]/.test(s)) {
    s = '_' + s;
  }
  return s;
}

/**
 * XML & Android Resource string escaping
 */
function escapeXml(str) {
  if (typeof str !== 'string') return '';
  let s = str;

  // 1. Ampersand MUST be first
  s = s.replace(/&/g, '&amp;');
  // 2. Angle brackets
  s = s.replace(/</g, '&lt;');
  s = s.replace(/>/g, '&gt;');
  // 3. Apostrophes and quotes
  // In Android XML, apostrophe ' must be escaped as \'
  // and quote " must be escaped as \"
  s = s.replace(/'/g, "\\'");
  s = s.replace(/"/g, '\\"');

  // 4. Leading @ or ? must be escaped
  if (s.startsWith('@') || s.startsWith('?')) {
    s = '\\' + s;
  }

  return s;
}

const placeholderRegex = /\{+([a-zA-Z0-9_]+)\}+/g;

/**
 * Process string or array value.
 * Maps parameters {param} or {{param}} to positional format tokens %1$s, %2$s, etc.
 * based on the canonical order of parameters in the English dictionary.
 */
function processValue(key, val, enVal) {
  if (Array.isArray(val)) {
    return {
      isArray: true,
      items: val.map(item => escapeXml(item))
    };
  }

  // Find canonical parameter order from English string
  const enMatches = [...(typeof enVal === 'string' ? enVal.matchAll(placeholderRegex) : [])].map(m => m[1]);
  const paramOrder = [];
  for (const p of enMatches) {
    if (!paramOrder.includes(p)) paramOrder.push(p);
  }

  let text = typeof val === 'string' ? val : String(val ?? '');
  let hasFormat = false;

  if (paramOrder.length > 0) {
    hasFormat = true;
    text = text.replace(placeholderRegex, (match, pName) => {
      const idx = paramOrder.indexOf(pName);
      if (idx === -1) {
        throw new Error(`Key ${key} param ${pName} not in EN ${paramOrder}`);
      }
      return `%${idx + 1}$s`;
    });
  }

  let escaped = escapeXml(text);

  // If the string contains literal '%' and does NOT have format specifiers,
  // Android AAPT/Lint requires formatted="false" to avoid string format errors.
  const needsFormattedFalse = !hasFormat && text.includes('%');

  return {
    isArray: false,
    value: escaped,
    needsFormattedFalse
  };
}

function generateXml(localeData, enData, appName = 'LingUBible') {
  const lines = [
    '<?xml version="1.0" encoding="utf-8"?>',
    '<!-- Generated automatically from src/locales/ - DO NOT EDIT MANUALLY -->',
    '<resources>',
    `    <string name="app_name">${escapeXml(appName)}</string>`
  ];

  for (const [k, v] of Object.entries(localeData)) {
    const resName = sanitizeKey(k);
    const processed = processValue(k, v, enData[k]);

    if (processed.isArray) {
      lines.push(`    <string-array name="${resName}">`);
      for (const item of processed.items) {
        lines.push(`        <item>${item}</item>`);
      }
      lines.push('    </string-array>');
    } else {
      const attr = processed.needsFormattedFalse ? ' formatted="false"' : '';
      lines.push(`    <string name="${resName}"${attr}>${processed.value}</string>`);
    }
  }

  lines.push('</resources>');
  lines.push('');
  return lines.join('\n');
}

// Generate all three XML strings
const enXml = generateXml(en, en, 'LingUBible');
const twXml = generateXml(tw, en, 'LingUBible');
const cnXml = generateXml(cn, en, 'LingUBible');

const outDir = path.resolve(__dirname, 'app/src/main/res');
fs.mkdirSync(path.join(outDir, 'values'), { recursive: true });
fs.mkdirSync(path.join(outDir, 'values-zh-rTW'), { recursive: true });
fs.mkdirSync(path.join(outDir, 'values-zh-rCN'), { recursive: true });

fs.writeFileSync(path.join(outDir, 'values/strings.xml'), enXml, 'utf-8');
fs.writeFileSync(path.join(outDir, 'values-zh-rTW/strings.xml'), twXml, 'utf-8');
fs.writeFileSync(path.join(outDir, 'values-zh-rCN/strings.xml'), cnXml, 'utf-8');

console.log('Successfully generated and wrote strings.xml to values, values-zh-rTW, values-zh-rCN.');
