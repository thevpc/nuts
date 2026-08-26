/* nuts-docs.js — documentation page interactivity and enhanced search */
(function () {
    'use strict';

    // ---- Preloader ----
    window.addEventListener('load', function () {
        var preloader = document.querySelector('.preloader');
        if (preloader) {
            preloader.style.opacity = '0';
            preloader.style.transition = 'opacity 0.4s';
            setTimeout(function () { preloader.style.display = 'none'; }, 400);
        }
    });

    // ---- Reading progress bar ----
    var bar = document.getElementById('doc-progress');
    if (bar) {
        window.addEventListener('scroll', function () {
            var doc = document.documentElement;
            var scrollTop = doc.scrollTop || document.body.scrollTop;
            var scrollHeight = doc.scrollHeight - doc.clientHeight;
            var pct = scrollHeight > 0 ? (scrollTop / scrollHeight) * 100 : 0;
            bar.style.width = pct + '%';
        }, { passive: true });
    }

    // ---- Sidebar active link tracking ----
    var sections = document.querySelectorAll('section[id]');
    var navLinks = document.querySelectorAll('.idocs-navigation .nav-link');

    if (sections.length && navLinks.length) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    var id = entry.target.getAttribute('id');
                    navLinks.forEach(function (link) {
                        link.classList.remove('active');
                        if (link.getAttribute('href') === '#' + id) {
                            link.classList.add('active');
                            /* scroll sidebar to show active link */
                            var nav = document.querySelector('.idocs-navigation');
                            if (nav) {
                                var linkTop = link.offsetTop;
                                var navHeight = nav.clientHeight;
                                if (linkTop < nav.scrollTop || linkTop > nav.scrollTop + navHeight - 60) {
                                    nav.scrollTo({ top: linkTop - navHeight / 2, behavior: 'smooth' });
                                }
                            }
                        }
                    });
                }
            });
        }, {
            rootMargin: '-60px 0px -70% 0px',
            threshold: 0
        });

        sections.forEach(function (s) { observer.observe(s); });
    }

    // ---- Copy button on every code block ----
    document.querySelectorAll('.idocs-content pre').forEach(function (pre) {
        if (pre.querySelector('.doc-copy-btn')) return; // Avoid duplicates
        var btn = document.createElement('button');
        btn.className = 'doc-copy-btn';
        btn.title = 'Copy to clipboard';
        btn.innerHTML = '<i class="far fa-copy"></i>';
        btn.style.cssText = [
            'position:absolute',
            'top:8px',
            'right:8px',
            'background:rgba(255,255,255,0.1)',
            'border:1px solid rgba(255,255,255,0.15)',
            'color:rgba(255,255,255,0.6)',
            'border-radius:5px',
            'width:28px',
            'height:28px',
            'display:flex',
            'align-items:center',
            'justify-content:center',
            'cursor:pointer',
            'font-size:0.78rem',
            'transition:all 0.15s',
            'padding:0'
        ].join(';');

        pre.style.position = 'relative';

        btn.addEventListener('click', function () {
            var code = pre.querySelector('code');
            if (!code) return;
            navigator.clipboard.writeText(code.innerText).then(function () {
                btn.innerHTML = '<i class="fas fa-check"></i>';
                btn.style.color = '#4ade80';
                btn.style.borderColor = '#4ade80';
                setTimeout(function () {
                    btn.innerHTML = '<i class="far fa-copy"></i>';
                    btn.style.color = 'rgba(255,255,255,0.6)';
                    btn.style.borderColor = 'rgba(255,255,255,0.15)';
                }, 2000);
            });
        });

        pre.appendChild(btn);
    });

    // ---- Smooth scroll for sidebar links ----
    document.querySelectorAll('.idocs-navigation .nav-link').forEach(function (link) {
        link.addEventListener('click', function (e) {
            var href = link.getAttribute('href');
            if (href && href.startsWith('#')) {
                var target = document.getElementById(href.slice(1));
                if (target) {
                    e.preventDefault();
                    var offset = 72; /* header height */
                    var top = target.getBoundingClientRect().top + window.scrollY - offset;
                    window.scrollTo({ top: top, behavior: 'smooth' });

                    // Close mobile navigation drawer if open
                    var nav = document.querySelector('.idocs-navigation');
                    if (nav && nav.classList.contains('active')) {
                        nav.classList.remove('active');
                        var collapseBtn = document.getElementById('sidebarCollapse');
                        if (collapseBtn) {
                            collapseBtn.querySelector('span:nth-child(3)').classList.remove('w-50');
                        }
                    }
                }
            }
        });
    });

    // ---- Enhanced Search Feature ----
    var searchInput = document.getElementById('docs-search-input');
    if (searchInput) {
        // Style search results panel dynamically
        var searchStyle = document.createElement('style');
        searchStyle.innerHTML = `
            .search-results-panel {
                display: none;
                position: absolute;
                left: 20px;
                right: 20px;
                top: 100%;
                background: #ffffff;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                box-shadow: 0 10px 25px -5px rgba(0, 0, 0, 0.1), 0 8px 10px -6px rgba(0, 0, 0, 0.1);
                max-height: 380px;
                overflow-y: auto;
                z-index: 9999;
                margin-top: 6px;
                scrollbar-width: thin;
            }
            .search-result-item {
                display: block;
                padding: 12px 16px;
                text-decoration: none !important;
                border-bottom: 1px solid #f1f5f9;
                transition: background-color 0.2s ease, border-left-color 0.2s ease;
                border-left: 3px solid transparent;
            }
            .search-result-item:last-child {
                border-bottom: none;
            }
            .search-result-item:hover {
                background-color: #f8fafc;
                border-left-color: #0366d6;
            }
            .search-result-title {
                font-size: 0.88rem;
                font-weight: 600;
                color: #1e293b;
                margin-bottom: 3px;
            }
            .search-result-snippet {
                font-size: 0.78rem;
                color: #64748b;
                line-height: 1.45;
            }
            .search-result-snippet mark {
                background-color: #fef08a;
                color: #854d0e;
                padding: 1px 3px;
                border-radius: 2px;
                font-weight: 500;
            }
        `;
        document.head.appendChild(searchStyle);

        // Create search results container
        var resultsPanel = document.createElement('div');
        resultsPanel.className = 'search-results-panel';
        searchInput.parentElement.style.position = 'relative';
        searchInput.parentElement.appendChild(resultsPanel);

        // Index page content
        var searchIndex = [];
        document.querySelectorAll('.idocs-content section[id]').forEach(function (section) {
            var heading = section.querySelector('h1, h2, h3, h4, h5');
            var title = heading ? heading.innerText.replace(/^[#0-9.\s]+/, '').trim() : '';
            if (!title) return;

            // Gather all content text
            var contentText = '';
            section.querySelectorAll('p, li, td, pre code').forEach(function (el) {
                contentText += ' ' + el.innerText;
            });
            contentText = contentText.replace(/\s+/g, ' ').trim();

            searchIndex.push({
                id: section.id,
                title: title,
                text: contentText,
                textLower: contentText.toLowerCase()
            });
        });

        // Search execution
        var searchDebounce;
        searchInput.addEventListener('input', function () {
            var query = this.value.trim().toLowerCase();
            clearTimeout(searchDebounce);

            searchDebounce = setTimeout(function () {
                // Filter the sidebar navigation items
                filterSidebar(query);

                if (query.length < 2) {
                    resultsPanel.style.display = 'none';
                    return;
                }

                var matches = [];
                var queryWords = query.split(/\s+/).filter(Boolean);

                searchIndex.forEach(function (entry) {
                    var titleScore = 0;
                    var contentScore = 0;

                    // Match all words
                    var matchesAll = queryWords.every(function (word) {
                        var inTitle = entry.title.toLowerCase().indexOf(word) !== -1;
                        var inContent = entry.textLower.indexOf(word) !== -1;
                        if (inTitle) titleScore += 10;
                        if (inContent) contentScore += 1;
                        return inTitle || inContent;
                    });

                    if (matchesAll) {
                        // Generate dynamic preview snippet
                        var firstWord = queryWords[0];
                        var idx = entry.textLower.indexOf(firstWord);
                        var start = Math.max(0, idx - 45);
                        var end = Math.min(entry.text.length, idx + firstWord.length + 80);
                        var snippet = entry.text.substring(start, end);
                        if (start > 0) snippet = '...' + snippet;
                        if (end < entry.text.length) snippet = snippet + '...';

                        matches.push({
                            id: entry.id,
                            title: entry.title,
                            snippet: snippet,
                            score: titleScore + contentScore
                        });
                    }
                });

                if (matches.length === 0) {
                    resultsPanel.innerHTML = '<div style="padding:16px;font-size:0.8rem;color:#94a3b8;text-align:center;">No matching results found</div>';
                    resultsPanel.style.display = 'block';
                    return;
                }

                // Sort by matching relevance score
                matches.sort(function (a, b) { return b.score - a.score; });

                var html = '';
                matches.slice(0, 8).forEach(function (m) {
                    var highlightSnippet = m.snippet;
                    queryWords.forEach(function (word) {
                        var regex = new RegExp('(' + word.replace(/[-\/\\^$*+?.()|[\]{}]/g, '\\$&') + ')', 'gi');
                        highlightSnippet = highlightSnippet.replace(regex, '<mark>$1</mark>');
                    });

                    html += '<a href="#' + m.id + '" class="search-result-item">' +
                                '<div class="search-result-title">' + m.title + '</div>' +
                                '<div class="search-result-snippet">' + highlightSnippet + '</div>' +
                            '</a>';
                });

                resultsPanel.innerHTML = html;
                resultsPanel.style.display = 'block';

                // Add click listener to results
                resultsPanel.querySelectorAll('.search-result-item').forEach(function (item) {
                    item.addEventListener('click', function (e) {
                        e.preventDefault();
                        var targetId = this.getAttribute('href').substring(1);
                        var target = document.getElementById(targetId);
                        if (target) {
                            var offset = 72;
                            var top = target.getBoundingClientRect().top + window.scrollY - offset;
                            window.scrollTo({ top: top, behavior: 'smooth' });
                        }
                        resultsPanel.style.display = 'none';
                        searchInput.value = '';
                        filterSidebar('');
                    });
                });
            }, 100);
        });

        // Close search list on clicking outside
        document.addEventListener('click', function (e) {
            if (!searchInput.contains(e.target) && !resultsPanel.contains(e.target)) {
                resultsPanel.style.display = 'none';
            }
        });
    }

    function filterSidebar(query) {
        document.querySelectorAll('.idocs-navigation .nav-item').forEach(function (item) {
            if (!query) {
                item.style.display = '';
                return;
            }
            var link = item.querySelector('.nav-link');
            var text = link ? link.innerText.toLowerCase() : '';

            if (text.indexOf(query) !== -1) {
                item.style.display = '';
                // Reveal parent list if hidden
                var parent = item.closest('ul');
                while (parent && parent.classList.contains('nav')) {
                    var parentItem = parent.closest('.nav-item');
                    if (parentItem) parentItem.style.display = '';
                    parent = parentItem ? parentItem.closest('ul') : null;
                }
            } else {
                // Keep visible if there are matching children
                var hasVisibleChildren = false;
                item.querySelectorAll('.nav-item').forEach(function (sub) {
                    var subLink = sub.querySelector('.nav-link');
                    if (subLink && subLink.innerText.toLowerCase().indexOf(query) !== -1) {
                        hasVisibleChildren = true;
                    }
                });
                item.style.display = hasVisibleChildren ? '' : 'none';
            }
        });
    }

})();