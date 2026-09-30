(function () {
  var cfg = window.APP_CONFIG || {};
  var jaIniciou = false;

  // troca qual das duas telas está visível
  function mostrar(id) {
    document.getElementById("loading").classList.add("oculto");
    document.getElementById("offline").classList.add("oculto");
    document.getElementById(id).classList.remove("oculto");
  }

  // sai da nossa tela e entra no sistema da 1Norte
  function abrirSistema() {
    if (!navigator.onLine) {
      mostrar("offline");
      return;
    }
    window.location.replace(cfg.SYSTEM_URL);
  }

  function iniciar() {
    if (jaIniciou) return;
    jaIniciou = true;

    // esconde a splash nativa
    if (window.Capacitor && Capacitor.Plugins.SplashScreen) {
      Capacitor.Plugins.SplashScreen.hide();
    }

    setTimeout(abrirSistema, 600);
  }

  // o app nativo avisa quando está pronto
  document.addEventListener("deviceready", iniciar, false);

  // rede de segurança: se o aviso não vier em 3 s, segue assim mesmo
  setTimeout(iniciar, 3000);

  document.getElementById("retry").addEventListener("click", abrirSistema);
})();
