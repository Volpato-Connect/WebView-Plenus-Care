package tech.norte1.plenuscare; // Define o identificador único do pacote do aplicativo Android

// Importações de dependências do Capacitor e do sistema Android
import com.getcapacitor.BridgeActivity; // Classe base do Capacitor que gerencia a tela do app
import android.net.Uri;                 // Classe para manipulação e análise de URLs
import android.os.Bundle;              // Usado para passar dados entre estados da aplicação
import android.webkit.WebView;          // Componente que renderiza a interface web (HTML/JS)
import com.getcapacitor.BridgeWebViewClient; // Gerenciador de eventos da WebView do Capacitor
import java.io.ByteArrayOutputStream;   // Fluxo para armazenar dados em memória temporária
import java.io.InputStream;             // Fluxo para leitura de arquivos de entrada
import android.Manifest;                // Nomes das permissões do Android
import android.app.DownloadManager;     // Serviço do sistema que baixa arquivos e mostra a notificação
import android.content.pm.PackageManager; // Usado para verificar se uma permissão foi concedida
import android.os.Build;                // Informa a versão do Android do aparelho
import android.os.Environment;          // Caminho da pasta pública "Downloads"
import android.webkit.CookieManager;    // Lê os cookies de sessão da WebView (login)
import android.webkit.URLUtil;          // Descobre o nome do arquivo a partir do link
import android.widget.Toast;            // Mensagens curtas na parte de baixo da tela
import androidx.activity.OnBackPressedCallback; // Trata o botão "voltar" do Android
import androidx.core.app.ActivityCompat; // Pede permissões ao usuário
import androidx.core.content.ContextCompat; // Verifica permissões já concedidas

public class MainActivity extends BridgeActivity {
    // Variável global para armazenar o conteúdo em texto do script JavaScript
    private String brandingJs = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Inicializa a tela e os componentes padrão do Capacitor
        super.onCreate(savedInstanceState);

        // Bloco try-with-resources que tenta abrir e ler o arquivo 'branding.js' da pasta assets
        try (InputStream in = getAssets().open("public/branding.js")) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096]; // Buffer de 4KB para ler o arquivo em blocos
            int n;
            
            // Loop que lê o arquivo até o fim e escreve na memória temporária (out)
            while ((n = in.read(buf)) != -1) out.write(buf, 0, n);
            
            // Converte o arquivo lido para texto usando a codificação UTF-8
            brandingJs = out.toString("UTF-8");
        } catch (Exception e) {
            // Caso dê erro ao ler o arquivo, registra uma mensagem de falha no Logcat do Android
            android.util.Log.e("Branding", "Falha ao ler branding.js", e);
        }

        // Customiza o comportamento da WebView do Capacitor quando as páginas são carregadas
        bridge.setWebViewClient(new BridgeWebViewClient(bridge) {
            @Override
            public void onPageFinished(WebView view, String url) {
                // Mantém o comportamento padrão do Capacitor ao terminar de carregar uma página
                super.onPageFinished(view, url);
                
                // Extrai o domínio (host) da URL atual de forma segura
                String host = url == null ? null : Uri.parse(url).getHost();
                
                // Se o domínio for válido e terminar com ".1norte.tech", injeta o código JavaScript salvo
                if (host != null && host.endsWith(".1norte.tech")) {
                    view.evaluateJavascript(brandingJs, null);
                }
            }
        });

        // Botão "voltar": volta uma página no sistema; na primeira tela, manda o app para segundo plano
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                WebView webView = bridge.getWebView();
                if (webView.canGoBack()) {
                    webView.goBack();
                } else {
                    moveTaskToBack(true);
                }
            }
        });

        // Downloads: a WebView não baixa arquivos sozinha, então repassa o link para o DownloadManager
        bridge.getWebView().setDownloadListener((url, userAgent, contentDisposition, mimeType, contentLength) ->
            baixarArquivo(url, userAgent, contentDisposition, mimeType)
        );
    }

    private void baixarArquivo(String url, String userAgent, String contentDisposition, String mimeType) {
        // Arquivos gerados dentro da página (blob:/data:) não têm endereço para o DownloadManager buscar
        if (!URLUtil.isHttpUrl(url) && !URLUtil.isHttpsUrl(url)) {
            Toast.makeText(this, "Não foi possível baixar este arquivo pelo app.", Toast.LENGTH_LONG).show();
            return;
        }

        // Até o Android 9 é preciso permissão para gravar na pasta Downloads
        if (
            Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.WRITE_EXTERNAL_STORAGE }, 1);
            Toast.makeText(this, "Permita o acesso e toque em baixar novamente.", Toast.LENGTH_LONG).show();
            return;
        }

        try {
            String nome = URLUtil.guessFileName(url, contentDisposition, mimeType);

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mimeType);
            // Envia os cookies do login para o servidor liberar o arquivo
            request.addRequestHeader("Cookie", CookieManager.getInstance().getCookie(url));
            request.addRequestHeader("User-Agent", userAgent);
            request.setTitle(nome);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, nome);

            DownloadManager dm = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            dm.enqueue(request);
            Toast.makeText(this, "Baixando " + nome + "...", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            android.util.Log.e("Download", "Falha ao baixar " + url, e);
            Toast.makeText(this, "Não foi possível baixar o arquivo.", Toast.LENGTH_LONG).show();
        }
    }
}
