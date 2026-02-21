// ATENÇÃO: Toda vez que você fizer uma mudança no layout (HTML/CSS/JS) e quiser
// que o celular dos clientes atualize, mude este número (Ex: v2, v3, v4...)
const CACHE_NAME = 'futsumula-torneio-v2';

// Arquivos que ficam salvos na memória do celular
const urlsToCache = [
  '/',
  '/login.html',
  '/index.html',
  '/css/style.css',
  '/manifest.json'
];

// 1. Instalação: Baixa os arquivos e guarda no cache
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        return cache.addAll(urlsToCache);
      })
  );
  // Força o Service Worker a assumir o controle imediatamente, sem esperar o app fechar
  self.skipWaiting(); 
});

// 2. Ativação: A MÁGICA DA ATUALIZAÇÃO! Apaga o cache velho se a versão mudou.
self.addEventListener('activate', event => {
  event.waitUntil(
    caches.keys().then(cacheNames => {
      return Promise.all(
        cacheNames.map(cache => {
          if (cache !== CACHE_NAME) {
            console.log('Apagando cache antigo:', cache);
            return caches.delete(cache);
          }
        })
      );
    })
  );
  // Garante que as abas abertas já usem a versão nova
  self.clients.claim();
});

// 3. Interceptação: Tenta a internet, se falhar, usa o cache
self.addEventListener('fetch', event => {
  // Ignora o cache para as chamadas de banco de dados (API)
  if (event.request.url.includes('/api/')) {
      return;
  }
  
  event.respondWith(
    // Tenta buscar sempre a versão mais fresca da internet primeiro (Network First)
    fetch(event.request).catch(() => {
      // Se estiver sem internet (Modo Avião), devolve a tela salva no cache
      return caches.match(event.request);
    })
  );
});