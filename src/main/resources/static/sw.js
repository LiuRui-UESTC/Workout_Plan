const CACHE_NAME = 'ruifei-fitness-v1';
const STATIC_ASSETS = [
    '/',
    '/css/style.css',
    '/js/app.js'
];

self.addEventListener('install', (event) => {
    event.waitUntil(
        caches.open(CACHE_NAME)
            .then(cache => cache.addAll(STATIC_ASSETS))
    );
    self.skipWaiting();
});

self.addEventListener('activate', (event) => {
    event.waitUntil(
        caches.keys().then(keys =>
            Promise.all(keys.filter(k => k !== CACHE_NAME).map(k => caches.delete(k)))
        )
    );
    self.clients.claim();
});

self.addEventListener('fetch', (event) => {
    if (event.request.url.includes('/api/')) {
        // API 请求走网络优先
        event.respondWith(
            fetch(event.request).catch(() => caches.match(event.request))
        );
    } else {
        // 静态资源走缓存优先
        event.respondWith(
            caches.match(event.request).then(cached => cached || fetch(event.request))
        );
    }
});
