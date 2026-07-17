/* nuts-blog.js — blog page extras */
(function () {
    'use strict';

    /* ---- Preloader ---- */
    window.addEventListener('load', function () {
        var preloader = document.querySelector('.preloader');
        if (preloader) {
            preloader.style.opacity = '0';
            preloader.style.transition = 'opacity 0.4s';
            setTimeout(function () { preloader.style.display = 'none'; }, 400);
        }
    });

    /* ---- Smooth scroll for sidebar links ---- */
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

    /* ---- Active sidebar link on scroll ---- */
    var articles = document.querySelectorAll('.blog-post[id]');
    var navLinks = document.querySelectorAll('.idocs-navigation .nav-link');

    if (articles.length && navLinks.length) {
        var observer = new IntersectionObserver(function (entries) {
            entries.forEach(function (entry) {
                if (entry.isIntersecting) {
                    var id = entry.target.getAttribute('id');
                    navLinks.forEach(function (link) {
                        link.classList.remove('active');
                        if (link.getAttribute('href') === '#' + id) {
                            link.classList.add('active');
                        }
                    });
                }
            });
        }, { rootMargin: '-60px 0px -70% 0px', threshold: 0 });

        articles.forEach(function (a) { observer.observe(a); });
    }

})();