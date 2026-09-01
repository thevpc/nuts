/* common.js (templated by nsite) */
var buildTime = "${{buildTime}}";
var latestJarLocation = "${{latestJarLocation}}";
var apiVersion = "${{apiVersion}}";
var runtimeVersion = "${{runtimeVersion}}";

var stableJarLocation = "${{stableJarLocation}}";
var stableApiVersion = "${{stableApiVersion}}";
var stableRuntimeVersion = "${{stableRuntimeVersion}}";

var buildTimeEl = document.getElementById('build-time');
if (buildTimeEl) {
    buildTimeEl.textContent = buildTime;
}