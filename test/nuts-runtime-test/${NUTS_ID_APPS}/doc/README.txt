jEdit README

* About jEdit

jEdit is a cross platform programmer's text editor written in Java.
jEdit requires Java 11 Runtime or later.
It will not run with earlier versions of Java.

jEdit comes with full online help; to read it, select 'jEdit Help' from
jEdit's 'Help' menu or point your web browser to the file named
'index.html' in the 'doc/users-guide/' directory of the jEdit
distribution.

A PDF (Adobe Acrobat) version of the user's guide be downloaded from
<http://www.jedit.org>.

A list of people who contributed to jEdit, either by submitting code or
edit modes to the core, or writing plugins can be viewed by selecting
'About jEdit' from jEdit's 'Help' menu.

* License

jEdit is free software, and you are welcome to redistribute and modify
it under the terms of the GNU General Public License (either version 2
or any later version). See the file COPYING.txt for details.

The jEdit installer is released under a public domain license and may be
used for any purpose.

A number of plugins are available for jEdit. Unless otherwise stated in
the plugin's documentation, each of the plugins is licensed for use and
redistribution under the terms of the GNU General Public License (either
version 2 or any later version, at the user's election).

The user's guide is released under the terms of the GNU Free
Documentation License, Version 1.1 or any later version published by the
Free Software Foundation; with no "Invariant Sections", "Front-Cover
Texts" or "Back-Cover Texts", each as defined in the license. A copy of
the license can be found in the file COPYING.DOC.txt.

The class libraries shipped with jEdit each have their own license; see
the 'Libraries' section below.

* jEdit on the Internet

The jEdit homepage, located at <http://www.jedit.org> contains the
latest version of jEdit, along with plugin downloads. There is also a
user-oriented site, <http://community.jedit.org>. Check it out.

If you would like to report a bug, first take a look at the 'jEdit
Frequently Asked Questions' document, accessible from either jEdit's
online help or by pointing your web browser to the file named
'index.html' in the 'doc/FAQ/' directory of the jEdit distribution.

If the FAQ doesn't answer your question, report a bug with our bug
tracker, located at <http://www.jedit.org/?page=feedback>.

When writing a bug report, please try to be as specific as possible. You
should specify your jEdit version, Java version, operating system, any
relevant output from the activity log, and an e-mail address, in case we
need further information from you to fix the bug.

The 'Make Bug Report' macro included with jEdit, which can be found in
the 'Misc' submenu of the 'Macros' menu, might be useful when preparing
a bug report.

If you would like to discuss jEdit and be informed when new versions are
released, you should subscribe to the mailing lists; see
<http://www.jedit.org/?page=feedback> for details.

If you would like to discuss the BeanShell scripting language, subscribe
to one of the BeanShell mailing lists by visiting
<http://www.beanshell.org/contact.html>.

Finally, if you want to chat about jEdit with other users and
developers, come join the #jedit channel on irc.freenode.net. You
can use the IRC plugin, available from <http://plugins.jedit.org>, for
this purpose.

* Libraries

jEdit bundles the following libraries:

- BeanShell scripting language, by Pat Niemeyer. jEdit bundles BeanShell
  version 2.0b4 with the bsh.util and bsh.classpath packages removed.

  BeanShell is released under a dual Sun Public License/GNU LGPL
  license. See the BeanShell homepage for details.

  The BeanShell web site is <http://www.beanshell.org>.

- ASM bytecode generation library from Objectweb. This class library is
  released under the 'GNU Lesser General Public License'.
  The ASM homepage can be found at <http://asm.objectweb.org/>.
  The library is in org/gjt/sp/jedit/bsh/org/objectweb/asm/.

The jEdit installer bundles the following libraries:

- org.apache.excalibur.bzip2 compression library. This library is
  released under the Apache license, which can be found in
  doc/Apache.LICENSE.txt.

  The org.apache.excalibur.bzip2 web site is
  <http://jakarta.apache.org/avalon/excalibur/bzip2/>.

- com.ice.tar, by Tim Endres. This code is in the public domain.

  The com.ice.tar web site is <http://www.trustice.com/java/tar/>.

