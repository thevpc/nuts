/**
 * Nuts : Network Updatable Things Service (universal package manager) is
 * a new Open Source Package Manager to help install packages
 * and libraries for runtime execution.
 * <br>
 * <strong>nuts</strong> stands for <strong>Network Updatable Things Services</strong> tool.
 * It's a <strong>Package Manager</strong> for (mainly) Java
 * and non Java world. It helps discovering, downloading, assembling and executing  remote artifacts (packages) in
 * a very handy way.
 * <br>
 * <br>
 * Each managed artifact  is also called a <strong>nuts</strong> which  is a <i>Network Updatable Thing Service</i> .
 * <strong>nuts</strong> artifacts are  stored  into repositories. A  repository  may be local for  storing
 * local <strong>nuts</strong> or remote for accessing  remote artifacts (good examples  are  remote maven
 * repositories). It may also be a proxy repository so that remote artifacts are fetched and cached locally to save network
 * resources.
 * <br>
 * <br>
 * One manages a set of repositories called a  workspace. Managed <strong>nuts</strong>  (artifacts)  have descriptors
 * that depict dependencies between them. This dependency is seamlessly handled by  <strong>nuts</strong>  (tool) to
 * resolve and download on-need dependencies over the wire.
 * <br>
 * <br>
 * <strong>nuts</strong> is a swiss army knife tool as it acts like (and supports) maven build tool to have an abstract
 * view of the  artifacts dependency and like  <i>zypper</i>/<i>apt-get</i>/<i>pip</i>/<i>npm</i>  package manager tools
 * to install and uninstall artifacts allowing multiple versions of the very same artifact to  be installed.
 * <br>
 *
 * <br>
 * Although <strong>nuts</strong> focuses on java artifacts, it still supports, by design, native and all non java artifacts.
 * Dependencies are fetched according to the current operating system type, distribution and hardware
 * architectures.
 * <br>
 *
 * <br>
 * <strong>nuts</strong> works either as standalone application or as java library to enable dynamic and dependency
 * aware class loading and brings a rich toolset to provide a versatile portable command line tools such
 * as <strong>nsh</strong> (a bash like shell), <strong>tomcat</strong>, <strong>derby</strong> ... wrapper tools to make it easier providing
 * development , test and deployment reproducible environments.
 * <br>
 *
 * <br>
 * With container concepts in mind, <strong>nuts</strong> is the perfect java application tool for <i>Docker</i>, <i>CoreOs</i>
 * and other container engines.
 * <br>
 * <br>
 * <strong>COMMON VERBS:</strong>
 * <ul>
 * <li>install,uninstall,update : install/uninstall/update an artifact (using its fetched/deployed installer)</li>
 * <li>deploy, undeploy         : manage artifacts on the local repositories</li>
 * <li>fetch,push               : download, upload to remote repositories</li>
 * <li>search                   : search for existing/installable artifacts</li>
 * <li>exec                     : execute an artifact (tool)</li>
 * </ul>
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;
