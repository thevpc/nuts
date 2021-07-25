package net.thevpc.nuts.lib.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsPathSPI;

import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;

class SshNutsPath implements NutsPathSPI {
    private SshPath path;
    private NutsSession session;
    private SshListener listener;

    public SshNutsPath(SshPath path, NutsSession session) {
        this.path = path;
        this.session = session;
    }

    @Override
    public String asString() {
        return path.toString();
    }

    @Override
    public NutsString asFormattedString() {
        //should implement better formatting...
        NutsTextStyle _sep = NutsTextStyle.separator();
        NutsTextStyle _path = NutsTextStyle.path();
        NutsTextStyle _nbr = NutsTextStyle.number();
//        if(true) {
            NutsTextManager text = session.getWorkspace().text();
            NutsTextBuilder sb = text.builder();
            String user=this.path.getUser();
            String host=this.path.getHost();
            int port=this.path.getPort();
            String path=this.path.getPath();
            String password=this.path.getPassword();
            String keyFile=this.path.getKeyFile();

            sb.append(text.forStyled("ssh://", _sep));
            if (!(user == null || user.trim().length() == 0)) {
                sb.append(user)
                        .append(text.forStyled("@", _sep));
            }
            sb.append(host);
            if (port >= 0) {
                sb.append(text.forStyled(":", _sep))
                        .append(text.forStyled(String.valueOf(port),_nbr));
            }
            if (!path.startsWith("/")) {
                sb.append(text.forStyled('/'+path,_path));
            }else {
                sb.append(text.forStyled(path,_path));
            }
            if (password != null || keyFile != null) {
                sb.append(text.forStyled("?",_sep));
                boolean first = true;
                if (password != null) {
                    first = false;
                    sb
                            .append("password")
                            .append(text.forStyled("=",_sep))
                            .append(password);
                }
                if (keyFile != null) {
                    if (!first) {
                        sb.append(text.forStyled(",",_sep));
                    }
                    sb
                            .append("key-file")
                            .append(text.forStyled("=",_sep))
                            .append(keyFile);
                }
            }
            return sb.toText();
//        }

//        return getSession().getWorkspace().text().forStyled(path.toString(), _sep);
        //return getSession().getWorkspace().text().toText(path);
    }
    @Override
    public InputStream inputStream() {
        return new SshFileInputStream(path,session);
    }

    @Override
    public OutputStream outputStream() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported output stream for %s",toString()));
    }

    @Override
    public long length() {
        return -1;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public void delete(boolean recurse) {
        try (SShConnection session = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            session.rm(path.getPath(), recurse);
        }
    }

    public void mkdir(boolean parents) {
        try (SShConnection c = new SShConnection(path.toAddress(),getSession())
                .addListener(listener)
        ) {
            c.mkdir(path.getPath(), parents);
        }
    }

    @Override
    public String location() {
        return path.getPath();
    }

    @Override
    public boolean exists() {
        throw new NutsIOException(getSession(), NutsMessage.cstyle("not supported exists for %s",toString()));
    }

    @Override
    public Instant lastModifiedInstant() {
        return null;
    }
}
