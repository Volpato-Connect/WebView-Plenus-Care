package tech.norte1.plenuscare; // Define o identificador único do pacote do aplicativo Android

// Importações de dependências do Capacitor e do sistema Android
import com.getcapacitor.BridgeActivity; // Classe base do Capacitor que gerencia a tela do app
import android.net.Uri;                 // Classe para manipulação e análise de URLs
import android.os.Bundle;              // Usado para passar dados entre estados da aplicação
import android.webkit.WebView;          // Componente que renderiza a interface web (HTML/JS)
import com.getcapacitor.BridgeWebViewClient; // Gerenciador de eventos da WebView do Capacitor
import java.io.ByteArrayOutputStream;   // Fluxo para armazenar dados em memória temporária
import java.io.InputStream;             // Fluxo para leitura de arquivos de entrada

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
    }
}
