# App WebView (molde)

Aplicativo Android que abre um sistema web dentro de uma WebView, desenvolvido com Capacitor.

**Exemplo:** Sayan Saúde & Benefícios.

## Documentação

📘 [Tutorial completo: construindo um app WebView do zero (PDF)](docs/tutorial-app-webview-v2.pdf)

## Início rápido

```bash
git clone https://github.com/<org>/<repositorio>.git
cd <repositorio>
npm install
npx cap sync
npx cap open android
```

## Atualizações do projeto

Sempre que houver alguma alteração na configuração do Capacitor ou nos arquivos web que precise ser sincronizada com o projeto Android, execute os seguintes comandos:

```bash
npx cap sync
npx cap open android
```

* `npx cap sync`: sincroniza as alterações do projeto web e as configurações do Capacitor com o projeto Android.
* `npx cap open android`: abre o projeto Android no Android Studio para executar e testar o aplicativo.

**Importante:** execute os comandos nessa ordem sempre que precisar atualizar o projeto Android após alterações.
