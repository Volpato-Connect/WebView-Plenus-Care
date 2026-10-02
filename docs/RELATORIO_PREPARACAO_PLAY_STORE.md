# Relatório: preparação do Plenus Care para a Play Store

**Data:** 01/10/2026
**App:** Plenus Care (`tech.norte1.plenuscare`), versionCode `1`, versionName `1.0`, targetSdk `36`

## Resumo

O app foi verificado como em uma pipeline de CI antes da publicação. O `.aab` já seria aceito tecnicamente pela Play Store, mas a análise do sistema web (`plenuscare.bemestar.1norte.tech`) mostrou que ele tem **teleconsulta por vídeo, envio de selfie/documentos e download de arquivos**, e o app não suportava nenhuma dessas funções. Foram feitas 6 correções. Depois delas o build compila, o lint do Android não tem erros e o `.aab` sai assinado.

## Melhorias feitas

### 1. Câmera e microfone para a teleconsulta
**Arquivo:** `android/app/src/main/AndroidManifest.xml`

**Problema:** o app só declarava a permissão `INTERNET`. Quando a tela de consulta pedia câmera ou microfone, o Android recusava sem nem perguntar ao usuário. O paciente entrava na consulta sem vídeo e sem áudio.

**O que foi feito:** foram declaradas as permissões `CAMERA`, `RECORD_AUDIO` e `MODIFY_AUDIO_SETTINGS`. O Capacitor já pede a autorização ao usuário no momento em que a página precisa dela, sem pedir nada ao abrir o app.

Também foram declarados `uses-feature` de câmera e microfone com `required="false"`. Sem isso, a Play Store esconderia o app de aparelhos sem câmera, como alguns tablets.

### 2. Botão "voltar" do Android
**Arquivo:** `android/app/src/main/java/tech/norte1/plenuscare/MainActivity.java`

**Problema:** o Capacitor não trata o botão voltar. Em qualquer tela do sistema, apertar voltar fechava o app inteiro em vez de voltar uma página. Esse é um dos motivos mais comuns de avaliação negativa na loja.

**O que foi feito:** agora o botão voltar volta uma página no sistema. Na primeira tela, ele manda o app para segundo plano, que é o comportamento padrão do Android.

O plugin oficial `@capacitor/app` foi testado e descartado. Na primeira tela, o botão voltar dele não faz nada.

### 3. Download de arquivos
**Arquivo:** `MainActivity.java`

**Problema:** a WebView não baixa arquivos sozinha. Botões que baixam documentos (por exemplo, a rota `documents/download`) não faziam nada no app.

**O que foi feito:** os downloads agora são repassados para o gerenciador de downloads do Android. O arquivo vai para a pasta **Downloads** e aparece uma notificação quando termina. Os cookies de login são enviados junto, para o servidor liberar o arquivo.

Até o Android 9, gravar na pasta Downloads exige permissão. Por isso foi declarada `WRITE_EXTERNAL_STORAGE` com `maxSdkVersion="28"`, que só vale até o Android 9.

**Limitação conhecida:** arquivos gerados dentro da própria página (links `blob:` ou `data:`) não podem ser baixados por esse caminho. Nesses casos, o app mostra o aviso "Não foi possível baixar este arquivo pelo app".

### 4. Backup dos dados do app desativado
**Arquivos:** `AndroidManifest.xml` e `android/app/src/main/res/xml/data_extraction_rules.xml` (novo)

**Problema:** com `allowBackup="true"`, a sessão logada e o cache de um app de saúde iam para o backup do Google do usuário e eram copiados para um celular novo.

**O que foi feito:**
- `allowBackup="false"` impede o backup até o Android 11.
- O `data_extraction_rules.xml` impede o backup e a cópia para outro aparelho no Android 12 ou mais novo.

Isso também simplifica a resposta sobre criptografia e armazenamento no formulário Data Safety.

### 5. Ordem do manifesto
**Arquivo:** `AndroidManifest.xml`

As permissões foram movidas para antes do bloco `<application>`, como o lint do Android pede. Não muda o funcionamento.

### 6. Domínio que sobrou do molde
**Arquivo:** `capacitor.config.json`

O domínio `sayan.bemestar.1norte.tech`, de outro cliente, foi removido da lista de navegação permitida. O Plenus Care continua coberto por `*.bemestar.1norte.tech`.

### Já feito antes (mesmo dia)
- **Assinatura automática do release:** o `build.gradle` lê a chave de `android/keystore.properties`.
- **`.gitignore` atualizado:** o `keystore.properties` e os arquivos `.jks`/`.keystore` ficam fora do git. Há um modelo em `android/keystore.properties.example`.

## Resultado das verificações

| Verificação | Resultado |
|---|---|
| ESLint e Prettier (código web) | sem erros |
| `npx cap doctor` | ok |
| Dependências que vão no app (`npm audit --omit=dev`) | 0 vulnerabilidades |
| Lint de release do Android | **0 erros**, 21 avisos cosméticos (ícones e recursos não usados) |
| Build do `.aab` | ok, assinado com a chave `key_app_android` (SHA-256 `60:41:83:…:18:28`) |
| Páginas de memória de 16 KB | ok, o app não tem bibliotecas nativas |
| App em modo debug | não está |
| Servidor de produção | HTTP 200, HTTPS válido, HTTP redireciona para HTTPS |

O `npm audit` completo aponta 3 vulnerabilidades moderadas no pacote `uuid`. Elas vêm da ferramenta de linha de comando do Capacitor, usada só no computador do desenvolvedor, e **não vão para o app**.

## Como testar no celular (antes do commit)

Use um celular real e uma conta de **beneficiário**:

1. [ ] O app abre, mostra a tela de carregamento e entra no login.
2. [ ] **Botão voltar:** navegue por 2 ou 3 telas e aperte voltar. Ele deve voltar uma página por vez. Na primeira tela, o app deve ir para segundo plano.
3. [ ] **Teleconsulta:** entre numa sala de consulta. O app deve pedir câmera e microfone. Depois de autorizar, o vídeo e o áudio devem funcionar.
4. [ ] **Selfie e encaminhamento:** ao enviar um arquivo, deve aparecer a opção de usar a câmera.
5. [ ] **Download:** baixe um documento ou receita. Deve aparecer a notificação, e o arquivo deve ficar na pasta Downloads.
6. [ ] **Sem internet:** abra o app em modo avião. Deve aparecer a tela "Sem conexão".

## Próximos passos

1. Testar no Android Studio com o checklist acima.
2. Fazer o commit.
3. Gerar o `.aab`: **Build → Generate Signed App Bundle or APK → Android App Bundle**, com a chave `C:\Informações_APK\APK_Info` e a variante `release`. O versionCode continua `1`, porque esta versão ainda não foi enviada ao Google.

## Pendências no Play Console (não dependem do código)

- **Declaração de app de saúde:** obrigatória, porque o app tem teleconsulta e dados médicos.
- **Data Safety:**
  - dados a declarar: dados pessoais, dados de saúde, fotos, áudio e vídeo;
  - serviços que processam esses dados: Agora (vídeo), Memed (receitas), Rapidoc e Communicare;
  - os dados são criptografados em trânsito (HTTPS).
- **Justificativa das permissões:**
  - câmera e microfone: "teleconsulta por vídeo e envio de selfie/documentos";
  - armazenamento: "salvar documentos baixados, apenas até o Android 9".
- **Política de privacidade:** https://plenuscare.bemestar.1norte.tech/politica-de-privacidade
- **Exclusão de conta:** confirmar com a 1Norte se o paciente pode criar conta pelo app. Se puder, o Google exige uma opção de exclusão e uma URL pública para isso.
- **Conta de teste para o revisor:** um beneficiário com acesso a uma consulta.
- **Texto do rodapé:** confirmar que a troca "Sistema PGB" → "Volpato Connect" (`www/branding.js`) está autorizada em contrato.
- **Certificado HTTPS do servidor:** vence em 20/11/2026. A 1Norte deve confirmar que a renovação é automática.
- **Imagens da loja:** ícone 512×512, feature graphic 1024×500 e screenshots ainda precisam ser feitos.
