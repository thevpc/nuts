(window.webpackJsonp=window.webpackJsonp||[]).push([[40],{117:function(e,n,t){"use strict";t.d(n,"a",(function(){return m}));var r=t(0),a=t.n(r);function o(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function s(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function c(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?s(Object(t),!0).forEach((function(n){o(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):s(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function u(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var i=a.a.createContext({}),p=function(e){var n=a.a.useContext(i),t=n;return e&&(t="function"==typeof e?e(n):c(c({},n),e)),t},l={inlineCode:"code",wrapper:function(e){var n=e.children;return a.a.createElement(a.a.Fragment,{},n)}},d=a.a.forwardRef((function(e,n){var t=e.components,r=e.mdxType,o=e.originalType,s=e.parentName,i=u(e,["components","mdxType","originalType","parentName"]),d=p(t),m=r,f=d["".concat(s,".").concat(m)]||d[m]||l[m]||o;return t?a.a.createElement(f,c(c({ref:n},i),{},{components:t})):a.a.createElement(f,c({ref:n},i))}));function m(e,n){var t=arguments,r=n&&n.mdxType;if("string"==typeof e||r){var o=t.length,s=new Array(o);s[0]=d;var c={};for(var u in n)hasOwnProperty.call(n,u)&&(c[u]=n[u]);c.originalType=e,c.mdxType="string"==typeof e?e:r,s[1]=c;for(var i=2;i<o;i++)s[i]=t[i];return a.a.createElement.apply(null,s)}return a.a.createElement.apply(null,t)}d.displayName="MDXCreateElement"},95:function(e,n,t){"use strict";t.r(n),t.d(n,"frontMatter",(function(){return s})),t.d(n,"metadata",(function(){return c})),t.d(n,"rightToc",(function(){return u})),t.d(n,"default",(function(){return p}));var r=t(2),a=t(6),o=(t(0),t(117)),s={id:"info-cmd",title:"Info Command",sidebar_label:"Info Command"},c={unversionedId:"cmd/info-cmd",id:"cmd/info-cmd",isDocsHomePage:!1,title:"Info Command",description:"info command is a more verbose command than version. It shows a lot of other nuts properties that describe the booted workspace, such as the workspace name, the store locations (artifacts, caches, ....)",source:"@site/docs/cmd/info-cmd.md",permalink:"/nuts/docs/cmd/info-cmd",editUrl:"https://github.com/thevpc/nuts/edit/master/website/docs/cmd/info-cmd.md",sidebar_label:"Info Command",sidebar:"someSidebar",previous:{title:"Help Command",permalink:"/nuts/docs/cmd/help-cmd"},next:{title:"Install Command",permalink:"/nuts/docs/cmd/install-cmd"}},u=[],i={rightToc:u};function p(e){var n=e.components,t=Object(a.a)(e,["components"]);return Object(o.a)("wrapper",Object(r.a)({},i,t,{components:n,mdxType:"MDXLayout"}),Object(o.a)("p",null,Object(o.a)("strong",{parentName:"p"},"info")," command is a more verbose command than version. It shows a lot of other ",Object(o.a)("strong",{parentName:"p"},"nuts")," properties that describe the booted workspace, such as the workspace name, the store locations (artifacts, caches, ....)"),Object(o.a)("pre",null,Object(o.a)("code",Object(r.a)({parentName:"pre"},{}),"me@linux:~> nuts info\nname                     = default-workspace\nnuts-api-version         = 0.5.7\nnuts-api-id              = net.vpc.app.nuts:nuts#0.5.7\nnuts-runtime-id          = net.vpc.app.nuts:nuts-core#0.5.7.0\nnuts-runtime-path        = ~/.cache/nuts/default-workspace/boot/net/vpc/app/nuts/nuts-core/0.5.7.0/nuts-core-0.5.7.0.jar;~/.cache/nuts/default-workspace/boot/net/vpc/app/nuts/nuts/0.5.7/nuts-0.5.7.jar;~/.cache/nuts/default-workspace/boot/com/google/code/gson/gson/2.8.5/gson-2.8.5.jar;~/.cache/nuts/default-workspace/boot/org/fusesource/jansi/jansi/1.17.1/jansi-1.17.1.jar\nnuts-workspace-id        = 99b73002-804d-4e4c-9a13-f57ac1f40b3d\nnuts-store-layout        = linux\nnuts-store-strategy      = exploded\nnuts-repo-store-strategy = exploded\nnuts-global              = false\nnuts-workspace           = ~/.config/nuts/default-workspace\nnuts-workspace-apps      = ~/.local/share/nuts/apps/default-workspace\nnuts-workspace-config    = ~/.config/nuts/default-workspace/config\nnuts-workspace-var       = ~/.local/share/nuts/var/default-workspace\nnuts-workspace-log       = ~/.local/log/nuts/default-workspace\nnuts-workspace-temp      = ~/nuts/default-workspace\nnuts-workspace-cache     = ~/.cache/nuts/default-workspace\nnuts-workspace-lib       = ~/.local/share/nuts/lib/default-workspace\nnuts-workspace-run       = /run/user/1000/nuts/default-workspace\nnuts-open-mode           = open-or-create\nnuts-secure              = false\nnuts-gui                 = false\nnuts-inherited           = false\nnuts-recover             = false\nnuts-reset               = false\nnuts-debug               = false\nnuts-trace               = true\nnuts-read-only           = false\nnuts-skip-companions     = false\nnuts-skip-welcome        = false\njava-version             = 1.8.0_222\nplatform                 = java#1.8.0_222\njava-home                = /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0/jre\njava-executable          = /usr/lib64/jvm/java-1.8.0-openjdk-1.8.0/jre/bin/java\njava-classpath           = ~/.m2/repository/net/vpc/app/nuts/nuts/0.5.7/nuts-0.5.7.jar\njava-library-path        = /usr/java/packages/lib/amd64:/usr/lib64:/lib64:/lib:/usr/lib\nos-name                  = linux#4.12.14-lp151.28.13-default\nos-family                = linux\nos-dist                  = opensuse-leap#15.1\nos-arch                  = x86_64\nuser-name                = me\nuser-home                = /home/me\nuser-dir                 = /home/me\ncommand-line-long        = --color=system --trace --open-or-create --exec info\ncommand-line-short       = -t info\ninherited                = false\ninherited-nuts-boot-args = \ninherited-nuts-args      = \ncreation-started         = 2019-08-26 00:02:10.903\ncreation-finished        = 2019-08-26 00:02:11.223\ncreation-within          = 320ms\nrepositories-count       = 5\n")))}p.isMDXComponent=!0}}]);