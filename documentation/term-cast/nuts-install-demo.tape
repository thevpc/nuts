Output nuts-install-demo.gif
Set FontSize 14
Set Width 1200
Set Height 600
Set LoopOffset 0

Type "# Let's install nuts..."
Enter
Sleep 2s

Type "curl -s https://thevpc.net/nuts/install-latest.sh | bash"
Enter
Sleep 3s

Type "# source .bashrc to make nuts available in the current shell..."
Enter
Sleep 2s
Type ". ~/.bashrc"
Enter
Sleep 3s

Type "# our first checkup..."
Enter
Sleep 2s
Type "nuts version"
Enter
Sleep 3s

Type "# more details on how to use nuts can be found in the help section..."
Enter
Sleep 2s
Type "nuts help"
Enter
Sleep 3s

Type "# lets search for already installed nuts packages..."
Enter
Sleep 2s
Type "nuts search --installed"
Enter
Sleep 3s

Type "# ...hmm none there,... let's install some..."
Enter
Sleep 2s
Type "nuts -y install nsh"
Enter
Sleep 3s

Type "# ...nsh and its dependencies are installed. Lets check the installed packages..."
Enter
Sleep 2s
Type "nuts search --installed --distinct --long"
Enter
Sleep 3s

Type "# ...lets run enhanced bash (nsh shell) and get json results..."
Enter
Sleep 2s
Type "nuts -y nsh -c ls --json | head -20"
Enter
Sleep 8s

Type "# ...or tson..."
Enter
Sleep 2s

Type "nuts -y nsh -c ls --tson | head -20"
Enter
Sleep 6s

Type "# ...or as tree..."
Enter
Sleep 2s

Type "nuts -y nsh -c ls --tree | head -20"
Enter
Sleep 6s

Type "# ...now lets download another application from maven central repository..."
Enter
Sleep 1s
Type "# checkstyle is a good example..."
Enter
Sleep 2s

Type "nuts -y com.puppycrawl.tools:checkstyle -help"
Enter
Sleep 5s

Type "# ...now lets run a webserver..."
Enter
Sleep 2s
Type "curl -sL https://repo1.maven.org/maven2/com/example/simple-servlet/1.0/simple-servlet-1.0.war -o demo.war"
Enter
Sleep 1s
Type "nuts -yb  --exec --class=1 org.eclipse.jetty:jetty-runner --port 8080 demo.war  > server.log 2>&1 &"
Enter
Sleep 10s

Type "# Verify it's running"
Enter
Sleep 2s
Type "curl -s http://localhost:8080"
Enter
Type "tail server.log"
Enter
Sleep 3s

Type "# Show it's a real process"
Enter
Sleep 2s
Type "ps aux | grep jetty-runner | grep org.eclipse.jetty.runner.Runner | grep -v grep | awk '{print $2}'"
Enter
Sleep 3s

Type "# Cleanup"
Enter
Type "kill %1"
Enter
Sleep 2s

Type "# nuts can also update all installed packages at once..."
Enter
Sleep 2s
Type "nuts -y update"
Enter
Sleep 3s

Type "# ...or check nuts itself..."
Enter
Sleep 2s
Type "nuts info"
Enter
Sleep 3s
Type "# -----------------------------------------------"
Enter
Type "# ...nuts is ready. Happy packaging!"
Enter
Type "# -----------------------------------------------"
Enter
Enter
Sleep 10s