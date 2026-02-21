/**
 * GERENCIADOR GLOBAL (IDIOMA, GÊNERO E TEMA)
 * Controla: Traduções (PT/EN/ES), Gênero (Masculino/Feminino) e Cores.
 */

// --- DICIONÁRIO DE TRADUÇÃO INTELIGENTE ---
const DICIONARIO = {
    // MENU E SISTEMA
    'bem_vindo': {
        pt: { male: 'BEM-VINDO AO JOGO', female: 'BEM-VINDA AO JOGO' },
        en: { male: 'WELCOME TO THE GAME', female: 'WELCOME TO THE GAME' },
        es: { male: 'BIENVENIDO AL JUEGO', female: 'BIENVENIDA AL JUEGO' }
    },
    'o_que_jogar': {
        pt: { male: 'O QUE VAMOS JOGAR HOJE?', female: 'O QUE VAMOS JOGAR HOJE?' },
        en: { male: 'WHAT ARE WE PLAYING TODAY?', female: 'WHAT ARE WE PLAYING TODAY?' },
        es: { male: '¿QUÉ JUGAREMOS HOY?', female: '¿QUÉ JUGAREMOS HOY?' }
    },
    'o_organizador': {
        pt: { male: 'ORGANIZADOR', female: 'ORGANIZADORA' },
        en: { male: 'ORGANIZER', female: 'ORGANIZER' },
        es: { male: 'ORGANIZADOR', female: 'ORGANIZADORA' }
    },
    'sua_assinatura': {
        pt: { male: 'SUA ASSINATURA', female: 'SUA ASSINATURA' },
        en: { male: 'YOUR SUBSCRIPTION', female: 'YOUR SUBSCRIPTION' },
        es: { male: 'TU SUSCRIPCIÓN', female: 'TU SUSCRIPCIÓN' }
    },
    'verificando': {
        pt: { male: 'VERIFICANDO...', female: 'VERIFICANDO...' },
        en: { male: 'VERIFYING...', female: 'VERIFYING...' },
        es: { male: 'VERIFICANDO...', female: 'VERIFICANDO...' }
    },
    
    // BOTÕES DO MENU
    'btn_config': {
        pt: { male: 'Configuração', female: 'Configuração' },
        en: { male: 'Settings', female: 'Settings' },
        es: { male: 'Configuración', female: 'Configuración' }
    },
    'btn_assinar': {
        pt: { male: 'Assinar Sistema', female: 'Assinar Sistema' },
        en: { male: 'Subscribe Now', female: 'Subscribe Now' },
        es: { male: 'Suscribirse', female: 'Suscribirse' }
    },
    'btn_ajuda': {
        pt: { male: 'Ajuda', female: 'Ajuda' },
        en: { male: 'Help', female: 'Help' },
        es: { male: 'Ayuda', female: 'Ayuda' }
    },
    'btn_sair': {
        pt: { male: 'Sair', female: 'Sair' },
        en: { male: 'Logout', female: 'Logout' },
        es: { male: 'Salir', female: 'Salir' }
    },

    // CARDS PRINCIPAIS
    'card_iniciar': {
        pt: { male: 'INICIAR PARTIDA', female: 'INICIAR PARTIDA' },
        en: { male: 'QUICK MATCH', female: 'QUICK MATCH' },
        es: { male: 'INICIAR PARTIDO', female: 'INICIAR PARTIDO' }
    },
    'desc_iniciar': {
        pt: { male: 'Acesse os torneios ativos e vá para o campo.', female: 'Acesse os torneios ativos e vá para o campo.' },
        en: { male: 'Access active tournaments and go to the field.', female: 'Access active tournaments and go to the field.' },
        es: { male: 'Accede a torneos activos y ve al campo.', female: 'Accede a torneos activos y ve al campo.' }
    },
    'card_torneio': {
        pt: { male: 'MODO TORNEIO', female: 'MODO TORNEIO' },
        en: { male: 'TOURNAMENT MODE', female: 'TOURNAMENT MODE' },
        es: { male: 'MODO TORNEO', female: 'MODO TORNEO' }
    },
    'desc_torneio': {
        pt: { male: 'Gerencie ligas, copas e mata-mata.', female: 'Gerencie ligas, copas e mata-mata.' },
        en: { male: 'Manage leagues, cups and brackets.', female: 'Manage leagues, cups and brackets.' },
        es: { male: 'Gestiona ligas, copas y eliminatorias.', female: 'Gestiona ligas, copas y eliminatorias.' }
    },

    // TERMOS ESPECÍFICOS DE GÊNERO
    'novo_clube': {
        pt: { male: 'NOVO CLUBE', female: 'NOVO CLUBE' },
        en: { male: 'NEW CLUB', female: 'NEW CLUB' },
        es: { male: 'NUEVO CLUB', female: 'NUEVO CLUB' }
    },
    'novo_craque': { 
        pt: { male: 'NOVO CRAQUE', female: 'NOVA CRAQUE' },
        en: { male: 'NEW STAR', female: 'NEW STAR' }, // Inglês neutro
        es: { male: 'NUEVO CRACK', female: 'NUEVA CRACK' }
    },
    'jogador': {
        pt: { male: 'JOGADOR', female: 'JOGADORA' },
        en: { male: 'PLAYER', female: 'PLAYER' },
        es: { male: 'JUGADOR', female: 'JUGADORA' }
    },
    'goleiro': {
        pt: { male: 'GOLEIRO', female: 'GOLEIRA' },
        en: { male: 'GOALKEEPER', female: 'GOALKEEPER' },
        es: { male: 'PORTERO', female: 'PORTERA' }
    },
    'artilheiro': {
        pt: { male: 'ARTILHEIRO', female: 'ARTILHEIRA' },
        en: { male: 'TOP SCORER', female: 'TOP SCORER' },
        es: { male: 'GOLEADOR', female: 'GOLEADORA' }
    }
};

const TEMAS = {
    male: {
        // AZUL NEON (Padrão)
        '--primary-neon': '#00eaff',
        '--primary-neon-dim': 'rgba(0, 234, 255, 0.1)',
        '--secondary-neon': '#0088ff',
        '--neon-blue': '#00eaff',
        
        // IMAGENS MASCULINAS / GENÉRICAS
        '--img-stadium': "url('https://images.unsplash.com/photo-1522778119026-d647f0565c6d?q=80&w=1920&auto=format&fit=crop')",
        '--img-trophy': "url('https://images.unsplash.com/photo-1579952363873-27f3bade9f55?q=80&w=1920&auto=format&fit=crop')",
        '--img-fans': "url('https://images.unsplash.com/photo-1518605348435-2aa9e13993d6?q=80&w=1920&auto=format&fit=crop')",
    },
    female: {
        // LILÁS / ROSA (Feminino Moderno)
        '--primary-neon': '#d946ef', 
        '--primary-neon-dim': 'rgba(217, 70, 239, 0.2)',
        '--secondary-neon': '#a855f7', 
        '--neon-blue': '#e879f9',
        
        // IMAGENS FEMININAS
        '--img-stadium': "url('https://images.unsplash.com/photo-1551966775-a4ddc8df052b?q=80&w=1920&auto=format&fit=crop')", 
        '--img-trophy': "url('https://images.unsplash.com/photo-1629901925121-8a141c2a42f4?q=80&w=1920&auto=format&fit=crop')",
        '--img-fans': "url('https://images.unsplash.com/photo-1628891890467-b79f2c8ba9dc?q=80&w=1920&auto=format&fit=crop')",
    }
};

// --- FUNÇÕES DE CONTROLE ---

function setPreferencias(idioma, genero) {
    if(idioma) localStorage.setItem('futSumulaLang', idioma);
    if(genero) localStorage.setItem('futSumulaGender', genero);
    aplicarPreferencias();
}

function aplicarPreferencias() {
    // 1. Recupera valores (ou define padrão PT / MALE)
    const lang = localStorage.getItem('futSumulaLang') || 'pt';
    const gender = localStorage.getItem('futSumulaGender') || 'male';
    
    const root = document.documentElement;
    const temaAtual = TEMAS[gender];

    // 2. Aplica CORES e IMAGENS (CSS Variables)
    for (const [prop, valor] of Object.entries(temaAtual)) {
        root.style.setProperty(prop, valor);
    }

    // 3. Aplica TRADUÇÕES (Textos)
    const elementos = document.querySelectorAll('[data-ref]');
    elementos.forEach(el => {
        const chave = el.getAttribute('data-ref');
        
        // Verifica se a chave existe no dicionário
        if (DICIONARIO[chave]) {
            // Tenta pegar a tradução exata (Lang + Gender)
            // Se não tiver a língua, cai para PT
            const traducaoLingua = DICIONARIO[chave][lang] || DICIONARIO[chave]['pt'];
            const textoFinal = traducaoLingua[gender];
            
            // Mantém formatação HTML se houver (ex: <br>) ou apenas texto
            if(el.childElementCount > 0 && chave === 'o_que_jogar') {
                 // Caso especial para títulos com quebra de linha (mantém a estrutura se necessário)
                 // Para simplicidade, aqui substituímos o texto todo.
                 // Se quiser preservar o <br>, teria que fazer um replace mais complexo.
                 // No seu caso do index, o <br> está no meio do <h1>.
                 // Vamos simplificar e inserir o texto direto ou tratar chaves compostas.
            }
            
            el.innerText = textoFinal;
        }
    });
}

// 4. Helper para uso em JavaScript (alertas, etc)
function getTexto(chave) {
    const lang = localStorage.getItem('futSumulaLang') || 'pt';
    const gender = localStorage.getItem('futSumulaGender') || 'male';
    if (DICIONARIO[chave]) {
        return (DICIONARIO[chave][lang] || DICIONARIO[chave]['pt'])[gender];
    }
    return chave;
}

// Executa ao carregar
document.addEventListener("DOMContentLoaded", aplicarPreferencias);