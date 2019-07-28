/**
 * Nuts : Network Updatable Things Service (universal package manager) is
 * a new Open Source Package Manager to help install packages
 * and libraries for runtime execution.
 * <p>
 * <strong>nuts</strong> stands for <strong>Network Updatable Things Services</strong> tool.
 * It is a <strong>Package Manager</strong> for (mainly) java
 * and non Java world. It helps discovering, downloading, assembling and executing  remote artifacts (packages) in
 * a very handy way.
 * </p>
 * <p>
 * Each managed artifact  is also called a <strong>nuts</strong> which  is a <i>Network Updatable Thing Service</i> .
 * <strong>nuts</strong> artifacts are  stored  into repositories. A  repository  may be local for  storing
 * local <strong>nuts</strong> or remote for accessing  remote artifacts (good examples  are  remote maven
 * repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network
 * resources.
 * </p>
 * <p>
 * One manages a set of repositories called a  workspace. Managed <strong>nuts</strong>  (artifacts)  have descriptors
 * that depicts dependencies between them. This dependency is seamlessly handled by  <strong>nuts</strong>  (tool) to
 * resolve and download on-need dependencies over the wire.
 * </p>
 * <p>
 * <strong>nuts</strong> is a swiss army knife tool as it acts like (and supports) maven build tool to have an abstract
 * view of the the  artifacts dependency and like  <i>zypper</i>/<i>apt-get</i>/<i>pip</i>/<i>npm</i>  package manager tools
 * to install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed.
 * </p>
 *
 * <p>
 * Although <strong>nuts</strong> focuses on java artifacts, it still supports, by design, native and all non java artifacts.
 * Dependencies are fetched according to the current operating system type, distribution and hardware
 * architectures.
 * </p>
 *
 * <p>
 * <strong>nuts</strong> works either as standalone application or as java library to enable dynamic and dependency
 * aware class loading and brings a rich toolset to provide a versatile portable command line tools such
 * as <strong>nsh</strong> (a bash like shell), <strong>tomcat</strong>, <strong>derby</strong> ... wrapper tools to make it easier providing
 * development , test and deployment reproducible environments.
 * </p>
 *
 * <p>
 * With container concepts in mind, <strong>nuts</strong> is the perfect java application tool for <i>Docker</i>, <i>CoreOs</i>
 * and other container managers.
 * </p>
 * <p>
 * <strong>COMMON VERBS:</strong>
 * <ul>
 * <li>install,uninstall,update : install/uninstall/update an artifact (using its fetched/deployed installer)</li>
 * <li>deploy, undeploy         : manage artifacts on the local repositories</li>
 * <li>fetch,push               : download, upload to remote repositories</li>
 * <li>search                   : search for existing/installable artifacts</li>
 * <li>exec                     : execute an artifact (tool)</li>
 * </ul>
 * Copyright (C) 2018-2019 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * </p>
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * </p>
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * </p>
 */
package net.vpc.app.nuts;
