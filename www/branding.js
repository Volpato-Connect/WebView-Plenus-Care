// Cria uma função que é executada imediatamente
(() => {
  // Verifica se o script já foi executado
  if (window.__vcBranding) return;

  // Marca que o script já foi inicializado
  window.__vcBranding = true;

  // Expressão regular que identifica o texto antigo
  const ANTIGO = /Sistema PGB\s*-\s*Plataforma de Gestão de Benefícios\s*©\s*\d{4}/;

  // Define o novo texto com o ano atual
  const NOVO = `Sistema Desenvolvido pela Volpato Connect © ${new Date().getFullYear()}`;

  // Controla se já existe uma execução agendada
  let agendado = false;

  // Função responsável por localizar e substituir o texto
  const trocar = () => {
    // Libera o próximo agendamento
    agendado = false;

    // Encerra a função se o body ainda não existir
    if (!document.body) return;

    // Cria um percurso para encontrar os nós de texto
    const walker = document.createTreeWalker(document.body, NodeFilter.SHOW_TEXT);

    // Variável que armazenará cada nó encontrado
    let node;

    // Percorre todos os nós de texto da página
    while ((node = walker.nextNode())) {
      // Verifica se o texto corresponde ao padrão antigo
      if (ANTIGO.test(node.nodeValue)) {
        // Substitui o texto antigo pelo novo
        node.nodeValue = node.nodeValue.replace(ANTIGO, NOVO);
      }
    }
  };

  // Função responsável por agendar a substituição
  const agendar = () => {
    // Evita agendar várias execuções simultaneamente
    if (agendado) return;

    // Indica que já existe uma execução agendada
    agendado = true;

    // Agenda a função trocar para o próximo ciclo de renderização
    requestAnimationFrame(trocar);
  };

  // Executa a substituição inicial
  trocar();

  // Observa alterações no HTML e agenda novas substituições
  new MutationObserver(agendar).observe(
    // Define o elemento raiz que será observado
    document.documentElement,

    {
      // Detecta a adição ou remoção de elementos
      childList: true,

      // Inclui todos os elementos descendentes na observação
      subtree: true,

      // Detecta alterações nos conteúdos dos nós de texto
      characterData: true,
    }
  );

  // Executa a função principal imediatamente
})();
