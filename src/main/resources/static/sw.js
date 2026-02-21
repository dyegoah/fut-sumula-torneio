const CACHE_NAME = 'futsumula-torneio-v1';

// Arquivos básicos que o app vai salvar no celular para abrir mais rápido
const urlsToCache = [
  '/',
  '/login.html',
  '/css/style.css',
  '/manifest.json'
];

// Instalando o Service Worker
self.addEventListener('install', event => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(cache => {
        return cache.addAll(urlsToCache);
      })
  );
});

// Interceptando as requisições (Tenta pegar da rede, se cair a internet, pega do cache)
self.addEventListener('fetch', event => {
  // Ignora chamadas de API (elas precisam de internet sempre)
  if (event.request.url.includes('/api/')) {
      return;
  }
  
  event.respondWith(
    fetch(event.request).catch(() => {
      return caches.match(event.request);
    })
  );
});