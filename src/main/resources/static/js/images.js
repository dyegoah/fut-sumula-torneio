/* ===========================================================
   CENTRAL DE IMAGENS - HIGITECH FUT-SÚMULA
   =========================================================== */

// Usamos 'var' em vez de 'const' para evitar travamento se o arquivo for carregado 2x por acidente
var AppImages = {
    // 1. Dashboard (Estádio Geral)
    stadium: "url('https://images.unsplash.com/photo-1518091043644-c1d4457512c6?q=80&w=1920&auto=format&fit=crop')",

    // 2. Cadastro (Estúdio/Fundo Tech)
    studio: "url('https://images.unsplash.com/photo-1516247161927-4b77d2524d77?q=80&w=1920&auto=format&fit=crop')",

    // 3. Plantel Jogadores (Coletiva de Imprensa)
    press: "url('https://images.unsplash.com/photo-1496337589254-7e19d01cec44?q=80&w=1920&auto=format&fit=crop')",

    // 4. Detalhes Táticos (Prancheta/Campo)
    tactics: "url('https://images.unsplash.com/photo-1555862124-a4ebae223956?q=80&w=1920&auto=format&fit=crop')",

    // 5. LIGA DE CLUBES (Ônibus/Viagem ou Estádio)
    bus: "url('https://images.unsplash.com/photo-1522778119026-d647f0565c6d?q=80&w=1920&auto=format&fit=crop')",
    
    // 6. TROFÉU / TORNEIO (Fundo Dourado)
    trophy: "url('https://images.unsplash.com/photo-1614632537423-1e6c2e7e0aab?q=80&w=1920&auto=format&fit=crop')",

    // 7. Placeholders (Imagens Padrão quando não tem foto)
    defaultAvatar: "https://cdn-icons-png.flaticon.com/512/21/21104.png",
    defaultShield: "https://cdn-icons-png.flaticon.com/512/21/21104.png"
};

// --- APLICAÇÃO AUTOMÁTICA NAS VARIÁVEIS CSS (DEFAULTS) ---
// Define as imagens padrão no CSS root para serem usadas antes do gender-mode carregar
if (document.documentElement) {
    document.documentElement.style.setProperty('--img-stadium', AppImages.stadium);
    document.documentElement.style.setProperty('--img-studio', AppImages.studio);
    document.documentElement.style.setProperty('--img-press', AppImages.press);
    document.documentElement.style.setProperty('--img-tactics', AppImages.tactics);
    document.documentElement.style.setProperty('--img-bus', AppImages.bus);
    document.documentElement.style.setProperty('--img-trophy', AppImages.trophy);
}