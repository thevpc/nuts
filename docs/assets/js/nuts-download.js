/* nuts-download.js — 3-step download wizard */
latestJarLocation="https://maven.thevpc.net/net/thevpc/nuts/nuts-app/1.0.0/nuts-app-1.0.0.jar";
apiVersion="1.0.0";
runtimeVersion="1.0.0.0";

stableJarLocation="https://maven.thevpc.net/net/thevpc/nuts/nuts-app/0.8.9/nuts-app-0.8.9.jar";
stableRuntimeVersion="0.8.9.0";

(function () {
    'use strict';

    /* ---- Version-specific URLs (nsite vars are already resolved in HTML,
            so we read them from the page or fall back to placeholders) ---- */
    var URLS = {
        latest: {
            jar:    document.querySelector('meta[name="dl-latest-jar"]')
                ? document.querySelector('meta[name="dl-latest-jar"]').content
                : latestJarLocation,
            version: runtimeVersion,
            stable:  false
        },
        lts: {
            jar:    document.querySelector('meta[name="dl-lts-jar"]')
                ? document.querySelector('meta[name="dl-lts-jar"]').content
                : stableJarLocation,
            version: stableRuntimeVersion,
            stable:  true
        }
    };

    /* ---- State ---- */
    var state = { version: null, platform: null, method: null };

    /* ---- Method definitions per platform ---- */
    var METHODS = {
        linux: [
            { id: 'curl',    icon: 'fas fa-terminal',   label: 'curl / wget',      desc: 'One-liner install. Recommended.' },
            { id: 'offline', icon: 'fas fa-cube',        label: 'Offline bundle',   desc: 'No internet after download. Air-gapped environments.' },
            { id: 'rpm',     icon: 'fas fa-box',         label: 'RPM package',      desc: 'RedHat, Fedora, OpenSuSE.' }
        ],
        macos: [
            { id: 'curl',    icon: 'fas fa-terminal',   label: 'curl',             desc: 'One-liner install. Recommended.' },
            { id: 'offline', icon: 'fas fa-cube',        label: 'Offline bundle',   desc: 'No internet after download.' }
        ],
        windows: [
            { id: 'jar',      icon: 'fas fa-download',  label: 'Download jar',     desc: 'Lightweight ~173KB. Requires Java 8+.' },
            { id: 'installer',icon: 'fas fa-magic',      label: 'Installer (.exe)', desc: 'Setup wizard. With or without bundled JRE.' },
            { id: 'offline',  icon: 'fas fa-cube',       label: 'Offline bundle',   desc: 'No internet after download. With or without JRE.' }
        ],
        docker: [
            { id: 'script',   icon: 'fas fa-play-circle', label: 'Bootstrap script', desc: 'Interactive. No Dockerfile needed.' },
            { id: 'dockerfile',icon: 'fab fa-docker',     label: 'Dockerfile',       desc: 'Build your own image.' }
        ]
    };

    /* ---- Install content per (platform, method, version) ---- */
    function getInstallContent(platform, method, ver) {
        var u = URLS[ver];
        var v = u.version;
        var jar = u.jar;

        var tip = {
            linux:   '<div class="dl-notice dl-notice--tip"><i class="fas fa-terminal"></i> Configures <code>~/.bashrc</code> automatically. Also supports <code>zsh</code>, <code>fish</code> and other shells. Open a new terminal after install.</div>',
            macos:   '<div class="dl-notice dl-notice--tip"><i class="fas fa-terminal"></i> Configures <code>~/.zshrc</code> automatically. Open a new terminal after install.</div>',
            windows: '<div class="dl-notice dl-notice--tip"><i class="fab fa-windows"></i> Creates a <strong>nuts</strong> Start Menu entry and Desktop shortcuts. Open a nuts-aware terminal after install. Supports Windows 7+.</div>',
            docker:  ''
        };

        var content = {
            linux: {
                curl: steps([
                    { label: 'Using curl', code: 'curl -sL ' + jar + ' -o nuts.jar\njava -jar nuts.jar -Zy\nexit' },
                    { label: 'Or using wget', code: 'wget ' + jar + ' -qO nuts.jar\njava -jar nuts.jar -y\nexit' }
                ]) + tip.linux,

                offline: dlTable([
                    { name: 'Linux x64 Offline Binaries', desc: 'Requires Java 8+.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-linux-x64-' + v + '.zip' },
                    { name: 'Linux x64 Offline + JRE', desc: 'Bundled JRE. No Java needed.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-linux64-bin-with-java-' + v + '.zip', badge: 'JRE included' }
                ]) + tip.linux,

                rpm: dlTable([
                    { name: 'RedHat / OpenSuSE RPM', desc: 'RPM with all dependencies.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-linux64-rpm-' + v + '.rpm' }
                ]) + tip.linux
            },

            macos: {
                curl: steps([
                    { label: 'Using curl', code: 'curl -sL ' + jar + ' -o nuts.jar\njava -jar nuts.jar -Zy\nexit' }
                ]) + tip.macos,

                offline: dlTable([
                    { name: 'macOS x64 Offline Binaries', desc: 'Requires Java 8+.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-mac64-' + v + '.app.zip' }
                ]) + tip.macos
            },

            windows: {
                jar: '<p style="font-size:0.88rem;color:#5a6a7a;margin-bottom:12px">Download the jar and run it with Java:</p>'
                    + dlTable([
                        { name: 'nuts-app-' + v + '.jar', desc: 'Lightweight ~173KB bootstrap jar.', url: jar }
                    ])
                    + steps([{ label: 'Then run', code: 'java -jar nuts.jar -Zy' }])
                    + tip.windows,

                installer: dlTable([
                    { name: 'Windows x64 Installer (.exe)', desc: 'Setup wizard. Requires Java 8+.', url: 'https://thevpc.net/nuts/nuts-installer-windows64-' + v + '.exe' },
                    { name: 'Windows x64 Installer + JRE', desc: 'Bundled JRE. No Java needed.', url: 'https://thevpc.net/nuts/nuts-installer-windows64-with-java-' + v + '.zip', badge: 'JRE included' }
                ]) + tip.windows,

                offline: dlTable([
                    { name: 'Windows x64 Offline Binaries', desc: 'Requires Java 8+.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-windows64-' + v + '.exe' },
                    { name: 'Windows x64 Offline + JRE', desc: 'Bundled JRE. No Java needed.', url: 'https://thevpc.net/nuts/' + apiVersion + '/nuts-app-full-windows64-with-java-' + v + '.zip', badge: 'JRE included' }
                ]) + tip.windows
            },

            docker: {
                script: [
                    '<p class="dl-docker__desc">JDK-agnostic: the bootstrap script uses whatever Java is in the base image. Nuts provisions the correct JDK per app internally.</p>',
                    steps([{ label: 'Run interactively — no Dockerfile needed', code: 'docker run -it --rm eclipse-temurin:8-jre-alpine bash -c \\\n  "$(curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh)"' }]),
                    '<p class="dl-docker__desc" style="margin-top:12px">Override the Nuts version via env var:</p>',
                    steps([{ label: 'Pin a specific version', code: 'docker run -it --rm -e NUTS_VERSION=' + v + ' eclipse-temurin:8-jre-alpine bash -c \\\n  "$(curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh)"' }]),
                    '<p class="dl-docker__desc" style="margin-top:12px">Once inside the container:</p>',
                    steps([{ label: 'Install and run any app', code: 'nuts install org.apache.netbeans:netbeans\nnuts netbeans' }]),
                    '<div class="dl-notice dl-notice--info" style="margin-top:16px"><i class="fas fa-info-circle"></i> We recommend <code>eclipse-temurin:8-jre-alpine</code> (~85MB) over the deprecated <code>openjdk:8</code> (~400MB). Any JDK 8+ image works.</div>'
                ].join(''),

                dockerfile: [
                    '<p class="dl-docker__desc">Use the bootstrap script in your Dockerfile — <code>NUTS_VERSION</code> controls which version is installed:</p>',
                    steps([{ label: 'Via bootstrap script (recommended)', code: 'FROM eclipse-temurin:8-jre-alpine\nENV NUTS_VERSION=' + v + '\nRUN curl -sSL https://thevpc.net/nuts/bootstrap-container-latest.sh | bash -s -- -Ny\nRUN nuts -Zy install &lt;your-application&gt;\nCMD ["nuts", "-y", "&lt;your-application&gt;"]' }]),
                    '<p class="dl-docker__desc" style="margin-top:20px">Or pin the jar directly for reproducible builds:</p>',
                    steps([{ label: 'Via jar (explicit control)', code: 'FROM eclipse-temurin:8-jre-alpine\nENV NUTS_VERSION=' + v + '\nRUN wget "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/${NUTS_VERSION}/nuts-app-${NUTS_VERSION}.jar" \\\n        -qO ~/bin/nuts.jar \\\n    && java -jar ~/bin/nuts.jar -Ny\nRUN nuts -Zy install &lt;your-application&gt;\nCMD ["nuts", "-y", "&lt;your-application&gt;"]' }]),
                    '<div class="dl-notice dl-notice--tip" style="margin-top:16px"><i class="fas fa-lightbulb"></i> The base image JDK is just a bootstrap ladder. Nuts provisions the correct JDK for each app it manages.</div>'
                ].join('')
            }
        };

        return (content[platform] && content[platform][method]) || '';
    }

    /* ---- Helpers ---- */
    function codeBlock(code) {
        return '<div class="dl-code-block"><pre><code class="language-bash">' + code + '</code></pre>'
            + '<button class="dl-copy-btn" title="Copy"><i class="far fa-copy"></i></button></div>';
    }

    function steps(arr) {
        return arr.map(function (s, i) {
            return '<div class="dl-step">'
                + (arr.length > 1 ? '<span class="dl-step__num">' + (i + 1) + '</span>' : '')
                + '<div class="dl-step__body">'
                + '<p class="dl-step__label">' + s.label + '</p>'
                + codeBlock(s.code)
                + '</div></div>';
        }).join('');
    }

    function dlTable(rows) {
        return '<div class="dl-installer-table"><div class="dl-installer-group">'
            + rows.map(function (r) {
                return '<div class="dl-installer-row">'
                    + '<div class="dl-installer-row__info">'
                    + '<span class="dl-installer-row__name">' + r.name + '</span>'
                    + '<span class="dl-installer-row__desc">' + r.desc + '</span>'
                    + (r.badge ? '<span class="dl-badge dl-badge--jre">' + r.badge + '</span>' : '')
                    + '</div>'
                    + '<a href="' + r.url + '" class="dl-installer-btn"><i class="fas fa-download"></i> Download</a>'
                    + '</div>';
            }).join('')
            + '</div></div>';
    }

    /* ---- Render method choices ---- */
    function renderMethodChoices(platform) {
        var methods = METHODS[platform] || [];
        var container = document.getElementById('method-choices');
        container.innerHTML = '<div class="dl-choice-grid dl-choice-grid--' + methods.length + '">'
            + methods.map(function (m) {
                return '<button class="dl-choice dl-choice--method" data-method="' + m.id + '">'
                    + '<div class="dl-choice__icon dl-choice__icon--indigo"><i class="' + m.icon + '"></i></div>'
                    + '<div class="dl-choice__text">'
                    + '<span class="dl-choice__label">' + m.label + '</span>'
                    + '<span class="dl-choice__desc">' + m.desc + '</span>'
                    + '</div></button>';
            }).join('')
            + '</div>';

        /* bind events on new buttons */
        container.querySelectorAll('.dl-choice--method').forEach(function (btn) {
            btn.addEventListener('click', function () {
                selectMethod(btn.dataset.method);
            });
        });
    }

    /* ---- Step unlock/lock ---- */
    function unlockStep(id) {
        var el = document.getElementById(id);
        if (el) el.classList.remove('dl-step-block--locked');
    }

    function lockStep(id) {
        var el = document.getElementById(id);
        if (el) el.classList.add('dl-step-block--locked');
    }

    function setSelection(id, text) {
        var el = document.getElementById(id);
        if (el) el.textContent = text ? '→ ' + text : '';
    }

    /* ---- Selection handlers ---- */
    function selectVersion(ver) {
        state.version = ver;
        state.platform = null;
        state.method = null;

        document.querySelectorAll('.dl-choice--version').forEach(function (b) {
            b.classList.toggle('dl-choice--active', b.dataset.version === ver);
        });

        var label = ver === 'latest'
            ? 'Latest ' + URLS.latest.version
            : 'LTS ' + URLS.lts.version;
        setSelection('sel-version', label);

        unlockStep('step-platform');
        lockStep('step-method');
        setSelection('sel-platform', '');
        setSelection('sel-method', '');

        document.getElementById('install-content').style.display = 'none';
        document.getElementById('install-content').innerHTML = '';
        document.getElementById('dl-verify').style.display = 'none';

        /* reset platform choices */
        document.querySelectorAll('.dl-choice--platform').forEach(function (b) {
            b.classList.remove('dl-choice--active');
        });

        /* scroll to step 2 */
        scrollToStep('step-platform');
    }

    function selectPlatform(platform) {
        state.platform = platform;
        state.method = null;

        document.querySelectorAll('.dl-choice--platform').forEach(function (b) {
            b.classList.toggle('dl-choice--active', b.dataset.platform === platform);
        });

        var labels = { linux: 'Linux', macos: 'macOS', windows: 'Windows', docker: 'Docker' };
        setSelection('sel-platform', labels[platform]);

        unlockStep('step-method');
        setSelection('sel-method', '');

        document.getElementById('install-content').style.display = 'none';
        document.getElementById('install-content').innerHTML = '';
        document.getElementById('dl-verify').style.display = 'none';

        renderMethodChoices(platform);
        scrollToStep('step-method');
    }

    function selectMethod(method) {
        state.method = method;

        document.querySelectorAll('.dl-choice--method').forEach(function (b) {
            b.classList.toggle('dl-choice--active', b.dataset.method === method);
        });

        var methods = METHODS[state.platform] || [];
        var found = methods.filter(function (m) { return m.id === method; })[0];
        setSelection('sel-method', found ? found.label : method);

        /* render install content */
        var html = getInstallContent(state.platform, method, state.version);
        var content = document.getElementById('install-content');
        content.innerHTML = html;
        content.style.display = 'block';

        /* syntax highlight new blocks */
        if (window.Prism) {
            content.querySelectorAll('code').forEach(function (el) {
                Prism.highlightElement(el);
            });
        }

        /* bind copy buttons in new content */
        bindCopyButtons(content);

        /* show verify section (not for docker) */
        document.getElementById('dl-verify').style.display =
            (state.platform === 'docker') ? 'none' : 'block';

        scrollToStep('install-content');
    }

    /* ---- Copy buttons ---- */
    function bindCopyButtons(root) {
        (root || document).querySelectorAll('.dl-copy-btn').forEach(function (btn) {
            /* avoid double-binding */
            if (btn.dataset.bound) return;
            btn.dataset.bound = '1';
            btn.addEventListener('click', function () {
                var block = btn.closest('.dl-code-block');
                if (!block) return;
                var code = block.querySelector('code');
                if (!code) return;
                navigator.clipboard.writeText(code.innerText.trim()).then(function () {
                    btn.classList.add('copied');
                    btn.innerHTML = '<i class="fas fa-check"></i>';
                    setTimeout(function () {
                        btn.classList.remove('copied');
                        btn.innerHTML = '<i class="far fa-copy"></i>';
                    }, 2000);
                });
            });
        });
    }

    /* ---- Smooth scroll ---- */
    function scrollToStep(id) {
        var el = document.getElementById(id);
        if (!el) return;
        setTimeout(function () {
            var top = el.getBoundingClientRect().top + window.scrollY - 80;
            window.scrollTo({ top: top, behavior: 'smooth' });
        }, 60);
    }

    /* ---- Archive toggle ---- */
    var archiveToggle = document.getElementById('archive-toggle');
    var archiveBody = document.getElementById('archive-body');
    if (archiveToggle && archiveBody) {
        archiveToggle.addEventListener('click', function () {
            var open = archiveBody.style.display !== 'none';
            archiveBody.style.display = open ? 'none' : 'block';
            archiveToggle.classList.toggle('dl-archive-toggle--open', !open);
        });
    }

    /* ---- Bind version and platform choices ---- */
    document.querySelectorAll('.dl-choice--version').forEach(function (btn) {
        btn.addEventListener('click', function () { selectVersion(btn.dataset.version); });
    });

    document.querySelectorAll('.dl-choice--platform').forEach(function (btn) {
        btn.addEventListener('click', function () { selectPlatform(btn.dataset.platform); });
    });

    /* ---- Static copy buttons (archive, verify) ---- */
    bindCopyButtons();

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