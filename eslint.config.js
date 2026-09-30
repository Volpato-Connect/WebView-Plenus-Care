const js = require("@eslint/js");
const globals = require("globals");

module.exports = [
  {
    ignores: ["android/**", "node_modules/**"],
  },
  {
    files: ["www/**/*.js"],
    languageOptions: {
      ecmaVersion: "latest",
      sourceType: "script",
      globals: {
        ...globals.browser,
        Capacitor: "readonly",
      },
    },
    rules: {
      ...js.configs.recommended.rules,
      indent: ["error", 2, { SwitchCase: 1 }],
    },
  },
];
