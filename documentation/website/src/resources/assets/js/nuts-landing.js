/* nuts-landing.js — minimal JS for the landing page */
(function () {
    'use strict';

    // ---- Snippet tab switching ----
    document.querySelectorAll('.nuts-snippet-tabs').forEach(function (tabGroup) {
        tabGroup.querySelectorAll('.nuts-snippet-tab').forEach(function (tab) {
            tab.addEventListener('click', function () {
                var target = tab.dataset.target || tab.dataset.os;
                var body = tabGroup.nextElementSibling; // .nuts-snippet-body

                // deactivate all tabs in this group
                tabGroup.querySelectorAll('.nuts-snippet-tab').forEach(function (t) {
                    t.classList.remove('active');
                });
                tab.classList.add('active');

                // show matching block
                body.querySelectorAll('.nuts-snippet-block').forEach(function (block) {
                    var match = block.id === target || block.dataset.os === target;
                    block.classList.toggle('active', match);
                });
            });
        });
    });

    // ---- Copy button ----
    document.querySelectorAll('.nuts-snippet-copy').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var body = btn.closest('.nuts-snippet-body');
            var activeBlock = body.querySelector('.nuts-snippet-block.active');
            if (!activeBlock) return;

            var text = activeBlock.querySelector('code').innerText;
            navigator.clipboard.writeText(text).then(function () {
                btn.classList.add('copied');
                btn.innerHTML = '<i class="fas fa-check"></i>';
                setTimeout(function () {
                    btn.classList.remove('copied');
                    btn.innerHTML = '<i class="far fa-copy"></i>';
                }, 2000);
            });
        });
    });

    // ---- Preloader ----
    window.addEventListener('load', function () {
        var preloader = document.querySelector('.preloader');
        if (preloader) {
            preloader.style.opacity = '0';
            preloader.style.transition = 'opacity 0.4s';
            setTimeout(function () { preloader.style.display = 'none'; }, 400);
        }
    });

})();