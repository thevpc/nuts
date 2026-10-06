/* common.js (templated by nsite) */
var buildTime = "2026-10-06-182842";
var latestJarLocation = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/1.1.0/nuts-app-1.1.0.jar";
var apiVersion = "1.1.0";
var runtimeVersion = "1.1.0.0";

var stableJarLocation = "https://maven.thevpc.net/net/thevpc/nuts/nuts-app/1.0.0/nuts-app-1.0.0.jar";
var stableApiVersion = "1.0.0";
var stableRuntimeVersion = "1.0.0.0";

var buildTimeEl = document.getElementById('build-time');
if (buildTimeEl) {
    buildTimeEl.textContent = buildTime;
}