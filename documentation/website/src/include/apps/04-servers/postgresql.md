---
id: app-postgresql-server
installCommand: nuts install org.postgresql:postgresql-server
exampleCommand: nuts org.postgresql:postgresql-server pg_ctl -D ~/pgdata -l ~/pgdata/logfile start
---


## T0017- PostgreSQL Server
install a portable version of PostgreSQL right from nuts

### install
```bash
nuts install org.postgresql:postgresql-server
```

### prepare pgdata
```bash
PGDATA=~/pgdata
nuts org.postgresql:postgresql-server initdb -D $PGDATA
echo "port = 8666" >> ~/pgdata/postgresql.conf
echo "unix_socket_directories = '/tmp'" >> $PGDATA/postgresql.conf
```

### run the server
```bash
nuts org.postgresql:postgresql-server pg_ctl -D ~/pgdata -l ~/pgdata/logfile start
```

> psql is not included; users can connect via psql -h localhost -p 8666 if they have the PostgreSQL client installed separately.