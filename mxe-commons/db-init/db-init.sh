#!/bin/bash

NAMESPACE=$(cat /var/run/secrets/kubernetes.io/serviceaccount/namespace)
PORT=5432

cd /home/dbinit/.postgresql/
echo "${POSTGRESQL_SERVICE}:${PORT}:*:${POSTGRESQL_SUPERUSER_USER}:${POSTGRESQL_SUPERUSER_PWD}" >/home/dbinit/.postgresql/.pgpass
chmod 0600 /home/dbinit/.postgresql/.pgpass
export PGPASSFILE=/home/dbinit/.postgresql/.pgpass

echo "Namespace: ${NAMESPACE}"
echo "Host: ${POSTGRESQL_SERVICE}"
echo "Port: ${PORT}"
echo "User: ${POSTGRESQL_SUPERUSER_USER}"

psql="psql -U ${POSTGRESQL_SUPERUSER_USER} -h ${POSTGRESQL_SERVICE} -p ${PORT}"

if [ -n "${POSTGRESL_CONNECTION_DB}" ]; then
    psql="${psql} -d ${POSTGRESL_CONNECTION_DB}"
fi

echo "Trying to connect..."

for i in {1..60}; do
    if $($psql -c "\echo true" 2>/dev/null || echo "false"); then
        echo "Creating new user: ${POSTGRESQL_CREATEDUSER_USER}"

        $psql <<EOF
CREATE OR REPLACE FUNCTION createuserifnotexists(username text, password text) RETURNS boolean AS \$\$
DECLARE
    userCount integer;
BEGIN
    EXECUTE 'SELECT COUNT(*) FROM pg_catalog.pg_roles WHERE rolname = \$1'
    INTO userCount
    USING username;

    IF userCount = 0 THEN
        EXECUTE format('CREATE role %I LOGIN PASSWORD %L', username, password);
        RETURN true;
    ELSE
        EXECUTE format('ALTER role %I LOGIN PASSWORD %L', username, password);
        RETURN false;
    END IF;
END;
\$\$ LANGUAGE plpgsql;
EOF

        $psql -v username="${POSTGRESQL_CREATEDUSER_USER}" -v pwd="${POSTGRESQL_CREATEDUSER_PWD}" <<EOF
SELECT createuserifnotexists(:'username', :'pwd');
EOF

        echo "User created."

        echo "Creating new database: ${POSTGRESQL_DB}"
        if $psql -v dbname="${POSTGRESQL_DB}" -v username="${POSTGRESQL_CREATEDUSER_USER}" -v ON_ERROR_STOP=true <<EOF
CREATE DATABASE :"dbname" OWNER :'username';
EOF
        then
            echo "Database created."
        else
            echo "Database was already created.";
        fi

        echo "Modifying access rights for database..."
        $psql -v dbname="${POSTGRESQL_DB}" -v username="${POSTGRESQL_SUPERUSER_USER}" <<EOF
REVOKE ALL PRIVILEGES ON DATABASE :"dbname" FROM :"username"
EOF
        echo "Modification done."

        echo "Exiting..."
        exit 0
    else
        echo -e "Connection failed!\nRetrying..."
        sleep 10
    fi
done

echo -e "Database is not reachable!\nExiting..."
exit 1
