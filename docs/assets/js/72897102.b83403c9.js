"use strict";(self.webpackChunknuts=self.webpackChunknuts||[]).push([[7179],{9741:(n,e,s)=>{s.r(e),s.d(e,{assets:()=>a,contentTitle:()=>l,default:()=>p,frontMatter:()=>i,metadata:()=>r,toc:()=>c});var o=s(4848),t=s(8453);const i={id:"commandline",title:"Command Line Arguments",sidebar_label:"Command Line Arguments"},l=void 0,r={id:"concepts/commandline",title:"Command Line Arguments",description:"nuts supports a specific format for command line arguments. This format is the format supported in `nuts` Application Framework (NAF) and as such all NAF applications support the same command line arguments format.",source:"@site/docs/concepts/commandline.md",sourceDirName:"concepts",slug:"/concepts/commandline",permalink:"/nuts/docs/concepts/commandline",draft:!1,unlisted:!1,editUrl:"https://github.com/facebook/docusaurus/tree/main/packages/create-docusaurus/templates/shared/docs/concepts/commandline.md",tags:[],version:"current",frontMatter:{id:"commandline",title:"Command Line Arguments",sidebar_label:"Command Line Arguments"},sidebar:"tutorialSidebar",previous:{title:"Automation & DevOps",permalink:"/nuts/docs/concepts/automation"},next:{title:"File system",permalink:"/nuts/docs/concepts/filesystem"}},a={},c=[{value:"Short vs Long Options",id:"short-vs-long-options",level:2},{value:"Valued / Non-valued Options",id:"valued--non-valued-options",level:2},{value:"Boolean Options",id:"boolean-options",level:2},{value:"Combo Simple Options",id:"combo-simple-options",level:2},{value:"Ignoring Options, Comments",id:"ignoring-options-comments",level:2},{value:"Nuts Option Types",id:"nuts-option-types",level:2}];function d(n){const e={code:"code",h2:"h2",li:"li",p:"p",pre:"pre",strong:"strong",ul:"ul",...(0,t.R)(),...n.components};return(0,o.jsxs)(o.Fragment,{children:[(0,o.jsxs)(e.p,{children:[(0,o.jsx)(e.strong,{children:"nuts"})," supports a specific format for command line arguments. This format is the format supported in ",(0,o.jsx)(e.strong,{children:(0,o.jsx)(e.code,{children:"nuts"})})," Application Framework (NAF) and as such all NAF applications support the same command line arguments format.\nArguments in ",(0,o.jsx)(e.strong,{children:(0,o.jsx)(e.code,{children:"nuts"})})," can be options or non options. Options always start with hyphen (-)."]}),"\n",(0,o.jsx)(e.h2,{id:"short-vs-long-options",children:"Short vs Long Options"}),"\n",(0,o.jsx)(e.p,{children:'Options can be long options (starts with double hyphen) or short options (start with a single hyphen).\nMany arguments support both forms. For instance "-w" and "--workspace" are the supported forms to define the workspace location in the nuts command.'}),"\n",(0,o.jsx)(e.h2,{id:"valued--non-valued-options",children:"Valued / Non-valued Options"}),"\n",(0,o.jsx)(e.p,{children:"Options can also support a value of type string or boolean. The value can be suffixed to the option while separated with '=' sign or immediately after the option."}),"\n",(0,o.jsx)(e.p,{children:"As an example, all are equivalent."}),"\n",(0,o.jsx)(e.pre,{children:(0,o.jsx)(e.code,{className:"language-sh",children:"nuts -w=/myfolder/myworkspace\nnuts -w /myfolder/myworkspace\nnuts --workspace /myfolder/myworkspace\nnuts --workspace=/myfolder/myworkspace\n"})}),"\n",(0,o.jsx)(e.p,{children:"Of course, not all options can support values, an not all options neither support the suffixed and/or the non-suffixed mode. Please relate to the documentation of nuts or the application you are using to know how to use the options."}),"\n",(0,o.jsx)(e.h2,{id:"boolean-options",children:"Boolean Options"}),"\n",(0,o.jsx)(e.p,{children:'Particularly, when the value is a boolean, the value do not need to be defined. As a result "--install-companions" and "--install-companions=true" are equivalent. However "--install-companions true" is not (because the option is of type boolean) and "true" will be parsed as a NonOption.'}),"\n",(0,o.jsxs)(e.p,{children:['To define a "false" value to the boolean option we can either suffix with "=false" or prefix with "!" or "~" sign.\nHence, "--install-companions=false", "--!install-companions" and "--~install-companions" are all equivalent.\nNote also that ',(0,o.jsx)(e.code,{children:"~"})," if referred to ",(0,o.jsx)(e.code,{children:"!"})," because in bash shells (and som other shells) ",(0,o.jsx)(e.code,{children:"!"})," will be expanded in a special manner."]}),"\n",(0,o.jsx)(e.h2,{id:"combo-simple-options",children:"Combo Simple Options"}),"\n",(0,o.jsx)(e.p,{children:'Simple options can be grouped in a single word. "-ls" is equivalent to "-l -s". So one should be careful.\nOne exception though. For portability reasons, "-version" is considered a single short option.'}),"\n",(0,o.jsx)(e.h2,{id:"ignoring-options-comments",children:"Ignoring Options, Comments"}),"\n",(0,o.jsx)(e.p,{children:'Options starting with "-//" and "--//" are simply ignored by the command line parser.'}),"\n",(0,o.jsx)(e.h2,{id:"nuts-option-types",children:"Nuts Option Types"}),"\n",(0,o.jsxs)(e.p,{children:["Options in ",(0,o.jsx)(e.strong,{children:(0,o.jsx)(e.code,{children:"nuts"})})," are can be of one of the following categories :"]}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsx)(e.p,{children:"Create Options : such options are only relevant when creating a new workspace. They define the configuration of the workspace to create. They will be ignored when the workspace already exists. They will be ignored too, in sub-processes. Examples include"}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"--install-companions"}),"\n",(0,o.jsx)(e.li,{children:"--archetype"}),"\n",(0,o.jsx)(e.li,{children:"--store-strategy"}),"\n",(0,o.jsx)(e.li,{children:"--standalone"}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsx)(e.p,{children:"Runtime Options : such options are relevant when running a workspace (be it existing or to be created) and are not passed to sub-processes"}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"--reset"}),"\n",(0,o.jsx)(e.li,{children:"--recover"}),"\n",(0,o.jsx)(e.li,{children:"--dry"}),"\n",(0,o.jsx)(e.li,{children:"--version"}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsxs)(e.p,{children:["Exported Options : are passed to sub-",(0,o.jsx)(e.strong,{children:"nuts"}),"-processes that will be created by ",(0,o.jsx)(e.strong,{children:"nuts"}),". For instance when nuts will call the ",(0,o.jsx)(e.strong,{children:"nsh"})," command it will spawn a new process. In such case, these options are passed to the sub-process as environment variable."]}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"--workspace"}),"\n",(0,o.jsx)(e.li,{children:"--global"}),"\n",(0,o.jsx)(e.li,{children:"--color"}),"\n",(0,o.jsx)(e.li,{children:"--bot"}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsx)(e.p,{children:"Executor Options : are options that are supported byte the package executor. Most of the time this will be the java executor and hence this coincides with the JVM options)"}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"-Xmx..."}),"\n",(0,o.jsx)(e.li,{children:"-Xmx"}),"\n",(0,o.jsx)(e.li,{children:"-D..."}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsxs)(e.p,{children:["Custom Nuts options : are special ",(0,o.jsx)(e.code,{children:"nuts"})," options that are specific to nuts implementation or validation process to be promoted to standard options. The arguments parser will never report an error regarding such options. They are used when available and valid. they will be ignored in all other cases. Such options start with triple hyphen (---)"]}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"---monitor.enabled"}),"\n",(0,o.jsx)(e.li,{children:"---monitor.start"}),"\n",(0,o.jsx)(e.li,{children:"---show-command"}),"\n",(0,o.jsx)(e.li,{children:"---perf"}),"\n",(0,o.jsx)(e.li,{children:"---init-platforms"}),"\n",(0,o.jsx)(e.li,{children:"---init-scripts"}),"\n",(0,o.jsx)(e.li,{children:"---init-java"}),"\n",(0,o.jsx)(e.li,{children:"---system-desktop-launcher"}),"\n",(0,o.jsx)(e.li,{children:"---system-menu-launcher"}),"\n",(0,o.jsx)(e.li,{children:"---system-custom-launcher"}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.li,{children:["\n",(0,o.jsx)(e.p,{children:"Application Options : are options that are by default supported by Applications using NAF (Nuts Application Framework) (as well as Nuts it self)."}),"\n",(0,o.jsxs)(e.ul,{children:["\n",(0,o.jsx)(e.li,{children:"--help"}),"\n",(0,o.jsx)(e.li,{children:"--version"}),"\n"]}),"\n"]}),"\n"]}),"\n",(0,o.jsxs)(e.p,{children:["all ",(0,o.jsx)(e.strong,{children:(0,o.jsx)(e.code,{children:"nuts"})})," options are described in the command help. Just type :"]}),"\n",(0,o.jsx)(e.pre,{children:(0,o.jsx)(e.code,{children:"nuts --help\n"})})]})}function p(n={}){const{wrapper:e}={...(0,t.R)(),...n.components};return e?(0,o.jsx)(e,{...n,children:(0,o.jsx)(d,{...n})}):d(n)}},8453:(n,e,s)=>{s.d(e,{R:()=>l,x:()=>r});var o=s(6540);const t={},i=o.createContext(t);function l(n){const e=o.useContext(i);return o.useMemo((function(){return"function"==typeof n?n(e):{...e,...n}}),[e,n])}function r(n){let e;return e=n.disableParentContext?"function"==typeof n.components?n.components(t):n.components||t:l(n.components),o.createElement(i.Provider,{value:e},n.children)}}}]);