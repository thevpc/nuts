/* nuts-apps.js — app store interactions */
(function () {
    'use strict';

    /* ---- Icon colors by first letter ---- */
    var COLORS = [
        ['135deg,#0366d6,#23a6d5', '135deg,#7c3aed,#a855f7', '135deg,#059669,#34d399',
            '135deg,#d97706,#fbbf24', '135deg,#dc2626,#f87171', '135deg,#0891b2,#67e8f9',
            '135deg,#7c3aed,#818cf8', '135deg,#065f46,#10b981', '135deg,#b45309,#fcd34d',
            '135deg,#1e40af,#60a5fa', '135deg,#be123c,#fb7185', '135deg,#0369a1,#38bdf8',
            '135deg,#4338ca,#818cf8', '135deg,#15803d,#4ade80', '135deg,#92400e,#f59e0b',
            '135deg,#1d4ed8,#93c5fd', '135deg,#9f1239,#fda4af', '135deg,#0c4a6e,#7dd3fc',
            '135deg,#581c87,#d8b4fe', '135deg,#14532d,#86efac', '135deg,#78350f,#fde68a',
            '135deg,#1e3a8a,#bfdbfe', '135deg,#831843,#fbcfe8', '135deg,#0f172a,#64748b',
            '135deg,#064e3b,#6ee7b7', '135deg,#422006,#fdba74']
    ];

    function iconColor(title) {
        var c = (title || 'A').toUpperCase().charCodeAt(0) - 65;
        var idx = ((c % 26) + 26) % 26;
        return COLORS[0][idx];
    }

    /* ---- Set icon colors and first-letter content ---- */
    document.querySelectorAll('.app-card__icon, .app-card__icon--lg').forEach(function (el) {
        var letter = (el.dataset.letter || '?')[0].toUpperCase();
        el.style.background = 'linear-gradient(' + iconColor(letter) + ')';
        el.setAttribute('data-letter', letter);
    });

    /* Also fix sidebar letter spans */
    document.querySelectorAll('.apps-nav-app__letter').forEach(function (el) {
        el.textContent = (el.dataset.title || '?')[0].toUpperCase();
    });

    /* ---- Search & filter state ---- */
    var activeCategory = 'all';
    var searchQuery = '';

    function applyFilters() {
        var cards = document.querySelectorAll('.app-card');
        var categories = document.querySelectorAll('.apps-category');
        var noResults = document.getElementById('apps-no-results');
        var visibleCount = 0;

        cards.forEach(function (card) {
            var catMatch = activeCategory === 'all' || card.dataset.category === activeCategory;
            var q = searchQuery.toLowerCase();
            var searchText = (card.dataset.search || '').toLowerCase();
            var searchMatch = !q || searchText.indexOf(q) !== -1;
            var show = catMatch && searchMatch;
            card.classList.toggle('app-card--hidden', !show);
            if (show) visibleCount++;
        });

        /* Hide empty category sections */
        categories.forEach(function (section) {
            var visibleCards = section.querySelectorAll('.app-card:not(.app-card--hidden)');
            section.style.display = visibleCards.length ? '' : 'none';
        });

        if (noResults) {
            noResults.style.display = visibleCount === 0 ? 'block' : 'none';
        }
    }

    /* ---- Category filter pills ---- */
    document.querySelectorAll('.apps-filter-pill').forEach(function (pill) {
        pill.addEventListener('click', function () {
            activeCategory = pill.dataset.category;
            document.querySelectorAll('.apps-filter-pill').forEach(function (p) {
                p.classList.remove('apps-filter-pill--active');
            });
            pill.classList.add('apps-filter-pill--active');
            applyFilters();
        });
    });

    /* ---- Search inputs (both main header and sidebar) ---- */
    function bindSearch(input) {
        if (!input) return;
        input.addEventListener('input', function () {
            searchQuery = input.value.trim();
            /* Sync both inputs */
            ['apps-search-main', 'apps-search-input'].forEach(function (id) {
                var el = document.getElementById(id);
                if (el && el !== input) el.value = input.value;
            });
            applyFilters();
        });
    }

    bindSearch(document.getElementById('apps-search-main'));
    bindSearch(document.getElementById('apps-search-input'));

    /* ---- Copy buttons ---- */
    function bindCopy(btn) {
        if (!btn || btn.dataset.bound) return;
        btn.dataset.bound = '1';
        btn.addEventListener('click', function (e) {
            e.stopPropagation();
            var cmd = btn.dataset.cmd;
            if (!cmd) return;
            navigator.clipboard.writeText(cmd).then(function () {
                btn.classList.add('copied');
                btn.innerHTML = '<i class="fas fa-check"></i>';
                setTimeout(function () {
                    btn.classList.remove('copied');
                    btn.innerHTML = '<i class="far fa-copy"></i>';
                }, 2000);
            });
        });
    }

    document.querySelectorAll('.app-card__copy').forEach(bindCopy);

    /* ---- Detail panel toggle ---- */
    document.querySelectorAll('.app-card__details-btn').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.dataset.id;
            var detail = document.getElementById('detail-' + id);
            if (!detail) return;

            var isOpen = detail.style.display !== 'none';

            /* Close all other open panels */
            document.querySelectorAll('.app-card__detail').forEach(function (d) {
                d.style.display = 'none';
            });
            document.querySelectorAll('.app-card__details-btn').forEach(function (b) {
                b.innerHTML = 'Details <i class="fas fa-chevron-right"></i>';
            });
            document.querySelectorAll('.app-card').forEach(function (c) {
                c.style.borderColor = '';
            });

            if (!isOpen) {
                detail.style.display = 'block';
                btn.innerHTML = 'Close <i class="fas fa-chevron-up"></i>';

                /* highlight card */
                btn.closest('.app-card').style.borderColor = '#0366d6';

                /* bind copy buttons in detail */
                detail.querySelectorAll('.app-card__copy').forEach(bindCopy);

                /* syntax highlight */
                if (window.Prism) {
                    detail.querySelectorAll('code').forEach(function (el) {
                        Prism.highlightElement(el);
                    });
                }

                /* scroll card into view */
                setTimeout(function () {
                    var card = btn.closest('.app-card');
                    var top = card.getBoundingClientRect().top + window.scrollY - 90;
                    window.scrollTo({ top: top, behavior: 'smooth' });
                }, 60);
            }
        });
    });

    /* Close buttons inside detail panels */
    document.querySelectorAll('.app-card__detail-close').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var id = btn.dataset.id;
            var detail = document.getElementById('detail-' + id);
            if (detail) detail.style.display = 'none';
            var openBtn = document.querySelector('.app-card__details-btn[data-id="' + id + '"]');
            if (openBtn) openBtn.innerHTML = 'Details <i class="fas fa-chevron-right"></i>';
            var card = btn.closest('.app-card');
            if (card) card.style.borderColor = '';
        });
    });

    /* ---- Sidebar smooth scroll ---- */
    document.querySelectorAll('.idocs-navigation .nav-link').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var href = link.getAttribute('href');
            if (href && href.startsWith('#')) {
                var target = document.getElementById(href.slice(1));
                if (target) {
                    e.preventDefault();
                    var top = target.getBoundingClientRect().top + window.scrollY - 80;
                    window.scrollTo({ top: top, behavior: 'smooth' });
                }
            }
        });
    });

    /* ---- Preloader ---- */
    window.addEventListener('load', function () {
        var preloader = document.querySelector('.preloader');
        if (preloader) {
            preloader.style.opacity = '0';
            preloader.style.transition = 'opacity 0.4s';
            setTimeout(function () { preloader.style.display = 'none'; }, 400);
        }
    });

})();