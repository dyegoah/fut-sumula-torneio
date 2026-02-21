// Dicionário Estendido com mais de 100 opções (Animais, Engraçados, Tenebrosos, Objetos)
window.ExtendedMascots = {
    // Clássicos / Animais
    'lion': 'fa-paw', 'eagle': 'fa-feather-alt', 'shark': 'fa-fish', 'wolf': 'fa-dog', 'dragon': 'fa-dragon',
    'bull': 'fa-bullhorn', 'horse': 'fa-horse', 'spider': 'fa-spider', 'cat': 'fa-cat', 'crow': 'fa-crow',
    'frog': 'fa-frog', 'hippo': 'fa-hippo', 'otter': 'fa-otter', 'dove': 'fa-dove', 'kiwi': 'fa-kiwi-bird', 'worm': 'fa-worm', 
    
    // Tenebrosos / Assustadores
    'skull': 'fa-skull', 'ghost': 'fa-ghost', 'radiation': 'fa-radiation', 'biohazard': 'fa-biohazard', 
    'disease': 'fa-disease', 'bone': 'fa-bone', 'skull-crossbones': 'fa-skull-crossbones', 'spider-web': 'fa-spider',
    
    // Engraçados / Divertidos
    'poo': 'fa-poop', 'alien': 'fa-pastafarianism', 'snowman': 'fa-snowman', 'robot': 'fa-robot', 
    'smile-wink': 'fa-smile-wink', 'grin-tongue': 'fa-grin-tongue', 'dizzy': 'fa-dizzy', 'grimace': 'fa-grimace',
    'laugh-beam': 'fa-laugh-beam', 'sad-cry': 'fa-sad-cry', 'angry': 'fa-angry', 'restroom': 'fa-restroom',
    
    // Fantasia / Heróis
    'ninja': 'fa-user-ninja', 'astronaut': 'fa-user-astronaut', 'secret': 'fa-user-secret', 'mask': 'fa-mask', 
    'hat-wizard': 'fa-hat-wizard', 'hat-cowboy': 'fa-hat-cowboy', 'jedi': 'fa-jedi', 'khanda': 'fa-khanda',
    
    // Objetos e Poder
    'bolt': 'fa-bolt', 'star': 'fa-star', 'anchor': 'fa-anchor', 'leaf': 'fa-leaf', 'cannabis': 'fa-cannabis', 
    'bomb': 'fa-bomb', 'fire': 'fa-fire', 'fire-alt': 'fa-fire-alt', 'burn': 'fa-burn', 'crown': 'fa-crown',
    'gem': 'fa-gem', 'hammer': 'fa-hammer', 'gavel': 'fa-gavel', 'magnet': 'fa-magnet', 'rocket': 'fa-rocket', 
    'shield-virus': 'fa-shield-virus', 'meteor': 'fa-meteor', 'tooth': 'fa-tooth', 'eye': 'fa-eye', 'brain': 'fa-brain', 
    'hand-fist': 'fa-hand-fist', 'hand-middle-finger': 'fa-hand-middle-finger',
    
    // Veículos e Guerra
    'fighter-jet': 'fa-fighter-jet', 'helicopter': 'fa-helicopter', 'motorcycle': 'fa-motorcycle', 
    'truck-monster': 'fa-truck-monster', 'tractor': 'fa-tractor',
    
    // Jogos e Esportes
    'chess-knight': 'fa-chess-knight', 'chess-rook': 'fa-chess-rook', 'chess-king': 'fa-chess-king', 
    'chess-queen': 'fa-chess-queen', 'dice-d20': 'fa-dice-d20', 'gamepad': 'fa-gamepad', 'headset': 'fa-headset', 
    'bowling-ball': 'fa-bowling-ball', 'baseball-ball': 'fa-baseball-ball', 'basketball-ball': 'fa-basketball-ball', 
    'volleyball-ball': 'fa-volleyball-ball', 'table-tennis': 'fa-table-tennis', 'hockey-puck': 'fa-hockey-puck', 
    'quidditch': 'fa-quidditch', 'dumbbell': 'fa-dumbbell', 'weight-hanging': 'fa-weight-hanging',
    
    // Natureza e Clima
    'cloud-showers-heavy': 'fa-cloud-showers-heavy', 'snowflake': 'fa-snowflake', 'wind': 'fa-wind', 
    'tornado': 'fa-tornado', 'water': 'fa-water', 'droplet': 'fa-tint', 'tree': 'fa-tree', 'seedling': 'fa-seedling', 
    'globe': 'fa-globe', 'moon': 'fa-moon', 'sun': 'fa-sun', 'campground': 'fa-campground',
    
    // Mídia
    'music': 'fa-music', 'guitar': 'fa-guitar', 'drum': 'fa-drum', 'camera-retro': 'fa-camera-retro', 
    'video': 'fa-video', 'film': 'fa-film', 'ticket-alt': 'fa-ticket-alt', 'bullseye': 'fa-bullseye'
};

// Função que cria o Modal Dinamicamente na tela
window.abrirModalMascotesExtra = function(callbackOnSelect) {
    // Remove modal anterior se existir
    const oldModal = document.getElementById('modal-mascotes-extra');
    if (oldModal) oldModal.remove();

    const overlay = document.createElement('div');
    overlay.id = 'modal-mascotes-extra';
    overlay.style.position = 'fixed';
    overlay.style.top = '0'; overlay.style.left = '0';
    overlay.style.width = '100%'; overlay.style.height = '100%';
    overlay.style.background = 'rgba(0,0,0,0.9)';
    overlay.style.zIndex = '99999';
    overlay.style.display = 'flex';
    overlay.style.alignItems = 'center'; overlay.style.justifyContent = 'center';
    overlay.style.backdropFilter = 'blur(10px)';

    const box = document.createElement('div');
    box.style.background = '#111';
    box.style.border = '1px solid var(--primary-neon, #00ff88)';
    box.style.borderRadius = '15px';
    box.style.width = '90%'; box.style.maxWidth = '600px';
    box.style.maxHeight = '80vh';
    box.style.display = 'flex'; box.style.flexDirection = 'column';

    const header = document.createElement('div');
    header.style.padding = '15px 20px';
    header.style.borderBottom = '1px solid rgba(255,255,255,0.1)';
    header.style.display = 'flex'; header.style.justifyContent = 'space-between';
    header.innerHTML = '<h3 style="color:#fff; margin:0;"><i class="fas fa-paw" style="color:var(--primary-neon, #00ff88);"></i> GALERIA DE MASCOTES</h3><i class="fas fa-times" style="color:#ff4444; cursor:pointer; font-size:1.5rem;"></i>';
    
    header.querySelector('.fa-times').onclick = () => overlay.remove();

    const grid = document.createElement('div');
    grid.style.padding = '20px';
    grid.style.display = 'grid';
    grid.style.gridTemplateColumns = 'repeat(auto-fill, minmax(60px, 1fr))';
    grid.style.gap = '15px';
    grid.style.overflowY = 'auto';

    Object.keys(window.ExtendedMascots).forEach(key => {
        const iconClass = window.ExtendedMascots[key];
        const btn = document.createElement('div');
        btn.style.background = 'rgba(255,255,255,0.05)';
        btn.style.border = '1px solid rgba(255,255,255,0.1)';
        btn.style.borderRadius = '8px';
        btn.style.height = '60px';
        btn.style.display = 'flex'; btn.style.alignItems = 'center'; btn.style.justifyContent = 'center';
        btn.style.cursor = 'pointer'; btn.style.transition = '0.2s';
        btn.innerHTML = `<i class="fas ${iconClass}" style="font-size:1.8rem; color:#fff;"></i>`;
        
        btn.onmouseover = () => { btn.style.background = 'var(--primary-neon, #00ff88)'; btn.querySelector('i').style.color = '#000'; };
        btn.onmouseout = () => { btn.style.background = 'rgba(255,255,255,0.05)'; btn.querySelector('i').style.color = '#fff'; };
        
        btn.onclick = () => {
            callbackOnSelect(key);
            overlay.remove();
        };
        grid.appendChild(btn);
    });

    box.appendChild(header);
    box.appendChild(grid);
    overlay.appendChild(box);
    document.body.appendChild(overlay);
};