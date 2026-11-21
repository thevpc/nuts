/**
 * JPMS descriptor for the nuts runtime.
 * Generated from META-INF/services so that named modules can discover providers.
 */
module net.thevpc.nuts.runtime {
    requires java.base;
    requires java.compiler;
    requires java.desktop;
    requires java.logging;
    requires java.management;
    requires java.xml;
    requires net.thevpc.nuts;
    requires net.thevpc.nuts.boot;

    uses net.thevpc.nuts.spi.NComponent;
    uses net.thevpc.nuts.boot.NBootWorkspaceFactory;

    // ARCH-01: mirrors META-INF/services. Four constructor-injected components
    // (DefaultNExtensions, NEnvAsCmd, NEnvRnsh, DefaultNCompress) are intentionally
    // omitted: ServiceLoader requires a public no-arg constructor and nuts creates
    // them with workspace/context arguments. They remain in the service file for
    // the classpath loader.
    provides net.thevpc.nuts.spi.NComponent with
            net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNCmdLineRPI
            ,net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNCmdLineWriter
            ,net.thevpc.nuts.runtime.standalone.app.NApplicationImpl
            ,net.thevpc.nuts.runtime.standalone.atrifact.DefaultNClasspathBuilder
            ,net.thevpc.nuts.runtime.standalone.collections.DefaultNUtilsRPI
            ,net.thevpc.nuts.runtime.standalone.concurrent.DefaultNConcurrentRPI
            ,net.thevpc.nuts.runtime.standalone.concurrent.DefaultNLockBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNArtifactCallBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNDependencyBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNDescriptorBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNEnvConditionBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNIdBuilder
            ,net.thevpc.nuts.runtime.standalone.DefaultNWorkspaceOptionsBuilder
            ,net.thevpc.nuts.runtime.standalone.definition.filter.InternalNDefinitionFilterRPI
            ,net.thevpc.nuts.runtime.standalone.definition.NDefinitionFactoryImpl
            ,net.thevpc.nuts.runtime.standalone.dependency.filter.InternalNDependencyFilterRPI
            ,net.thevpc.nuts.runtime.standalone.dependency.format.DefaultNDependencyWriter
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.DefaultNDependencySolverRPI
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.DescriptorNDependencySolverFactory
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.GradleFirstNDependencySolverFactory
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.GradleNDependencySolverFactory
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.MavenFirstNDependencySolverFactory
            ,net.thevpc.nuts.runtime.standalone.dependency.solver.MavenNDependencySolverFactory
            ,net.thevpc.nuts.runtime.standalone.descriptor.format.DefaultNDescriptorWriter
            ,net.thevpc.nuts.runtime.standalone.descriptor.parser.DefaultNDescriptorParser
            ,net.thevpc.nuts.runtime.standalone.descriptor.parser.JarDescriptorContentParserComponent
            ,net.thevpc.nuts.runtime.standalone.descriptor.parser.NshDescriptorContentParserComponent
            ,net.thevpc.nuts.runtime.standalone.descriptor.parser.ZipDescriptorContentParserComponent
            ,net.thevpc.nuts.runtime.standalone.elem.DefaultNElementRPI
            ,net.thevpc.nuts.runtime.standalone.elem.DefaultNElements
            ,net.thevpc.nuts.runtime.standalone.elem.DefaultNElementWriter
            ,net.thevpc.nuts.runtime.standalone.elem.parser.DefaultNElementReader
            ,net.thevpc.nuts.runtime.standalone.executor.java.JavaExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.javasrc.JavaSourceExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.pom.PomAndUnsupportedJavaExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.system.ExecExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.war.WarExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.executor.zip.ZipExecutorComponent
            ,net.thevpc.nuts.runtime.standalone.extension.ClassPathNExtensionCatalog
            ,net.thevpc.nuts.runtime.standalone.format.DefaultNObjectObjectWriter
            ,net.thevpc.nuts.runtime.standalone.format.props.DefaultNPropertiesObjectWriter
            ,net.thevpc.nuts.runtime.standalone.format.tree.DefaultNTreeObjectWriter
            ,net.thevpc.nuts.runtime.standalone.id.filter.InternalNIdFilterRPI
            ,net.thevpc.nuts.runtime.standalone.id.format.DefaultNIdWriter
            ,net.thevpc.nuts.runtime.standalone.installer.CommandForIdNInstallerComponent
            ,net.thevpc.nuts.runtime.standalone.installer.ZipInstallerComponent
            ,net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIO
            ,net.thevpc.nuts.runtime.standalone.io.inputstream.DefaultNIORPI
            ,net.thevpc.nuts.runtime.standalone.io.terminal.DefaultNSystemTerminalBase
            ,net.thevpc.nuts.runtime.standalone.io.terminal.history.NCmdLineHistoryImpl
            ,net.thevpc.nuts.runtime.standalone.log.DefaultNLogRPI
            ,net.thevpc.nuts.runtime.standalone.log.NLogFactorySPIJUL
            ,net.thevpc.nuts.runtime.standalone.net.DefaultNConnectionStringBuilderFactory
            ,net.thevpc.nuts.runtime.standalone.platform.NEnvLocal
            ,net.thevpc.nuts.runtime.standalone.platform.NRuntimeDistributionManagerImpl
            ,net.thevpc.nuts.runtime.standalone.platform.rnsh.RnshPathFactorySPI
            ,net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectConfigurationBuilder
            ,net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectRepository
            ,net.thevpc.nuts.runtime.standalone.reflect.DefaultNReflectRPI
            ,net.thevpc.nuts.runtime.standalone.reflect.NReflectImpl
            ,net.thevpc.nuts.runtime.standalone.repository.filter.InternalNRepositoryFilterRPI
            ,net.thevpc.nuts.runtime.standalone.repository.impl.defaults.DefaultNRepoFactoryComponent
            ,net.thevpc.nuts.runtime.standalone.repository.impl.maven.MavenRepositoryFactoryComponent
            ,net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryFactoryComponent
            ,net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV1
            ,net.thevpc.nuts.runtime.standalone.security.DefaultNAuthenticationAgentV2
            ,net.thevpc.nuts.runtime.standalone.security.DefaultNSecurityManager
            ,net.thevpc.nuts.runtime.standalone.text.art.figlet.FigletNTextArtRendererFactory
            ,net.thevpc.nuts.runtime.standalone.text.art.img.PixelNTextArtRendererFactory
            ,net.thevpc.nuts.runtime.standalone.text.art.NTextArtImpl
            ,net.thevpc.nuts.runtime.standalone.text.art.table.DefaultNMutableTableModel
            ,net.thevpc.nuts.runtime.standalone.text.art.table.DefaultTableObjectWriter
            ,net.thevpc.nuts.runtime.standalone.text.art.table.TableNTextArtRendererFactory
            ,net.thevpc.nuts.runtime.standalone.text.art.tree.TreeNTextArtRendererFactory
            ,net.thevpc.nuts.runtime.standalone.text.DefaultNTextRPI
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.CppCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.CSharpCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.FishCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.HadraCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.JavaCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.JsCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.JsonCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.MarkdownCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.MtfCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.NaruCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.NTexupCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.NtfCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.PlainCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.PythonCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.RustCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.ShCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.ShoptCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.SqlCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.TsCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.TsonCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.WinCmdBlocTextHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.XmlCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.text.highlighter.YamlCodeHighlighter
            ,net.thevpc.nuts.runtime.standalone.util.filters.DefaultNFilters
            ,net.thevpc.nuts.runtime.standalone.version.filter.InternalNVersionFilterRPI
            ,net.thevpc.nuts.runtime.standalone.version.format.DefaultNVersionWriter
            ,net.thevpc.nuts.runtime.standalone.workspace.archetype.DefaultNWorkspaceArchetypeComponent
            ,net.thevpc.nuts.runtime.standalone.workspace.archetype.MinimalNWorkspaceArchetypeComponent
            ,net.thevpc.nuts.runtime.standalone.workspace.archetype.ServerNWorkspaceArchetypeComponent
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy.DefaultNDeploy
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.editorsyntax.NSysEditorSupportCmdImpl
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExec
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.format.DefaultNExecWriter
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch.DefaultNFetch
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.info.DefaultNInfo
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.install.DefaultNInstall
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.push.DefaultNPush
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.search.DefaultNSearch
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.service.DefaultInstallSvcCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias.NSettingsAliasSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.archetype.NSettingsArchetypeSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.backup.NSettingsBackupSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo.NCliInfoSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.connect.NSettingsConnectSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.delete.NSettingsDeleteFoldersSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports.NSettingsImportSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index.NSettingsIndexSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.installeditorsyntax.NSettingsInstallEditorSyntaxSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java.NSettingsJavaSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.log.NSettingsLogSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NSettingsNdiSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo.NSettingsRepositorySubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats.DefaultNUpdateStats
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user.NSettingsUserSubCommand
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall.DefaultNUninstall
            ,net.thevpc.nuts.runtime.standalone.workspace.cmd.update.DefaultNUpdate
            ,net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNIndexStoreFactory
            ,net.thevpc.nuts.runtime.standalone.workspace.list.DefaultNWorkspaceList
            ,net.thevpc.nuts.runtime.standalone.xtra.compress.DefaultNUncompress
            ,net.thevpc.nuts.runtime.standalone.xtra.compress.NCompressGzip
            ,net.thevpc.nuts.runtime.standalone.xtra.compress.NCompressZip
            ,net.thevpc.nuts.runtime.standalone.xtra.compress.NUncompressGzip
            ,net.thevpc.nuts.runtime.standalone.xtra.compress.NUncompressZip
            ,net.thevpc.nuts.runtime.standalone.xtra.contenttype.DefaultNContentTypeResolver
            ,net.thevpc.nuts.runtime.standalone.xtra.cp.DefaultNCp
            ,net.thevpc.nuts.runtime.standalone.xtra.digest.DefaultNDigest
            ,net.thevpc.nuts.runtime.standalone.xtra.expr.NExprRPIImpl
            ,net.thevpc.nuts.runtime.standalone.xtra.glob.DefaultNGlob
            ,net.thevpc.nuts.runtime.standalone.xtra.hashname.DefaultNDigestName
            ,net.thevpc.nuts.runtime.standalone.xtra.ps.DefaultNPs
            ,net.thevpc.nuts.runtime.standalone.xtra.throwables.DefaultNExceptionWorkspaceHandler
            ,net.thevpc.nuts.runtime.standalone.xtra.time.DefaultNInputStreamMonitor
            ,net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNHttpClient
    ;

    provides net.thevpc.nuts.boot.NBootWorkspaceFactory with
            net.thevpc.nuts.runtime.standalone.boot.DefaultNBootWorkspaceFactory;

    exports net.thevpc.nuts.runtime;
    exports net.thevpc.nuts.runtime.optional.jansi;
    exports net.thevpc.nuts.runtime.optional.mslink;
    exports net.thevpc.nuts.runtime.standalone;
    exports net.thevpc.nuts.runtime.standalone.app;
    exports net.thevpc.nuts.runtime.standalone.app.cmdline;
    exports net.thevpc.nuts.runtime.standalone.app.gui;
    exports net.thevpc.nuts.runtime.standalone.app.util;
    exports net.thevpc.nuts.runtime.standalone.atrifact;
    exports net.thevpc.nuts.runtime.standalone.boot;
    exports net.thevpc.nuts.runtime.standalone.cmdline;
    exports net.thevpc.nuts.runtime.standalone.collections;
    exports net.thevpc.nuts.runtime.standalone.concurrent;
    exports net.thevpc.nuts.runtime.standalone.definition;
    exports net.thevpc.nuts.runtime.standalone.definition.filter;
    exports net.thevpc.nuts.runtime.standalone.dependency;
    exports net.thevpc.nuts.runtime.standalone.dependency.filter;
    exports net.thevpc.nuts.runtime.standalone.dependency.format;
    exports net.thevpc.nuts.runtime.standalone.dependency.solver;
    exports net.thevpc.nuts.runtime.standalone.dependency.solver.gradle;
    exports net.thevpc.nuts.runtime.standalone.dependency.solver.maven;
    exports net.thevpc.nuts.runtime.standalone.dependency.util;
    exports net.thevpc.nuts.runtime.standalone.descriptor;
    exports net.thevpc.nuts.runtime.standalone.descriptor.format;
    exports net.thevpc.nuts.runtime.standalone.descriptor.parser;
    exports net.thevpc.nuts.runtime.standalone.descriptor.util;
    exports net.thevpc.nuts.runtime.standalone.elem;
    exports net.thevpc.nuts.runtime.standalone.elem.builder;
    exports net.thevpc.nuts.runtime.standalone.elem.item;
    exports net.thevpc.nuts.runtime.standalone.elem.mapper;
    exports net.thevpc.nuts.runtime.standalone.elem.mapper.builder;
    exports net.thevpc.nuts.runtime.standalone.elem.parser;
    exports net.thevpc.nuts.runtime.standalone.elem.parser.mapperstore;
    exports net.thevpc.nuts.runtime.standalone.elem.path;
    exports net.thevpc.nuts.runtime.standalone.elem.steps;
    exports net.thevpc.nuts.runtime.standalone.elem.writer;
    exports net.thevpc.nuts.runtime.standalone.event;
    exports net.thevpc.nuts.runtime.standalone.executor;
    exports net.thevpc.nuts.runtime.standalone.executor.embedded;
    exports net.thevpc.nuts.runtime.standalone.executor.exec;
    exports net.thevpc.nuts.runtime.standalone.executor.java;
    exports net.thevpc.nuts.runtime.standalone.executor.javasrc;
    exports net.thevpc.nuts.runtime.standalone.executor.pom;
    exports net.thevpc.nuts.runtime.standalone.executor.system;
    exports net.thevpc.nuts.runtime.standalone.executor.war;
    exports net.thevpc.nuts.runtime.standalone.executor.zip;
    exports net.thevpc.nuts.runtime.standalone.extension;
    exports net.thevpc.nuts.runtime.standalone.format;
    exports net.thevpc.nuts.runtime.standalone.format.desc;
    exports net.thevpc.nuts.runtime.standalone.format.impl;
    exports net.thevpc.nuts.runtime.standalone.format.json;
    exports net.thevpc.nuts.runtime.standalone.format.obj;
    exports net.thevpc.nuts.runtime.standalone.format.plain;
    exports net.thevpc.nuts.runtime.standalone.format.props;
    exports net.thevpc.nuts.runtime.standalone.format.tree;
    exports net.thevpc.nuts.runtime.standalone.format.tson;
    exports net.thevpc.nuts.runtime.standalone.format.tson.format;
    exports net.thevpc.nuts.runtime.standalone.format.tson.parser;
    exports net.thevpc.nuts.runtime.standalone.format.tson.parser.custom;
    exports net.thevpc.nuts.runtime.standalone.format.tson.util;
    exports net.thevpc.nuts.runtime.standalone.format.xml;
    exports net.thevpc.nuts.runtime.standalone.format.yaml;
    exports net.thevpc.nuts.runtime.standalone.id;
    exports net.thevpc.nuts.runtime.standalone.id.filter;
    exports net.thevpc.nuts.runtime.standalone.id.format;
    exports net.thevpc.nuts.runtime.standalone.id.util;
    exports net.thevpc.nuts.runtime.standalone.installer;
    exports net.thevpc.nuts.runtime.standalone.io;
    exports net.thevpc.nuts.runtime.standalone.io.ask;
    exports net.thevpc.nuts.runtime.standalone.io.cache;
    exports net.thevpc.nuts.runtime.standalone.io.inputstream;
    exports net.thevpc.nuts.runtime.standalone.io.net.util;
    exports net.thevpc.nuts.runtime.standalone.io.outputstream;
    exports net.thevpc.nuts.runtime.standalone.io.path;
    exports net.thevpc.nuts.runtime.standalone.io.path.spi;
    exports net.thevpc.nuts.runtime.standalone.io.path.spi.htmlfs;
    exports net.thevpc.nuts.runtime.standalone.io.path.spi.mem;
    exports net.thevpc.nuts.runtime.standalone.io.printstream;
    exports net.thevpc.nuts.runtime.standalone.io.terminal;
    exports net.thevpc.nuts.runtime.standalone.io.terminal.history;
    exports net.thevpc.nuts.runtime.standalone.io.urlpart;
    exports net.thevpc.nuts.runtime.standalone.io.util;
    exports net.thevpc.nuts.runtime.standalone.log;
    exports net.thevpc.nuts.runtime.standalone.net;
    exports net.thevpc.nuts.runtime.standalone.platform;
    exports net.thevpc.nuts.runtime.standalone.platform.rnsh;
    exports net.thevpc.nuts.runtime.standalone.reflect;
    exports net.thevpc.nuts.runtime.standalone.reflect.mapper;
    exports net.thevpc.nuts.runtime.standalone.repository;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.deploy;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.fetch;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.push;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.search;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.undeploy;
    exports net.thevpc.nuts.runtime.standalone.repository.cmd.updatestats;
    exports net.thevpc.nuts.runtime.standalone.repository.config;
    exports net.thevpc.nuts.runtime.standalone.repository.filter;
    exports net.thevpc.nuts.runtime.standalone.repository.impl;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.defaults;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.folder;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.main;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.lucene;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.solrsearch;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.nuts;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.toolbox;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;
    exports net.thevpc.nuts.runtime.standalone.repository.impl.util;
    exports net.thevpc.nuts.runtime.standalone.repository.index;
    exports net.thevpc.nuts.runtime.standalone.repository.util;
    exports net.thevpc.nuts.runtime.standalone.security;
    exports net.thevpc.nuts.runtime.standalone.security.util;
    exports net.thevpc.nuts.runtime.standalone.session;
    exports net.thevpc.nuts.runtime.standalone.store;
    exports net.thevpc.nuts.runtime.standalone.text;
    exports net.thevpc.nuts.runtime.standalone.text.art;
    exports net.thevpc.nuts.runtime.standalone.text.art.figlet;
    exports net.thevpc.nuts.runtime.standalone.text.art.img;
    exports net.thevpc.nuts.runtime.standalone.text.art.region;
    exports net.thevpc.nuts.runtime.standalone.text.art.table;
    exports net.thevpc.nuts.runtime.standalone.text.art.tree;
    exports net.thevpc.nuts.runtime.standalone.text.highlighter;
    exports net.thevpc.nuts.runtime.standalone.text.parser;
    exports net.thevpc.nuts.runtime.standalone.text.parser.v1;
    exports net.thevpc.nuts.runtime.standalone.text.parser.v2;
    exports net.thevpc.nuts.runtime.standalone.text.theme;
    exports net.thevpc.nuts.runtime.standalone.text.util;
    exports net.thevpc.nuts.runtime.standalone.util;
    exports net.thevpc.nuts.runtime.standalone.util.filters;
    exports net.thevpc.nuts.runtime.standalone.util.jclass;
    exports net.thevpc.nuts.runtime.standalone.util.stream;
    exports net.thevpc.nuts.runtime.standalone.version.filter;
    exports net.thevpc.nuts.runtime.standalone.version.format;
    exports net.thevpc.nuts.runtime.standalone.workspace;
    exports net.thevpc.nuts.runtime.standalone.workspace.archetype;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.deploy;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.editorsyntax;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.format;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.alias;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.artifact;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.open;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.path;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.system;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.artifact;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.system;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.fetch;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.help;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.info;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.install;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.license;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.prepare;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.push;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.recom;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.search;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.service;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.archetype;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.backup;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.clinfo;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.connect;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.delete;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.imports;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.index;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.installeditorsyntax;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.java;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.log;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.unix;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.util;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.win;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.repo;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.shell;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.updatestats;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.user;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.undeploy;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.uninstall;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.update;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.version;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.welcome;
    exports net.thevpc.nuts.runtime.standalone.workspace.cmd.which;
    exports net.thevpc.nuts.runtime.standalone.workspace.config;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat.v506;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat.v803;
    exports net.thevpc.nuts.runtime.standalone.workspace.config.compat.v805;
    exports net.thevpc.nuts.runtime.standalone.workspace.list;
    exports net.thevpc.nuts.runtime.standalone.xtra.compress;
    exports net.thevpc.nuts.runtime.standalone.xtra.contenttype;
    exports net.thevpc.nuts.runtime.standalone.xtra.cp;
    exports net.thevpc.nuts.runtime.standalone.xtra.digest;
    exports net.thevpc.nuts.runtime.standalone.xtra.execentries;
    exports net.thevpc.nuts.runtime.standalone.xtra.expr;
    exports net.thevpc.nuts.runtime.standalone.xtra.expr.template;
    exports net.thevpc.nuts.runtime.standalone.xtra.glob;
    exports net.thevpc.nuts.runtime.standalone.xtra.hashname;
    exports net.thevpc.nuts.runtime.standalone.xtra.idresolver;
    exports net.thevpc.nuts.runtime.standalone.xtra.mon;
    exports net.thevpc.nuts.runtime.standalone.xtra.nanodb;
    exports net.thevpc.nuts.runtime.standalone.xtra.nanodb.file;
    exports net.thevpc.nuts.runtime.standalone.xtra.nanodb.mem;
    exports net.thevpc.nuts.runtime.standalone.xtra.ntalk;
    exports net.thevpc.nuts.runtime.standalone.xtra.ps;
    exports net.thevpc.nuts.runtime.standalone.xtra.shell;
    exports net.thevpc.nuts.runtime.standalone.xtra.throwables;
    exports net.thevpc.nuts.runtime.standalone.xtra.time;
    exports net.thevpc.nuts.runtime.standalone.xtra.web;
}
